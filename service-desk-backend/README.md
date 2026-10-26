# Service Desk Backend

A Spring Boot REST API for a service desk: people raise tickets, the support team triages, replies and resolves them.

- Java 17, Spring Boot 3, Spring Data JPA, Bean Validation
- Embedded in-memory H2 database, nothing to install
- No Docker, no external services, no security layer

## Run it

You need Java 17+ and Maven (IntelliJ bundles it).

```bash
mvn spring-boot:run
```

The API starts on http://localhost:8080 with a few sample agents and tickets. Run the tests with `mvn test`.

It keeps the same `/api/tickets` contract as the earlier Service Desk API, so the React `servicedesk-ui` works against it unchanged (`npm run dev` on port 5173 proxies `/api` to port 8080).

## What it does

- **Tickets** with status (OPEN, IN_PROGRESS, RESOLVED, CLOSED), priority and category
- **Agents** as their own table; assigning a ticket by name finds or creates the agent
- **SLA deadlines**: every ticket gets a `dueAt` from its priority (urgent 4 h, high 8 h, medium 24 h, low 72 h). `overdue` is true when an open or in-progress ticket is past it. Changing the priority recalculates the deadline.
- **Resolved time**: `resolvedAt` is set when a ticket is resolved or closed and cleared when it is reopened
- **Activity log**: every create, status change, priority change, assignment and reply is recorded per ticket

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
| GET | `/api/tickets?status=OPEN&priority=HIGH&q=vpn` | List tickets, newest first (all filters optional) |
| GET | `/api/tickets/stats` | Totals per status and the number of overdue tickets |
| GET | `/api/tickets/{id}` | One ticket with its comments |
| GET | `/api/tickets/{id}/activity` | The ticket's activity log |
| POST | `/api/tickets` | Create a ticket |
| PATCH | `/api/tickets/{id}` | Change status, priority or assignee (send only what changes, empty assignee unassigns) |
| DELETE | `/api/tickets/{id}` | Delete a ticket |
| POST | `/api/tickets/{id}/comments` | Add a comment |
| GET | `/api/agents` | Agents with their number of open tickets |
| POST | `/api/agents` | Add an agent |

Errors are JSON: `{ "status": 400, "message": "Validation failed", "fieldErrors": { "title": "must not be blank" } }`.

Example:

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"title":"VPN down","description":"Cannot connect","requesterName":"Sam","requesterEmail":"sam@example.com","priority":"HIGH","category":"NETWORK"}'
```

## Configuration

`src/main/resources/application.properties`

- Use `jdbc:h2:file:./data/servicedesk` as the datasource URL to keep data between restarts
- Set `app.seed-demo=false` to skip the sample data
- `app.cors.allowed-origin` is the front end origin (default `http://localhost:5173`)
