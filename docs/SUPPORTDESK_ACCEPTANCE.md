## Support Desk Acceptance Checklist

Run the automated script after the Support Desk service is up (fat JAR or dev run). The script will exercise the core REST workflow end-to-end using fresh, randomised data so it is idempotent.

```powershell
# From repo root (requires PowerShell 7+)
pwsh ./scripts/supportdesk_acceptance.ps1 -BaseUrl http://localhost:8079
```

Optional flag:

```powershell
-SkipAiReply   # Skips the AI draft reply probe when AI credentials are not configured
```

### Hugging Face provider

Set the following when using Hugging Face Inference as the AI backend:

```powershell
-Dai.provider=huggingface `
-Dai.huggingface.apiKey=hf_xxx `
[-Dai.huggingface.modelId=tiiuae/falcon-7b-instruct] `
[-Dai.huggingface.temperature=0.7] `
[-Dai.huggingface.maxNewTokens=256]
```

Any of the options can also be supplied as environment variables
(`AI_HUGGINGFACE_API_KEY`, `HUGGINGFACE_MODEL_ID`, etc.).

---

## Manual Acceptance Flow

Run these calls from a clean database (or adapt IDs accordingly). Replace values in angle brackets as needed.

> All examples assume the service is listening on `http://localhost:8079`.

### 1. Health probe

```
curl http://localhost:8079/health
```

Expected (timestamp will vary):

```json
{
  "timestamp": "2025-11-10T17:25:14.003082600Z",
  "service": "support-desk",
  "status": "ok"
}
```

### 2. List customers (should be empty on a fresh DB)

```
curl http://localhost:8079/api/customers
```

```json
{
  "customers": []
}
```

### 3. Create customer

```
curl -X POST http://localhost:8079/api/customers ^
  -H "Content-Type: application/json" ^
  -d "{""email"":""qa+acme@example.com"",""name"":""QA Acme"",""company"":""Acme Corp""}"
```

Success → HTTP 201 and the created record:

```json
{
  "id": "4e6d05b3-5c9a-4f4c-9f47-6dd4c2c4c01a",
  "email": "qa+acme@example.com",
  "name": "QA Acme",
  "company": "Acme Corp",
  "createdAt": "2025-11-10T17:26:03.821231Z"
}
```

Capture the `id` for subsequent steps (call it `<CUSTOMER_ID>`).

### 4. Create ticket

```
curl -X POST http://localhost:8079/api/tickets ^
  -H "Content-Type: application/json" ^
  -d "{""customerId"":""<CUSTOMER_ID>"",""subject"":""Printer offline"",""body"":""The marketing printer is not responding"",""priority"":""high""}"
```

Success → HTTP 201 and ticket payload:

```json
{
  "id": "9d5f8f51-0756-452b-b9e5-8390f36c1d5a",
  "customerId": "4e6d05b3-5c9a-4f4c-9f47-6dd4c2c4c01a",
  "subject": "Printer offline",
  "body": "The marketing printer is not responding",
  "status": "open",
  "priority": "high",
  "assignedTo": null,
  "createdAt": "2025-11-10T17:27:14.115126Z",
  "updatedAt": "2025-11-10T17:27:14.115126Z"
}
```

Keep the `id` as `<TICKET_ID>`.

### 5. List tickets

```
curl http://localhost:8079/api/tickets
```

```json
{
  "tickets": [
    {
      "id": "<TICKET_ID>",
      "customerId": "<CUSTOMER_ID>",
      "subject": "Printer offline",
      "body": "The marketing printer is not responding",
      "status": "open",
      "priority": "high",
      "assignedTo": null,
      "createdAt": "2025-11-10T17:27:14.115126Z",
      "updatedAt": "2025-11-10T17:27:14.115126Z"
    }
  ]
}
```

Filters (`?status=`, `?priority=`) should narrow the list as expected.

### 6. Fetch ticket details

```
curl http://localhost:8079/api/tickets/<TICKET_ID>
```

```json
{
  "ticket": {
    "id": "<TICKET_ID>",
    "customerId": "<CUSTOMER_ID>",
    "subject": "Printer offline",
    "body": "The marketing printer is not responding",
    "status": "open",
    "priority": "high",
    "assignedTo": null,
    "createdAt": "2025-11-10T17:27:14.115126Z",
    "updatedAt": "2025-11-10T17:27:14.115126Z"
  },
  "customer": {
    "id": "<CUSTOMER_ID>",
    "email": "qa+acme@example.com",
    "name": "QA Acme",
    "company": "Acme Corp",
    "createdAt": "2025-11-10T17:26:03.821231Z"
  },
  "messages": [
    {
      "id": "d97e7f5f-6c8d-490a-9c99-5b9d4234f8dc",
      "author": "customer",
      "message": "The marketing printer is not responding",
      "createdAt": "2025-11-10T17:27:14.115126Z"
    }
  ]
}
```

### 7. Assign the ticket

```
curl -X POST http://localhost:8079/api/tickets/<TICKET_ID>/assign ^
  -H "Content-Type: application/json" ^
  -d "{""assignee"":""l2-escalation""}"
```

Expected response (status stays `open`, assignee changes):

```json
{
  "id": "<TICKET_ID>",
  "customerId": "<CUSTOMER_ID>",
  "subject": "Printer offline",
  "body": "The marketing printer is not responding",
  "status": "open",
  "priority": "high",
  "assignedTo": "l2-escalation",
  "createdAt": "2025-11-10T17:27:14.115126Z",
  "updatedAt": "2025-11-10T17:29:02.441882Z"
}
```

### 8. Update ticket status

```
curl -X POST http://localhost:8079/api/tickets/<TICKET_ID>/status ^
  -H "Content-Type: application/json" ^
  -d "{""status"":""in_progress""}"
```

Response with new status:

```json
{
  "id": "<TICKET_ID>",
  "customerId": "<CUSTOMER_ID>",
  "subject": "Printer offline",
  "body": "The marketing printer is not responding",
  "status": "in_progress",
  "priority": "high",
  "assignedTo": "l2-escalation",
  "createdAt": "2025-11-10T17:27:14.115126Z",
  "updatedAt": "2025-11-10T17:30:01.028312Z"
}
```

### 9. AI-drafted reply

> Requires configured AI provider (e.g. OpenAI, Hugging Face). Without credentials the endpoint returns HTTP 503 and JSON `{"error":"AI features are disabled - configure AI credentials."}`.

```
curl -X POST http://localhost:8079/api/tickets/<TICKET_ID>/reply ^
  -H "Content-Type: application/json" ^
  -d "{""prompt"":""Draft a short update for the requester."" }"
```

Successful (example with Hugging Face provider):

```json
{
  "ticket": {
    "id": "<TICKET_ID>",
    "customerId": "<CUSTOMER_ID>",
    "subject": "Printer offline",
    "body": "The marketing printer is not responding",
    "status": "in_progress",
    "priority": "high",
    "assignedTo": "l2-escalation",
    "createdAt": "2025-11-10T17:27:14.115126Z",
    "updatedAt": "2025-11-10T17:30:01.028312Z"
  },
  "reply": "Hi QA Acme,\n\nWe've restarted the print server and are monitoring the job queue. Please retry within the next 10 minutes and let us know if the issue persists.\n\nRegards,\nSupport",
  "summary": "• Restarted the print server\n• Monitoring print queue for errors\n• Awaiting confirmation from marketing team",
  "citations": "Printer onboarding checklist - https://kb.acme.internal/print/onboarding\nTroubleshooting: network printers - https://kb.acme.internal/print/network"
}
```

---

### What the script validates

1. `/health` responds with `status: "ok"`.
2. `/api/customers` list is reachable.
3. Customer creation.
4. Ticket creation for the new customer.
5. Ticket appears in list view and single-ticket fetch.
6. Assignment update persists.
7. Status update persists.
8. AI draft reply endpoint (success or clean 503 fallback).

All requests are issued through a shared web session, so middleware such as cookies, sessions, rate limits, and logging are exercised. A summary is printed at the end and the script exits with a non-zero status if any required step fails.

