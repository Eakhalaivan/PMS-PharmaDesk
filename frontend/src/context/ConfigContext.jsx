import { useQuery } from '@tanstack/react-query';
import api from '../utils/api';

export const useConfig = (key) => {
  const { data: config = {}, isLoading: loading } = useQuery({
    queryKey: ['config'],
    queryFn: async () => {
      const response = await api.get('/config/public');
      return response.data;
    },
    staleTime: 1000 * 60 * 60, // 1 hour
  });

  return key ? config[key] : config;
};
