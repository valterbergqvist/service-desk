# Deskflow (simple version)

A small React front end for the service desk backend (`service-desk-backend`).
It uses the same API as the larger version, with much less code.

- React 18, TypeScript, Vite, React Router
- Four pages: Dashboard, Tickets, New ticket, Ticket
- No state library, no popups, no polling: each page loads its own data

## Run it

Start the backend first (port 8080), then:

```bash
npm install
npm run dev
```

Open http://localhost:5173.

## Where to start reading

1. `index.html` and `src/main.tsx` start the app
2. `src/App.tsx` has the sidebar and the page routes
3. `src/api.ts` is the only file that talks to the backend
4. `src/pages/` has one file per page
5. `src/styles.css` holds all colours and layout
