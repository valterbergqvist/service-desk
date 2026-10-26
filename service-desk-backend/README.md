# Service Desk Backend

A Spring Boot REST API for a service desk: people raise tickets, the support team assigns, replies to and resolves them.

- Java 17, Spring Boot 3, Spring Data JPA, Bean Validation
- Embedded in-memory H2 database, nothing to install (data resets when the backend restarts)
- No Docker, no external services, no authentication

The React front end for this API is in the `service-desk-frontend` folder.

## Run it

You need Java 17 or newer.

**IntelliJ:** open this folder (the one with `pom.xml`) and run `ServiceDeskApplication`.

**Command line:** with Maven installed, run `mvn spring-boot:run`.

The API starts on http://localhost:8080 with two sample agents and four sample tickets. Run the tests with `mvn test`, or right-click `TicketApiTest` in IntelliJ.

## What it does

- **Tickets** with a reference like `SD-4`, a status (OPEN, IN_PROGRESS, RESOLVED, CLOSED), a priority and a category
- **Agents** in their own table. Assigning a ticket by name finds the agent or creates a new one.
- **SLA deadlines:** every ticket gets a `dueAt` from its priority (urgent 4 h, high 8 h, medium 24 h, low 72 h). `overdue` is true when an open or in-progress ticket is past it. Changing the priority recalculates the deadline.
- **Resolved time:** `resolvedAt` is set when a ticket is resolved or closed and cleared when it is reopened
- **Activity log:** every create, status change, priority change, assignment and reply is recorded per ticket

## Structure

```
com.servicedesk
  ticket/   Ticket, Comment, TicketEvent, repository, specifications, service, controller, DTOs
  agent/    Agent, repository, service, controller, DTOs
  common/   exceptions and the JSON error handler
  config/   CORS and demo data
```

`Ticket` owns its rules (`changeStatus`, `changePriority`, `assignTo`, `addComment`), so the SLA deadline, resolved time and activity log cannot get out of sync. List filtering is built with JPA Specifications.

## API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/tickets?status=OPEN&priority=HIGH&q=vpn` | List tickets, newest first. All filters are optional, and `q` searches the title and requester name. |
| GET | `/api/tickets/stats` | Totals per status and the number of overdue tickets |
| GET | `/api/tickets/{id}` | One ticket with its comments |
| GET | `/api/tickets/{id}/activity` | The ticket's activity log |
| POST | `/api/tickets` | Create a ticket |
| PATCH | `/api/tickets/{id}` | Change status, priority or assignee. Send only what changes; an empty assignee unassigns. |
| DELETE | `/api/tickets/{id}` | Delete a ticket |
| POST | `/api/tickets/{id}/comments` | Add a comment |
| GET | `/api/agents` | Agents with their number of open tickets |
| POST | `/api/agents` | Add an agent |

Errors are JSON: `{ "status": 400, "message": "Validation failed", "fieldErrors": { "title": "must not be blank" } }`.

Example (Windows Command Prompt, one line):

```
curl -X POST http://localhost:8080/api/tickets -H "Content-Type: application/json" -d "{\"title\":\"VPN down\",\"description\":\"Cannot connect\",\"requesterName\":\"Sam\",\"requesterEmail\":\"sam@example.com\",\"priority\":\"HIGH\",\"category\":\"NETWORK\"}"
```

## Tests

`TicketApiTest` starts the app with a test client and checks ticket creation and validation, status, priority and assignee changes, resolved time, the activity log, comments, deleting, agents and stats.

## Configuration

`src/main/resources/application.properties`

- Use `jdbc:h2:file:./data/servicedesk` as the datasource URL to keep data between restarts
- Set `app.seed-demo=false` to skip the sample data
- `app.cors.allowed-origin` is the front end origin (default `http://localhost:5173`)
