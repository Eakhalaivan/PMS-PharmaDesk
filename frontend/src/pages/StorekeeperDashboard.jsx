import React from 'react';
import { RotateCcw, AlertTriangle, Clock, ArrowRight, Package, Box, RefreshCw } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import KPICard from '../components/ui/KPICard';
import DataTable from '../components/ui/DataTable';
import { useSystem } from '../context/SystemContext';
import { useConfig } from '../context/ConfigContext';
import api from '../utils/api';

const DashboardSkeleton = () => (
  <div className="space-y-8 animate-pulse">
    <div className="h-8 w-64 bg-slate-200 rounded"></div>
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      {[...Array(3)].map((_, i) => <div key={i} className="h-32 bg-slate-200 rounded-2xl"></div>)}
    </div>
    <div className="grid grid-cols-1 gap-8">
      <div className="h-[400px] bg-slate-200 rounded-2xl"></div>
    </div>
  </div>
);

export default function StorekeeperDashboard() {
  const navigate = useNavigate();
  const { systemData } = useSystem();
  const refreshIntervalSeconds = useConfig('dashboard_refresh_interval_seconds');
  const refreshInterval = (refreshIntervalSeconds ? Number(refreshIntervalSeconds) * 1000 : 60000);

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-kpis'],
    queryFn: () => api.get('/dashboard/kpis').then(r => r.data),
    refetchInterval: refreshInterval,
  });

  const { data: lowStockData, isLoading: lowStockLoading } = useQuery({
    queryKey: ['dashboard-low-stock'],
    queryFn: () => api.get('/pharmacy/stocks/low-stock').then(r => r.data?.data || []),
    refetchInterval: refreshInterval,
  });

  const isLoading = statsLoading || lowStockLoading;

  if (isLoading) return <DashboardSkeleton />;

  const lowStockMedicines = lowStockData || [];
  const fmt = (val) => val != null ? Number(val).toLocaleString('en-IN') : '0';

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">
          {systemData?.greeting || 'Welcome'}, here's your Inventory Dashboard
        </h2>
        <p className="text-sm text-gray-500 font-medium">
          Stock alerts and supply chain overview as of {new Date().toLocaleString('en-IN')}
        </p>
      </div>

      <div className="flex gap-4 overflow-x-auto pb-2">
        <button onClick={() => navigate('/stocks')} className="flex items-center gap-2 bg-primary text-white px-4 py-2 rounded-lg shadow-sm hover:bg-primary-dark transition-colors whitespace-nowrap">
          <Box className="w-4 h-4" /> Manage Stock
        </button>
        <button onClick={() => navigate('/purchase-orders')} className="flex items-center gap-2 bg-amber-500 text-white px-4 py-2 rounded-lg shadow-sm hover:bg-amber-600 transition-colors whitespace-nowrap">
          <Package className="w-4 h-4" /> Purchase Orders
        </button>
        <button onClick={() => navigate('/pending-indents')} className="flex items-center gap-2 bg-rose-500 text-white px-4 py-2 rounded-lg shadow-sm hover:bg-rose-600 transition-colors whitespace-nowrap">
          <RefreshCw className="w-4 h-4" /> Pending Indents
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <KPICard
          title="Total SKUs In Stock"
          value={`${fmt(stats?.total_skus_in_stock)}`}
          icon={RotateCcw}
          className="border-l-4 border-l-info"
        />
        <KPICard
          title="Low Stock Alerts"
          value={stats?.low_stock_alerts_count?.toString() || '0'}
          icon={AlertTriangle}
          className="border-l-4 border-l-warning"
        />
        <KPICard
          title="Expiring Soon (30 days)"
          value={stats?.expiring_in_30_days_count?.toString() || '0'}
          icon={Clock}
          className="border-l-4 border-l-rose-500"
        />
      </div>

      <div className="grid grid-cols-1 gap-8 pb-8">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-lg text-gray-800 flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-amber-500" /> Critical Low Stock
            </h3>
            <button onClick={() => navigate('/low-stock-alerts')} className="text-primary hover:text-primary-dark text-sm font-medium flex items-center gap-1">
              View All <ArrowRight className="w-4 h-4" />
            </button>
          </div>
          <div className="flex-1 overflow-auto">
            <DataTable
              data={lowStockMedicines}
              columns={[
                { header: 'Medicine Name', render: (row) => row.medicineName || row.name },
                { header: 'Current Stock', render: (row) => (
                  <span className="font-bold text-red-600">{row.currentStock}</span>
                )},
                { header: 'Reorder Level', accessor: 'reorderLevel' },
                { header: 'Unit', accessor: 'unit' },
                { header: 'Action', render: (row) => (
                  <button 
                    onClick={() => navigate('/purchase-orders')}
                    className="text-[10px] font-bold uppercase tracking-wider bg-indigo-900 text-white px-3 py-1.5 rounded-lg hover:bg-indigo-800 transition-all">
                    Reorder
                  </button>
                )}
              ]}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
