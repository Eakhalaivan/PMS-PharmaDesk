import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../utils/api';

const LookupContext = createContext(null);

export const LookupProvider = ({ children }) => {
  const [lookups, setLookups] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBulkLookups = async () => {
      try {
        const response = await api.get('/lookups/bulk');
        setLookups(response.data);
      } catch (error) {
        console.error('Failed to fetch lookups', error);
      } finally {
        setLoading(false);
      }
    };
    fetchBulkLookups();
  }, []);

  return (
    <LookupContext.Provider value={{ lookups, loading }}>
      {children}
    </LookupContext.Provider>
  );
};

export const useLookup = (lookupType) => {
  const context = useContext(LookupContext);
  if (!context) {
    throw new Error('useLookup must be used within a LookupProvider');
  }
  return context.lookups[lookupType] || [];
};
