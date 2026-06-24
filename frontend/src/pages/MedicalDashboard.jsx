import React from 'react';
import { Stethoscope, Users, Activity, FileText, ArrowRight } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import KPICard from '../components/ui/KPICard';
import { useSystem } from '../context/SystemContext';
import { useConfig } from '../context/ConfigContext';
import api from '../utils/api';

const DashboardSkeleton = () => (
  <div className="space-y-8 animate-pulse">
    <div className="h-8 w-64 bg-slate-200 rounded"></div>
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {[...Array(2)].map((_, i) => <div key={i} className="h-32 bg-slate-200 rounded-2xl"></div>)}
    </div>
  </div>
);

export default function MedicalDashboard() {
  const navigate = useNavigate();
  const { systemData } = useSystem();
  const refreshIntervalSeconds = useConfig('dashboard_refresh_interval_seconds');
  const refreshInterval = (refreshIntervalSeconds ? Number(refreshIntervalSeconds) * 1000 : 60000);

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-kpis'],
    queryFn: () => api.get('/dashboard/kpis').then(r => r.data),
    refetchInterval: refreshInterval,
  });

  const isLoading = statsLoading;

  if (isLoading) return <DashboardSkeleton />;

  const fmt = (val) => val != null ? Number(val).toLocaleString('en-IN') : '0';

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">
          {systemData?.greeting || 'Welcome'}, here's your Medical Dashboard
        </h2>
        <p className="text-sm text-gray-500 font-medium">
          Clinical operations overview as of {new Date().toLocaleString('en-IN')}
        </p>
      </div>

      <div className="flex gap-4 overflow-x-auto pb-2">
        <button onClick={() => navigate('/pending-prescriptions')} className="flex items-center gap-2 bg-primary text-white px-4 py-2 rounded-lg shadow-sm hover:bg-primary-dark transition-colors whitespace-nowrap">
          <Stethoscope className="w-4 h-4" /> Pending Prescriptions
        </button>
        <button onClick={() => navigate('/patients')} className="flex items-center gap-2 bg-amber-500 text-white px-4 py-2 rounded-lg shadow-sm hover:bg-amber-600 transition-colors whitespace-nowrap">
          <Users className="w-4 h-4" /> Patients Directory
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <KPICard
          title="Pending Prescriptions"
          value={`${fmt(stats?.pending_prescriptions_count)}`}
          icon={Stethoscope}
          className="border-l-4 border-l-primary"
        />
        <KPICard
          title="Active Patients Today"
          value={`${fmt(stats?.active_patients_today_count)}`}
          icon={Activity}
          className="border-l-4 border-l-info"
        />
      </div>

      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center justify-center text-center py-12">
        <div className="w-16 h-16 rounded-full flex items-center justify-center mb-4 bg-emerald-100 bg-opacity-50">
          <FileText className="w-8 h-8 text-emerald-600" />
        </div>
        <h3 className="text-xl font-bold text-gray-900 mb-2">Clinical Workspace</h3>
        <p className="text-gray-500 max-w-md mb-6">
          Review patient history, issue new prescriptions, and manage medical workflows directly from the navigation menu.
        </p>
        <button onClick={() => navigate('/patients')} className="flex items-center gap-2 bg-emerald-50 text-emerald-700 px-6 py-2.5 rounded-lg shadow-sm font-medium hover:bg-emerald-100 transition-colors">
          Browse Patients <ArrowRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
