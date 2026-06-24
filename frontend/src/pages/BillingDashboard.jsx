import React, { useState } from 'react';
import { IndianRupee, FileText, FileSpreadsheet, CreditCard, Eye, Printer, ArrowRight, RotateCcw } from 'lucide-react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import KPICard from '../components/ui/KPICard';
import DataTable from '../components/ui/DataTable';
import AppModal from '../components/ui/AppModal';
import Badge from '../components/ui/Badge';
import PharmacyInvoice from '../components/pharmacy/PharmacyInvoice';
import { useSystem } from '../context/SystemContext';
import { useConfig } from '../context/ConfigContext';
import api from '../utils/api';

const DashboardSkeleton = () => (
  <div className="space-y-8 animate-pulse">
    <div className="h-8 w-64 bg-slate-200 rounded"></div>
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      {[...Array(3)].map((_, i) => <div key={i} className="h-32 bg-slate-200 rounded-2xl"></div>)}
    </div>
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <div className="h-[400px] bg-slate-200 rounded-2xl"></div>
      <div className="h-[400px] bg-slate-200 rounded-2xl"></div>
    </div>
  </div>
);

export default function BillingDashboard() {
  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const navigate = useNavigate();

  const { systemData } = useSystem();
  const refreshIntervalSeconds = useConfig('dashboard_refresh_interval_seconds');
  const refreshInterval = (refreshIntervalSeconds ? Number(refreshIntervalSeconds) * 1000 : 60000);
  const currencySymbol = useConfig('currency_symbol') || '₹';

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-kpis'],
    queryFn: () => api.get('/dashboard/kpis').then(r => r.data),
    refetchInterval: refreshInterval,
  });

  const { data: revSummary, isLoading: revLoading } = useQuery({
    queryKey: ['dashboard-revenue-summary'],
    queryFn: () => api.get('/dashboard/revenue-summary').then(r => r.data),
    refetchInterval: refreshInterval,
  });

  const { data: trendRaw, isLoading: trendLoading } = useQuery({
    queryKey: ['dashboard-revenue-trend'],
    queryFn: () => api.get('/dashboard/revenue-trend?days=7').then(r => r.data),
    refetchInterval: refreshInterval,
  });

  const { data: recentBillsData, isLoading: billsLoading } = useQuery({
    queryKey: ['dashboard-recent-bills'],
    queryFn: () => api.get('/pharmacy/dashboard/recent-activities').then(r => r.data?.data || []),
    refetchInterval: refreshInterval,
  });

  const chartSales = Array.isArray(trendRaw)
    ? trendRaw.map(d => ({
        day: d.day_of_week || d.day || d.sale_date,
        sales: Number(d.daily_revenue || 0),
      }))
    : [];

  const isLoading = statsLoading || trendLoading || revLoading || billsLoading;

  if (isLoading) return <DashboardSkeleton />;

  const recentBills = recentBillsData || [];
  const fmt = (val) => val != null ? Number(val).toLocaleString('en-IN') : '0';

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">
          {systemData?.greeting || 'Welcome'}, here's your Billing Dashboard
        </h2>
        <p className="text-sm text-gray-500 font-medium">
          Financial overview and billing operations as of {new Date().toLocaleString('en-IN')}
        </p>
      </div>

      <div className="flex gap-4 overflow-x-auto pb-2">
        <button onClick={() => navigate('/sales')} className="flex items-center gap-2 bg-primary text-white px-4 py-2 rounded-lg shadow-sm hover:bg-primary-dark transition-colors whitespace-nowrap">
          <FileText className="w-4 h-4" /> New Sale
        </button>
        <button onClick={() => navigate('/credit-bills')} className="flex items-center gap-2 bg-amber-500 text-white px-4 py-2 rounded-lg shadow-sm hover:bg-amber-600 transition-colors whitespace-nowrap">
          <CreditCard className="w-4 h-4" /> Credit Bills
        </button>
        <button onClick={() => navigate('/returns')} className="flex items-center gap-2 bg-rose-500 text-white px-4 py-2 rounded-lg shadow-sm hover:bg-rose-600 transition-colors whitespace-nowrap">
          <RotateCcw className="w-4 h-4" /> Process Return
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <KPICard
          title="Today's Revenue"
          value={`${currencySymbol} ${fmt(stats?.todays_sales_revenue)}`}
          icon={IndianRupee}
          trend="up"
          className="border-l-4 border-l-success"
        />
        <KPICard
          title="Total Bills Today"
          value={`${fmt(stats?.bills_today)}`}
          icon={FileSpreadsheet}
          className="border-l-4 border-l-primary"
        />
        <KPICard
          title="This Week's Revenue"
          value={`${currencySymbol} ${fmt(revSummary?.this_weeks_total)}`}
          icon={IndianRupee}
          className="border-l-4 border-l-amber-500"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 pb-8">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col">
          <h3 className="font-bold text-lg text-gray-800 mb-6">Sales Trend (Last 7 Days)</h3>
          <div className="flex-1 w-full min-w-0 min-h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartSales}>
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
                <Area type="monotone" dataKey="sales" stroke="#2563eb" strokeWidth={3} fillOpacity={1} fill="url(#colorSales)" name="Revenue" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-lg text-gray-800">Recent Transactions</h3>
            <button onClick={() => navigate('/sales')} className="text-primary hover:text-primary-dark text-sm font-medium flex items-center gap-1">
              View All <ArrowRight className="w-4 h-4" />
            </button>
          </div>
          <div className="flex-1 overflow-auto">
            <DataTable
              data={recentBills.slice(0, 6)}
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
        </div>
      </div>

      <AppModal isOpen={isInvoiceModalOpen} onClose={() => setIsInvoiceModalOpen(false)} maxWidth="sm:max-w-4xl" padding={false}>
        <PharmacyInvoice bill={selectedInvoice} onClose={() => setIsInvoiceModalOpen(false)} />
      </AppModal>
    </div>
  );
}
