// The allowed values. TypeScript will complain if we use any other text.
export type Status = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type Category = 'HARDWARE' | 'SOFTWARE' | 'NETWORK' | 'ACCESS' | 'OTHER';

// Lists we loop over to build dropdowns.
export const STATUSES: Status[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
export const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
export const CATEGORIES: Category[] = ['HARDWARE', 'SOFTWARE', 'NETWORK', 'ACCESS', 'OTHER'];

// Text shown to the user for each value.
export const STATUS_LABEL: Record<Status, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In progress',
  RESOLVED: 'Resolved',
  CLOSED: 'Closed',
};
export const PRIORITY_LABEL: Record<Priority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  URGENT: 'Urgent',
};
export const CATEGORY_LABEL: Record<Category, string> = {
  HARDWARE: 'Hardware',
  SOFTWARE: 'Software',
  NETWORK: 'Network',
  ACCESS: 'Access and accounts',
  OTHER: 'Something else',
};

// These interfaces describe the JSON that the backend sends.
export interface Comment {
  id: number;
  author: string;
  message: string;
  createdAt: string;
}

export interface Ticket {
  id: number;
  reference: string; // for example "SD-4"
  title: string;
  description: string;
  requesterName: string;
  requesterEmail: string;
  status: Status;
  priority: Priority;
  category: Category;
  assignee: string | null; // null means nobody is assigned
  createdAt: string;
  updatedAt: string;
  dueAt: string; // the SLA deadline
  comments: Comment[];
}

export interface Agent {
  id: number;
  name: string;
}

// What we send when creating or changing a ticket.
export interface NewTicket {
  title: string;
  description: string;
  requesterName: string;
  requesterEmail: string;
  priority: Priority;
  category: Category;
}

export interface TicketChange {
  status?: Status;
  priority?: Priority;
  assignee?: string; // an empty string means "unassign"
}
