import { deadline } from '../format';
import type { Priority, Status, Ticket } from '../types';
import { PRIORITY_LABEL, STATUS_LABEL } from '../types';

// Small coloured labels. The CSS class is built from the value,
// for example "status-OPEN", and styles.css decides the colour.

export function StatusBadge({ status }: { status: Status }) {
  return <span className={'pill status-' + status}>{STATUS_LABEL[status]}</span>;
}

export function PriorityBadge({ priority }: { priority: Priority }) {
  return (
    <span className={'prio prio-' + priority}>
      <i />
      {PRIORITY_LABEL[priority]}
    </span>
  );
}

export function DeadlineChip({ ticket }: { ticket: Ticket }) {
  const d = deadline(ticket);
  return <span className={'sla sla-' + d.tone}>{d.text}</span>;
}
