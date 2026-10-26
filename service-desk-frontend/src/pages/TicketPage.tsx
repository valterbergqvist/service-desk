import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import { DeadlineChip, PriorityBadge, StatusBadge } from '../components/Badges';
import { formatDate } from '../format';
import type { Agent, Priority, Status, Ticket, TicketChange } from '../types';
import { PRIORITIES, PRIORITY_LABEL, STATUSES, STATUS_LABEL } from '../types';

export default function TicketPage() {
  const { id } = useParams(); // the number from the URL /tickets/:id
  const ticketId = Number(id);
  const navigate = useNavigate();

  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [agents, setAgents] = useState<Agent[]>([]);
  const [error, setError] = useState('');
  const [author, setAuthor] = useState(''); // who writes the reply
  const [message, setMessage] = useState(''); // the reply text

  // Load the ticket and the list of agents when the page opens.
  useEffect(() => {
    api
      .getTicket(ticketId)
      .then((t) => {
        setTicket(t);
        setAuthor(t.assignee ?? t.requesterName);
      })
      .catch((e: Error) => setError(e.message));
    api.listAgents().then(setAgents).catch(() => undefined);
  }, [ticketId]);

  // Send a change to the backend and show the updated ticket.
  async function update(change: TicketChange) {
    if (!ticket) return;
    try {
      setTicket(await api.updateTicket(ticket.id, change));
      setError('');
    } catch (err) {
      setError((err as Error).message);
    }
  }

  async function sendReply(e: FormEvent) {
    e.preventDefault();
    if (!ticket) return;
    try {
      await api.addComment(ticket.id, { author, message });
      setMessage('');
      setTicket(await api.getTicket(ticket.id)); // reload to show the new reply
    } catch (err) {
      setError((err as Error).message);
    }
  }

  async function remove() {
    if (!ticket || !window.confirm('Delete this ticket?')) return;
    try {
      await api.deleteTicket(ticket.id);
      navigate('/tickets');
    } catch (err) {
      setError((err as Error).message);
    }
  }

  if (!ticket) return error ? <p className="error">{error}</p> : <p className="empty">Loading…</p>;

  // People who can write a reply: the agents and the requester.
  const authors = [...agents.map((a) => a.name), ticket.requesterName];

  return (
    <>
      <p className="crumbs"><Link to="/tickets">Tickets</Link> / {ticket.reference}</p>

      <h1>{ticket.title}</h1>
      <div className="head-meta">
        <StatusBadge status={ticket.status} />
        <PriorityBadge priority={ticket.priority} />
        <DeadlineChip ticket={ticket} />
      </div>

      {error && <p className="error">{error}</p>}

      <div className="detail">
        <div className="detail-main">
          <section className="card">
            <p className="muted small">
              Opened by {ticket.requesterName} ({ticket.requesterEmail}) on {formatDate(ticket.createdAt)}
            </p>
            <p className="description">{ticket.description}</p>
          </section>

          <section className="card">
            <h2>Replies</h2>
            {ticket.comments.length === 0 && <p className="muted">No replies yet.</p>}
            {ticket.comments.map((c) => (
              <div key={c.id} className="comment">
                <p><strong>{c.author}</strong> <span className="muted small">{formatDate(c.createdAt)}</span></p>
                <p className="description">{c.message}</p>
              </div>
            ))}

            <form className="form reply" onSubmit={sendReply}>
              <textarea
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                rows={3}
                placeholder="Write a reply"
                required
              />
              <div className="reply-bar">
                <label className="inline">
                  Reply as
                  <select value={author} onChange={(e) => setAuthor(e.target.value)}>
                    {authors.map((name) => (
                      <option key={name} value={name}>{name}</option>
                    ))}
                  </select>
                </label>
                <button className="btn primary" type="submit">Send reply</button>
              </div>
            </form>
          </section>
        </div>

        <aside className="card form">
          <label>
            Status
            <select value={ticket.status} onChange={(e) => update({ status: e.target.value as Status })}>
              {STATUSES.map((s) => (
                <option key={s} value={s}>{STATUS_LABEL[s]}</option>
              ))}
            </select>
          </label>

          <label>
            Priority
            <select value={ticket.priority} onChange={(e) => update({ priority: e.target.value as Priority })}>
              {PRIORITIES.map((p) => (
                <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>
              ))}
            </select>
          </label>

          <label>
            Assigned to
            <select value={ticket.assignee ?? ''} onChange={(e) => update({ assignee: e.target.value })}>
              <option value="">Unassigned</option>
              {agents.map((a) => (
                <option key={a.id} value={a.name}>{a.name}</option>
              ))}
            </select>
          </label>

          <p className="muted small">Deadline: {formatDate(ticket.dueAt)}</p>

          <button className="btn danger" onClick={remove}>Delete ticket</button>
        </aside>
      </div>
    </>
  );
}
