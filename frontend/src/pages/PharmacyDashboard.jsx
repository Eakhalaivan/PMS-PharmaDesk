import React, { useState } from 'react';
import { IndianRupee, RotateCcw, Stethoscope, AlertTriangle, Eye, Printer } from 'lucide-react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import { useQuery } from '@tanstack/react-query';
import KPICard from '../components/ui/KPICard';
import DataTable from '../components/ui/DataTable';
import AppModal from '../components/ui/AppModal';
import Badge from '../components/ui/Badge';
import PharmacyInvoice from '../components/pharmacy/PharmacyInvoice';
import { useSystem } from '../context/SystemContext';
import { useConfig } from '../context/ConfigContext';
import api from '../utils/api';

const COLORS = ['#2563eb', '#16a34a', '#d97706', '#dc2626', '#1e3a8a'];

const DashboardSkeleton = () => (
  <div className="space-y-8 animate-pulse">
    <div className="h-8 w-64 bg-slate-200 rounded"></div>
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {[...Array(4)].map((_, i) => <div key={i} className="h-32 bg-slate-200 rounded-2xl"></div>)}
    </div>
    <div className="grid grid-cols-1 lg:grid-cols-10 gap-8">
      <div className="lg:col-span-6 h-[400px] bg-slate-200 rounded-2xl"></div>
      <div className="lg:col-span-4 h-[400px] bg-slate-200 rounded-2xl"></div>
    </div>
  </div>
);

export default function PharmacyDashboard() {
  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState(null);

  const { systemData } = useSystem();
  const refreshIntervalSeconds = useConfig('dashboard_refresh_interval_seconds');
  const refreshInterval = (refreshIntervalSeconds ? Number(refreshIntervalSeconds) * 1000 : 60000);
  const currencySymbol = useConfig('currency_symbol') || '₹';

  // KPIs — uses authenticated api instance (Bearer token attached automatically)
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-kpis'],
    queryFn: () => api.get('/dashboard/kpis').then(r => r.data),
    refetchInterval: refreshInterval,
    staleTime: 30000,
    retry: 2,
  });

  // Revenue trend
  const { data: trendRaw, isLoading: trendLoading } = useQuery({
    queryKey: ['dashboard-revenue-trend'],
    queryFn: () => api.get('/dashboard/revenue-trend?days=7').then(r => r.data),
    refetchInterval: refreshInterval,
    staleTime: 30000,
    retry: 2,
  });

  // Revenue summary (Today / Week / Month totals)
  const { data: revSummary, isLoading: revLoading } = useQuery({
    queryKey: ['dashboard-revenue-summary'],
    queryFn: () => api.get('/dashboard/revenue-summary').then(r => r.data),
    refetchInterval: refreshInterval,
    staleTime: 30000,
    retry: 2,
  });

  // Recent bills
  const { data: recentBillsData, isLoading: billsLoading } = useQuery({
    queryKey: ['dashboard-recent-bills'],
    queryFn: () => api.get('/pharmacy/dashboard/recent-activities').then(r => r.data?.data || []),
    refetchInterval: refreshInterval,
    staleTime: 30000,
    retry: 2,
  });

  // Low stock list
  const { data: lowStockData, isLoading: lowStockLoading } = useQuery({
    queryKey: ['dashboard-low-stock'],
    queryFn: () => api.get('/pharmacy/stocks/low-stock').then(r => r.data?.data || []),
    refetchInterval: refreshInterval,
    staleTime: 30000,
    retry: 2,
  });

  // Sales vs Returns chart data — build from trend + recentBills
  const chartSalesReturns = Array.isArray(trendRaw)
    ? trendRaw.map(d => ({
        day: d.day_of_week || d.day || d.sale_date,
        sales: Number(d.daily_revenue || 0),
        returns: 0, // returns trend endpoint not yet available — show 0
      }))
    : [];

  // Category breakdown — derive from low stock or fallback empty
  const categoryData = [];

  const isLoading = statsLoading || trendLoading || revLoading;

  if (isLoading) return <DashboardSkeleton />;

  const recentBills = recentBillsData || [];
  const lowStockMedicines = lowStockData || [];

  const fmt = (val) => val != null ? Number(val).toLocaleString('en-IN') : '0';

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">
          {systemData?.greeting || 'Welcome'}, here's your Pharmacy Dashboard
        </h2>
        <p className="text-sm text-gray-500 font-medium">
          Daily operations and financial overview as of {new Date().toLocaleString('en-IN')}
        </p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <KPICard
          title="Today's Sales"
          value={`${currencySymbol} ${fmt(stats?.todays_sales_revenue)}`}
          icon={IndianRupee}
          trend="up"
          className="border-l-4 border-l-success"
        />
        <KPICard
          title="Total SKUs In Stock"
          value={`${fmt(stats?.total_skus_in_stock)}`}
          icon={RotateCcw}
          className="border-l-4 border-l-info"
        />
        <KPICard
          title="Pending Prescriptions"
          value={`${fmt(stats?.pending_prescriptions_count)}`}
          icon={Stethoscope}
          className="border-l-4 border-l-primary"
        />
        <KPICard
          title="Low Stock Medicines"
          value={stats?.low_stock_alerts_count?.toString() || '0'}
          icon={AlertTriangle}
          className="border-l-4 border-l-warning"
        />
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-10 gap-8">
        <div className="lg:col-span-6 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 className="font-bold text-lg text-gray-800 mb-6">Sales Trend (Last 7 Days)</h3>
          <div className="h-[350px] w-full mt-4 min-w-0">
            <ResponsiveContainer width="100%" height={300} minWidth={100}>
              <AreaChart data={chartSalesReturns}>
                <defs>
                  <linearGradient id="colorSales" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#2563eb" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#2563eb" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="day" axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12}} tickFormatter={(val) => `₹${(val/1000).toFixed(0)}k`} />
                <Tooltip contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }} />
                <Area type="monotone" dataKey="sales" stroke="#2563eb" strokeWidth={3} fillOpacity={1} fill="url(#colorSales)" name="Sales" />
                <Legend iconType="circle" wrapperStyle={{ paddingTop: '20px' }} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="lg:col-span-4 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 className="font-bold text-lg text-gray-800 mb-4">Revenue Summary</h3>
          <div className="space-y-4 mt-6">
            {[
              { label: "Today", value: revSummary?.todays_total },
              { label: "This Week", value: revSummary?.this_weeks_total },
              { label: "This Month", value: revSummary?.this_months_total },
            ].map(({ label, value }) => (
              <div key={label} className="flex justify-between items-center py-3 border-b border-gray-100 last:border-0">
                <span className="text-sm font-medium text-gray-500">{label}</span>
                <span className="text-lg font-bold text-gray-900">{currencySymbol} {fmt(value)}</span>
              </div>
            ))}
          </div>
          <div className="mt-6 text-xs text-gray-400 text-center">Bills today: {fmt(stats?.bills_today)}</div>
        </div>
      </div>

      {/* Bottom Tables */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 pb-8">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-lg text-gray-800">Recent Bills</h3>
          </div>
          <DataTable
            data={recentBills}
            columns={[
              { header: 'Bill No', accessor: 'billNumber' },
              { header: 'Patient', accessor: 'patientName' },
              { header: 'Amount', render: (row) => `₹ ${fmt(row.netAmount)}` },
              { header: 'Status', render: (row) => (
                <Badge variant={row.status === 'PAID' ? 'success' : 'warning'}>{row.status}</Badge>
              )},
              { header: 'Action', render: (row) => (
                <div className="flex items-center gap-2">
                  <button onClick={() => { setSelectedInvoice(row); setIsInvoiceModalOpen(true); }}
                    className="p-1.5 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors">
                    <Eye className="w-4 h-4" />
                  </button>
                  <button onClick={() => { setSelectedInvoice(row); setIsInvoiceModalOpen(true); }}
                    className="p-1.5 bg-gray-50 text-gray-600 rounded-lg hover:bg-gray-100 transition-colors">
                    <Printer className="w-4 h-4" />
                  </button>
                </div>
              )}
            ]}
          />
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-lg text-gray-800">Low Stock Alerts</h3>
          </div>
          <DataTable
            data={lowStockMedicines}
            columns={[
              { header: 'Medicine Name', render: (row) => row.medicineName || row.name },
              { header: 'Current Stock', render: (row) => (
                <span className="font-bold text-red-600">{row.currentStock}</span>
              )},
              { header: 'Reorder Level', accessor: 'reorderLevel' },
              { header: 'Unit', accessor: 'unit' },
              { header: 'Action', render: () => (
                <button className="text-[10px] font-bold uppercase tracking-wider bg-indigo-900 text-white px-3 py-1.5 rounded-lg hover:bg-indigo-800 transition-all">
                  Reorder
                </button>
              )}
            ]}
          />
        </div>
      </div>

      <AppModal isOpen={isInvoiceModalOpen} onClose={() => setIsInvoiceModalOpen(false)} maxWidth="sm:max-w-4xl" padding={false}>
        <PharmacyInvoice bill={selectedInvoice} onClose={() => setIsInvoiceModalOpen(false)} />
      </AppModal>
    </div>
  );
}
