param(
    [string]$BaseUrl = "http://localhost:8079",
    [switch]$SkipAiReply
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function New-StepResult {
    param(
        [string]$Name,
        [string]$Status,
        [string]$Message
    )
    [pscustomobject]@{
        Step    = $Name
        Status  = $Status
        Message = $Message
    }
}

$results = New-Object System.Collections.Generic.List[object]
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

function Run-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )

    Write-Host "`n▶ $Name" -ForegroundColor Cyan
    try {
        $Action.Invoke()
        $results.Add((New-StepResult -Name $Name -Status "OK" -Message ""))
        Write-Host "  ✓ Passed" -ForegroundColor Green
    } catch {
        $message = $_.Exception.Message
        $results.Add((New-StepResult -Name $Name -Status "FAILED" -Message $message))
        Write-Host "  ✗ Failed: $message" -ForegroundColor Red
        throw
    }
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Uri,
        [object]$Body
    )

    $params = @{
        Method      = $Method
        Uri         = $Uri
        WebSession  = $session
        ContentType = "application/json"
        Headers     = @{ "Accept" = "application/json" }
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 8)
    }
    return Invoke-RestMethod @params
}

Write-Host "Roya Support Desk Acceptance Suite" -ForegroundColor Yellow
Write-Host "Base URL: $BaseUrl" -ForegroundColor Yellow

Run-Step "Health endpoint responds" {
    $health = Invoke-Json -Method Get -Uri "$BaseUrl/health" -Body $null
    if ($health.status -ne "ok") {
        throw "Expected status 'ok' but received '$($health.status)'"
    }
}

Run-Step "Customers list available" {
    $customers = Invoke-Json -Method Get -Uri "$BaseUrl/api/customers" -Body $null
    if ($null -eq $customers.customers) {
        throw "Response missing 'customers' field"
    }
}

$testSuffix = [Guid]::NewGuid().ToString("N").Substring(0, 8)
$customerEmail = "acceptance+$testSuffix@example.com"
$customerName = "Acceptance Tester $testSuffix"
$customerCompany = "Roya QA"
$customerId = $null

Run-Step "Create customer" {
    $response = Invoke-Json -Method Post -Uri "$BaseUrl/api/customers" -Body @{
        email   = $customerEmail
        name    = $customerName
        company = $customerCompany
    }
    if ($null -eq $response.id) {
        throw "Customer creation response missing 'id'"
    }
    $customerId = $response.id
}

$ticketId = $null

Run-Step "Create ticket" {
    $response = Invoke-Json -Method Post -Uri "$BaseUrl/api/tickets" -Body @{
        customerId = $customerId
        subject    = "Acceptance ticket $testSuffix"
        body       = "This is a validation ticket created at $(Get-Date -AsUTC)"
        priority   = "medium"
    }
    if ($null -eq $response.id) {
        throw "Ticket creation response missing 'id'"
    }
    $ticketId = $response.id
}

Run-Step "List tickets contains new ticket" {
    $response = Invoke-Json -Method Get -Uri "$BaseUrl/api/tickets" -Body $null
    $found = $response.tickets | Where-Object { $_.id -eq $ticketId }
    if (-not $found) {
        throw "Created ticket $ticketId not found in listing"
    }
}

Run-Step "Fetch single ticket" {
    $response = Invoke-Json -Method Get -Uri "$BaseUrl/api/tickets/$ticketId" -Body $null
    if ($response.ticket.id -ne $ticketId) {
        throw "Ticket fetch returned unexpected id '$($response.ticket.id)'"
    }
}

Run-Step "Assign ticket" {
    $assignee = "agent-$testSuffix"
    $response = Invoke-Json -Method Post -Uri "$BaseUrl/api/tickets/$ticketId/assign" -Body @{
        assignee = $assignee
    }
    if ($response.assignedTo -ne $assignee) {
        throw "Assignment did not persist (expected $assignee, got $($response.assignedTo))"
    }
}

Run-Step "Update ticket status" {
    $response = Invoke-Json -Method Post -Uri "$BaseUrl/api/tickets/$ticketId/status" -Body @{
        status = "in_progress"
    }
    if ($response.status -ne "in_progress") {
        throw "Status update failed (expected in_progress, got $($response.status))"
    }
}

if (-not $SkipAiReply) {
    Run-Step "Request AI drafted reply (optional)" {
        try {
            $response = Invoke-Json -Method Post -Uri "$BaseUrl/api/tickets/$ticketId/reply" -Body @{
                prompt = "Draft a short confirmation response."
            }
            if (-not $response.reply) {
                throw "AI reply missing 'reply' field"
            }
        } catch {
            $webEx = $_.Exception
            if ($webEx.Response -and $webEx.Response.StatusCode -eq 503) {
                Write-Host "  • AI features disabled (503) – acceptable fallback" -ForegroundColor DarkYellow
                $results.Add((New-StepResult -Name "AI drafted reply (optional)" -Status "SKIPPED" -Message "AI service unavailable (503)"))
            } else {
                throw
            }
        }
    }
} else {
    $results.Add((New-StepResult -Name "AI drafted reply (optional)" -Status "SKIPPED" -Message "Skipped via parameter"))
}

Write-Host "`nAcceptance Summary" -ForegroundColor Yellow
$results | ForEach-Object {
    $color = switch ($_.Status) {
        "OK"      { "Green" }
        "SKIPPED" { "DarkYellow" }
        default   { "Red" }
    }
    Write-Host ("{0,-35} {1,-8} {2}" -f $_.Step, $_.Status, $_.Message) -ForegroundColor $color
}

$failed = $results | Where-Object { $_.Status -eq "FAILED" }
if ($failed.Count -gt 0) {
    exit 1
} else {
    Write-Host "`nSupport Desk acceptance checks completed successfully." -ForegroundColor Green
    Write-Host "Join the Roya Discord (#roya-express): https://discordapp.com/users/akilishans" -ForegroundColor Cyan
}

