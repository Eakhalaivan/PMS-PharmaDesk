import React from 'react';
import { Shield, Users, Activity, FileSpreadsheet, IndianRupee, Bell, ArrowRight } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import KPICard from '../components/ui/KPICard';
import { useSystem } from '../context/SystemContext';
import { useConfig } from '../context/ConfigContext';
import api from '../utils/api';

const DashboardSkeleton = () => (
  <div className="space-y-8 animate-pulse">
    <div className="h-8 w-64 bg-slate-200 rounded"></div>
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {[...Array(4)].map((_, i) => <div key={i} className="h-32 bg-slate-200 rounded-2xl"></div>)}
    </div>
  </div>
);

export default function SupervisorDashboard() {
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

  const { data: alertsData, isLoading: alertsLoading } = useQuery({
    queryKey: ['dashboard-alerts'],
    queryFn: () => api.get('/dashboard/alerts').then(r => r.data?.alerts || []),
    refetchInterval: refreshInterval,
  });

  const isLoading = statsLoading || alertsLoading;

  if (isLoading) return <DashboardSkeleton />;

  const fmt = (val) => val != null ? Number(val).toLocaleString('en-IN') : '0';
  const alerts = alertsData || [];

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">
          {systemData?.greeting || 'Welcome'}, here's your Supervisor Dashboard
        </h2>
        <p className="text-sm text-gray-500 font-medium">
          Operational oversight and team activity as of {new Date().toLocaleString('en-IN')}
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <KPICard
          title="Active Staff"
          value={`${fmt(stats?.active_staff)}`}
          icon={Users}
          className="border-l-4 border-l-primary"
        />
        <KPICard
          title="Today's Bills"
          value={`${fmt(stats?.bills_today)}`}
          icon={FileSpreadsheet}
          className="border-l-4 border-l-info"
        />
        <KPICard
          title="Today's Revenue"
          value={`${currencySymbol} ${fmt(stats?.todays_sales_revenue)}`}
          icon={IndianRupee}
          trend="up"
          className="border-l-4 border-l-success"
        />
        <KPICard
          title="Active Patients"
          value={`${fmt(stats?.active_patients_today_count)}`}
          icon={Activity}
          className="border-l-4 border-l-warning"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 pb-8">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-lg text-gray-800 flex items-center gap-2">
              <Bell className="w-5 h-5 text-indigo-500" /> Active System Alerts
            </h3>
          </div>
          <div className="space-y-4">
            {alerts.length === 0 ? (
              <p className="text-gray-500 text-center py-4">No active alerts.</p>
            ) : (
              alerts.map((alert, idx) => (
                <div key={idx} className="flex items-start gap-4 p-4 rounded-xl bg-slate-50 border border-slate-100">
                  <div className={`p-2 rounded-lg ${alert.severity === 'critical' ? 'bg-red-100 text-red-600' : alert.severity === 'warning' ? 'bg-amber-100 text-amber-600' : 'bg-blue-100 text-blue-600'}`}>
                    <Shield className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="font-bold text-gray-900 text-sm">{alert.title}</h4>
                    <p className="text-gray-600 text-sm mt-1">{alert.desc}</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center justify-center text-center py-12">
          <div className="w-16 h-16 rounded-full flex items-center justify-center mb-4 bg-indigo-100 bg-opacity-50">
            <Shield className="w-8 h-8 text-indigo-600" />
          </div>
          <h3 className="text-xl font-bold text-gray-900 mb-2">Management Controls</h3>
          <p className="text-gray-500 max-w-md mb-6">
            Review detailed reports, manage user access, and oversee clinical and financial operations.
          </p>
          <button onClick={() => navigate('/reports')} className="flex items-center gap-2 bg-indigo-50 text-indigo-700 px-6 py-2.5 rounded-lg shadow-sm font-medium hover:bg-indigo-100 transition-colors">
            View Analytics & Reports <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
