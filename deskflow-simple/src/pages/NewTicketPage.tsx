import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { Category, Priority } from '../types';
import { CATEGORIES, CATEGORY_LABEL, PRIORITIES, PRIORITY_LABEL } from '../types';

export default function NewTicketPage() {
  const navigate = useNavigate(); // lets us change page from code

  // One piece of state for every field in the form.
  const [requesterName, setRequesterName] = useState('');
  const [requesterEmail, setRequesterEmail] = useState('');
  const [category, setCategory] = useState<Category>('SOFTWARE');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');

  async function submit(e: FormEvent) {
    e.preventDefault(); // stop the browser from reloading the page
    setError('');
    try {
      const ticket = await api.createTicket({
        title,
        description,
        requesterName,
        requesterEmail,
        priority,
        category,
      });
      navigate('/tickets/' + ticket.id); // open the new ticket
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <>
      <h1>New ticket</h1>

      <form className="card form" onSubmit={submit}>
        <div className="row">
          <label>
            Requester name
            <input value={requesterName} onChange={(e) => setRequesterName(e.target.value)} required />
          </label>
          <label>
            Requester email
            <input type="email" value={requesterEmail} onChange={(e) => setRequesterEmail(e.target.value)} required />
          </label>
        </div>

        <div className="row">
          <label>
            Category
            <select value={category} onChange={(e) => setCategory(e.target.value as Category)}>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>{CATEGORY_LABEL[c]}</option>
              ))}
            </select>
          </label>
          <label>
            Priority
            <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
              {PRIORITIES.map((p) => (
                <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>
              ))}
            </select>
          </label>
        </div>

        <label>
          Summary
          <input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={150} />
        </label>

        <label>
          Details
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={6} required />
        </label>

        {error && <p className="error">{error}</p>}

        <button className="btn primary" type="submit">Create ticket</button>
      </form>
    </>
  );
}
