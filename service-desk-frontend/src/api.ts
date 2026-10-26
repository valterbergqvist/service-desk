import type { Agent, NewTicket, Ticket, TicketChange } from './types';

// One small function sends every request to the backend.
// "/api/..." is forwarded to Spring Boot by the proxy in vite.config.ts.
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch('/api' + path, {
      ...options,
      headers: { 'Content-Type': 'application/json' },
    });
  } catch {
    throw new Error('Cannot reach the server. Is the backend running on port 8080?');
  }

  // 204 means "success, nothing to return" (used when deleting).
  if (response.status === 204) return undefined as T;

  // The body is JSON, but an error can come back empty, so parse carefully.
  let data: any = null;
  try {
    data = await response.json();
  } catch {
    data = null;
  }

  if (!response.ok) {
    // Show the backend's message if it sent one.
    throw new Error(data?.message ?? 'Something went wrong. Is the backend running?');
  }
  return data as T;
}

// One function per backend endpoint. The pages only ever call these.
export const api = {
  listTickets: () => request<Ticket[]>('/tickets'),

  getTicket: (id: number) => request<Ticket>('/tickets/' + id),

  createTicket: (ticket: NewTicket) =>
    request<Ticket>('/tickets', { method: 'POST', body: JSON.stringify(ticket) }),

  updateTicket: (id: number, change: TicketChange) =>
    request<Ticket>('/tickets/' + id, { method: 'PATCH', body: JSON.stringify(change) }),

  deleteTicket: (id: number) => request<void>('/tickets/' + id, { method: 'DELETE' }),

  addComment: (id: number, comment: { author: string; message: string }) =>
    request<unknown>('/tickets/' + id + '/comments', {
      method: 'POST',
      body: JSON.stringify(comment),
    }),

  listAgents: () => request<Agent[]>('/agents'),
};
