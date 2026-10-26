import { Link, NavLink, Route, Routes } from 'react-router-dom';
import DashboardPage from './pages/DashboardPage';
import NewTicketPage from './pages/NewTicketPage';
import TicketPage from './pages/TicketPage';
import TicketsPage from './pages/TicketsPage';

// NavLink tells us if it points to the current page, so we can highlight it.
const navClass = ({ isActive }: { isActive: boolean }) => (isActive ? 'nav active' : 'nav');

export default function App() {
  return (
    <div className="shell">
      {/* The sidebar is always visible */}
      <aside className="sidebar">
        <div className="brand">
          <span className="logo" />
          Deskflow
        </div>
        <Link to="/tickets/new" className="new-btn">+ New ticket</Link>
        <nav>
          <NavLink to="/" end className={navClass}>Dashboard</NavLink>
          <NavLink to="/tickets" className={navClass}>Tickets</NavLink>
        </nav>
      </aside>

      {/* Only this part changes when the URL changes */}
      <main className="content">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/tickets" element={<TicketsPage />} />
          <Route path="/tickets/new" element={<NewTicketPage />} />
          <Route path="/tickets/:id" element={<TicketPage />} />
          <Route path="*" element={<p className="empty">Page not found.</p>} />
        </Routes>
      </main>
    </div>
  );
}
