import { useQuery } from '@tanstack/react-query';
import { fetchWithRetry } from '../utils/api';
import { useSearchParams } from 'react-router-dom';
import { useRef, useEffect } from 'react';

/**
 * Reusable hook for paginated data fetching
 * @param {string} queryKey - Unique key for the query
 * @param {string} endpoint - API endpoint to fetch from
 * @param {Object} extraParams - Additional query parameters
 */
export const usePageData = (queryKey, endpoint, extraParams = {}) => {
  const [searchParams, setSearchParams] = useSearchParams();
  
  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = parseInt(searchParams.get('size') || '20', 10);

  const query = useQuery({
    queryKey: [queryKey, page, size, extraParams],
    queryFn: () => fetchWithRetry(endpoint, {
      params: { page, size, ...extraParams }
    }).then(res => res.data.data || res.data),
    staleTime: 5000,
  });

  // Cache the last successful response in a ref so stale data is shown while fresh data loads
  const lastDataRef = useRef(null);
  useEffect(() => {
    if (query.data) {
      lastDataRef.current = query.data;
    }
  }, [query.data]);

  const activeData = query.data || lastDataRef.current;

  const goToPage = (newPage) => {
    setSearchParams(prev => {
      prev.set('page', newPage);
      return prev;
    });
  };

  return {
    ...query,
    isLoading: query.isPending, // true only on initial load when no data
    isFetching: query.isFetching, // true whenever a request is in flight
    isError: query.isError,
    page,
    size,
    goToPage,
    totalPages: activeData?.totalPages || 0,
    totalElements: activeData?.totalElements || 0,
    items: activeData?.content || activeData || [], // initialized to []
  };
};
