# Service Desk

A ticketing app: users raise tickets, the support team assigns, replies and resolves them.

- `service-desk-backend`: Spring Boot REST API with an embedded H2 database
- `service-desk-frontend`: React and TypeScript front end

## Screenshots

![Dashboard](screenshots/1-dashboard.png)
![Tickets](screenshots/2-tickets.png)

## Run it

1. Open `service-desk-backend` in IntelliJ and run `ServiceDeskApplication` (port 8080).
2. In a second terminal:

   cd service-desk-frontend
   npm install
   npm run dev

3. Open http://localhost:5173