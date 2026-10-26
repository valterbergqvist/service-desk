import type { Ticket } from './types';

// A ticket needs work while it is open or in progress.
export const isActive = (t: Ticket) => t.status === 'OPEN' || t.status === 'IN_PROGRESS';

// Overdue = still active and the deadline has passed.
export const isOverdue = (t: Ticket) => isActive(t) && new Date(t.dueAt).getTime() < Date.now();

// "23 Oct, 14:05" in the user's own language and format.
export function formatDate(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });
}

// Turns milliseconds into "45 min" or "3 h".
function span(ms: number): string {
  const minutes = Math.round(Math.abs(ms) / 60000);
  return minutes < 60 ? `${minutes} min` : `${Math.round(minutes / 60)} h`;
}

// The text and colour for the deadline chip.
export function deadline(t: Ticket): { text: string; tone: 'ok' | 'soon' | 'overdue' | 'done' } {
  if (!isActive(t)) return { text: 'Finished', tone: 'done' };
  const left = new Date(t.dueAt).getTime() - Date.now();
  if (left < 0) return { text: 'Overdue by ' + span(left), tone: 'overdue' };
  if (left < 2 * 3600 * 1000) return { text: 'Due in ' + span(left), tone: 'soon' };
  return { text: 'Due in ' + span(left), tone: 'ok' };
}
