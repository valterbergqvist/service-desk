import { useEffect, useState } from 'react';
import { api } from './api';
import type { Ticket } from './types';

// A custom hook: loads all tickets once, so two pages can share this code.
export function useTickets() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // useEffect with [] runs one time, when the page first appears.
  useEffect(() => {
    api
      .listTickets()
      .then(setTickets)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return { tickets, loading, error };
}
