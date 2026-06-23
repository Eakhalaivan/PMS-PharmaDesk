import React, { createContext, useContext, useState, useEffect } from 'react';

const SystemContext = createContext(null);

export const SystemProvider = ({ children }) => {
  const [systemData, setSystemData] = useState({
    current_date: '',
    current_time: '',
    current_datetime: '',
    day_of_week: '',
    greeting: 'Welcome',
    branch_name: 'Loading...',
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSystemData = async () => {
      try {
        const response = await fetch('/api/system/current-datetime');
        if (response.ok) {
          const data = await response.json();
          setSystemData(data);
        }
      } catch (error) {
        console.error('Failed to fetch system datetime', error);
      } finally {
        setLoading(false);
      }
    };
    fetchSystemData();
  }, []);

  return (
    <SystemContext.Provider value={{ systemData, loading }}>
      {children}
    </SystemContext.Provider>
  );
};

export const useSystem = () => {
  const context = useContext(SystemContext);
  if (!context) {
    throw new Error('useSystem must be used within a SystemProvider');
  }
  return context;
};
