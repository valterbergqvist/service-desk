import { useState } from 'react';
import { Link } from 'react-router-dom';
import { DeadlineChip, PriorityBadge, StatusBadge } from '../components/Badges';
import { useTickets } from '../useTickets';
import type { Status } from '../types';
import { STATUSES, STATUS_LABEL } from '../types';

export default function TicketsPage() {
  const { tickets, loading, error } = useTickets();

  // The two filters. When one changes, React draws the page again.
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState<Status | ''>(''); // '' means "all"

  if (loading) return <p className="empty">Loading…</p>;
  if (error) return <p className="error">{error}</p>;

  // Keep only the tickets that match both filters.
  const text = search.trim().toLowerCase();
  const shown = tickets.filter(
    (t) =>
      (status === '' || t.status === status) &&
      (text === '' ||
        t.title.toLowerCase().includes(text) ||
        t.requesterName.toLowerCase().includes(text)),
  );

  return (
    <>
      <h1>Tickets</h1>

      <div className="chips">
        <button className={status === '' ? 'chip active' : 'chip'} onClick={() => setStatus('')}>
          All
        </button>
        {STATUSES.map((s) => (
          <button key={s} className={status === s ? 'chip active' : 'chip'} onClick={() => setStatus(s)}>
            {STATUS_LABEL[s]}
          </button>
        ))}
      </div>

      <input
        className="search"
        type="search"
        placeholder="Search by title or requester"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      {shown.length === 0 ? (
        <p className="empty">No tickets found.</p>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Ticket</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Deadline</th>
                <th>Assignee</th>
              </tr>
            </thead>
            <tbody>
              {shown.map((t) => (
                <tr key={t.id}>
                  <td>
                    <Link to={'/tickets/' + t.id} className="ticket-link">
                      <span className="ref">{t.reference}</span> {t.title}
                    </Link>
                    <span className="sub">{t.requesterName}</span>
                  </td>
                  <td><PriorityBadge priority={t.priority} /></td>
                  <td><StatusBadge status={t.status} /></td>
                  <td><DeadlineChip ticket={t} /></td>
                  <td>{t.assignee ?? <span className="muted">Unassigned</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
