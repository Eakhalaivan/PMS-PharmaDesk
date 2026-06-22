import React, { useState, useEffect } from 'react';
import { Bell, Search, Menu } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { ROLES } from '../../config/roles.config';
import pharmacyService from '../../utils/pharmacyService';

export default function TopNav() {
  const { activeRole } = useAuth();
  const [notificationCount, setNotificationCount] = useState(0);

  useEffect(() => {
    const fetchCounts = async () => {
      try {
        let count = 0;
        
        // 1. Fetch low stock count
        const lowStockRes = await pharmacyService.api.get('/pharmacy/stocks/low-stock');
        count += lowStockRes.data?.totalElements || lowStockRes.data?.length || 0;

        // 2. Fetch pending POs if admin
        if (activeRole === ROLES.SYSTEM_ADMIN) {
          const poRes = await pharmacyService.api.get('/pharmacy/purchase-orders', { params: { status: 'PENDING' } });
          count += poRes.data?.totalElements || poRes.data?.length || 0;
        }
        
        setNotificationCount(count);
      } catch (error) {
        console.warn('Failed to fetch notification counts');
      }
    };

    fetchCounts();
    const interval = setInterval(fetchCounts, 60000); // refresh every minute
    return () => clearInterval(interval);
  }, [activeRole]);

  return (
    <header className="h-20 bg-white border-b border-gray-200 flex items-center justify-between px-8 sticky top-0 z-10">
      <div className="flex items-center gap-4">
        <button className="lg:hidden p-2 hover:bg-gray-100 rounded-lg text-gray-500">
          <Menu className="w-5 h-5" />
        </button>
        <div className="relative hidden md:block">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input 
            type="text" 
            placeholder="Search patient UHID, medicine, bills..." 
            className="pl-10 pr-4 py-2 w-[400px] bg-gray-50 border border-gray-200 rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all"
          />
        </div>
      </div>

      <div className="flex items-center gap-6">
        <button className="relative p-2 text-gray-500 hover:text-primary transition-colors">
          <Bell className="w-5 h-5" />
          {notificationCount > 0 && (
            <span className="absolute top-0 right-0 min-w-4 min-h-4 px-1 bg-red-500 rounded-full text-[10px] font-bold text-white flex items-center justify-center border-2 border-white translate-x-1/4 -translate-y-1/4">
              {notificationCount > 99 ? '99+' : notificationCount}
            </span>
          )}
        </button>
        
        <div className="flex items-center gap-3 pl-6 border-l border-gray-200">
          <div className="text-right hidden sm:block">
            <p className="text-sm font-semibold text-gray-900">Hospital General</p>
            <p className="text-xs text-gray-500">Main Branch</p>
          </div>
          <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold">
            HG
          </div>
        </div>
      </div>
    </header>
  );
}
