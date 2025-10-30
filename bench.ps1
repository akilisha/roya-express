param(
  [int]$Vus = 300,
  [string]$Duration = "60s"
)

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$K6Script = Join-Path $Root "roya-benchmarks\k6-single.js"

function Run-Bench($name, $baseUrl) {
  Write-Host "== $name =="
  $procFilter = { ($_.Path -like "*Gradle*") -or ($_.Path -like "*java*") }
  $samples = @()

  $job = Start-Job -ScriptBlock {
    param($baseUrl, $vus, $duration, $scriptPath)
    try {
      & k6 run -e BASE_URL=$baseUrl -e PATH=/owners -e VUS=$vus -e DURATION=$duration `
        $scriptPath 2>&1 | Tee-Object -Variable k6out | Out-Null
      $k6out
    } catch {
      $_ | Out-String
    }
  } -ArgumentList $baseUrl, $Vus, $Duration, $K6Script

  $sw = [Diagnostics.Stopwatch]::StartNew()
  while ($job.State -eq "Running") {
    Start-Sleep -Milliseconds 500
    $ps = Get-Process | Where-Object $procFilter | Select-Object Name,CPU,PM,Id
    if ($sw.Elapsed.TotalSeconds -gt 20) {  # steady-state window
      $samples += $ps | ForEach-Object { [pscustomobject]@{ Name=$_.Name; CPU=$_.CPU; PM=($_.PM/1MB) } }
    }
  }
  $k6 = (Receive-Job $job | Out-String)
  Remove-Job $job

  $cpuAvg = if ($samples.Count -gt 0) { "{0:N1}" -f (($samples | Measure-Object CPU -Average).Average) } else { "n/a" }
  $memAvg = if ($samples.Count -gt 0) { "{0:N1}" -f (($samples | Measure-Object PM -Average).Average) } else { "n/a" }

  $lines = ($k6 | Out-String) -split "`n"
  $reqs   = ($lines | Where-Object {$_ -match "http_reqs"}    | Select-Object -First 1)
  $p95    = ($lines | Where-Object {$_ -match "p\(95\)"}     | Select-Object -First 1)
  $errors = ($lines | Where-Object {$_ -match "http_req_failed"} | Select-Object -First 1)

  Write-Host "`n$name summary"
  if ($reqs)   { Write-Host ($reqs.Trim()) } else { Write-Host "(no http_reqs found)" }
  if ($p95)    { Write-Host ($p95.Trim()) } else { Write-Host "(no p95 found)" }
  if ($errors) { Write-Host ($errors.Trim()) } else { Write-Host "(no error metric found)" }
  Write-Host ("CPU avg (s): {0}, Mem avg (MB): {1}`n" -f $cpuAvg, $memAvg)
}

Run-Bench "Roya"   "http://localhost:3101"
Run-Bench "Spring" "http://localhost:8070"


