import { useQuery } from '@tanstack/react-query';
import api from '../utils/api';

export const useLookup = (lookupType) => {
  const { data: lookups = {}, isLoading: loading } = useQuery({
    queryKey: ['lookups'],
    queryFn: async () => {
      const response = await api.get('/lookups/bulk');
      return response.data;
    },
    staleTime: Infinity,
  });

  return lookupType ? (lookups[lookupType] || []) : lookups;
};
