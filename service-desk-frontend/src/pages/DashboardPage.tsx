import { Link } from 'react-router-dom';
import { DeadlineChip, PriorityBadge } from '../components/Badges';
import { isActive, isOverdue } from '../format';
import { useTickets } from '../useTickets';

export default function DashboardPage() {
  const { tickets, loading, error } = useTickets();

  if (loading) return <p className="empty">Loading…</p>;
  if (error) return <p className="error">{error}</p>;

  // Count the tickets in each group.
  const open = tickets.filter((t) => t.status === 'OPEN').length;
  const inProgress = tickets.filter((t) => t.status === 'IN_PROGRESS').length;
  const overdue = tickets.filter(isOverdue).length;
  const finished = tickets.filter((t) => !isActive(t)).length;

  // The five active tickets with the nearest deadline.
  const urgent = tickets
    .filter(isActive)
    .sort((a, b) => new Date(a.dueAt).getTime() - new Date(b.dueAt).getTime())
    .slice(0, 5);

  return (
    <>
      <h1>Overview</h1>

      <section className="kpis">
        <div className="kpi"><span>Open</span><strong>{open}</strong></div>
        <div className="kpi"><span>In progress</span><strong>{inProgress}</strong></div>
        <div className={overdue > 0 ? 'kpi danger' : 'kpi'}><span>Overdue</span><strong>{overdue}</strong></div>
        <div className="kpi good"><span>Resolved or closed</span><strong>{finished}</strong></div>
      </section>

      <section className="card">
        <h2>Needs attention</h2>
        {urgent.length === 0 ? (
          <p className="muted">No open tickets.</p>
        ) : (
          <ul className="list">
            {urgent.map((t) => (
              <li key={t.id}>
                <Link to={'/tickets/' + t.id} className="list-title">
                  <span className="ref">{t.reference}</span> {t.title}
                </Link>
                <PriorityBadge priority={t.priority} />
                <DeadlineChip ticket={t} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
