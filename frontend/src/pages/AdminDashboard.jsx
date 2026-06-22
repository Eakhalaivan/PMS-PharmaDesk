import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Box, IndianRupee, FileText, AlertTriangle, Clock, Users,
  ShoppingCart, Barcode, Plus, FilePlus, Printer, ArrowUp, ArrowDown,
  Info, AlertCircle, ShieldAlert, ArrowRight, Activity
} from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import api from '../utils/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';

const fetchDashboard = () => api.get('/pharmacy/dashboard').then(r => r.data.data);

export default function AdminDashboard() {
  const navigate = useNavigate();
  const { data: stats, isLoading } = useQuery({ queryKey: ['dashboard-stats'], queryFn: fetchDashboard, staleTime: 30000 });

  // ------------------------------------
  // Mock Data
  // ------------------------------------
  const mockRevenueData = [
    { name: 'Mon', revenue: 145000 },
    { name: 'Tue', revenue: 132000 },
    { name: 'Wed', revenue: 168000 },
    { name: 'Thu', revenue: 125000 },
    { name: 'Fri', revenue: 189000 },
    { name: 'Sat', revenue: 210000 },
    { name: 'Sun', revenue: 254000 }, // Today
  ];

  const mockAlerts = [
    { id: 1, severity: 'critical', title: 'Stock-out Risk', desc: 'Paracetamol 500mg has 0 inventory across all branches.', time: '10 mins ago', icon: ShieldAlert },
    { id: 2, severity: 'critical', title: 'Cold Chain Breach', desc: 'Fridge A temperature exceeded 8°C for 30 minutes.', time: '1 hr ago', icon: AlertTriangle },
    { id: 3, severity: 'warning', title: 'Near Expiry', desc: 'Batch BX-990 (Amoxicillin) expiring in 15 days.', time: '2 hrs ago', icon: Clock },
    { id: 4, severity: 'info', title: 'Pending GRN', desc: 'PO #1045 from Sun Pharma requires verification.', time: '3 hrs ago', icon: FilePlus },
    { id: 5, severity: 'info', title: 'Credit Bills Unverified', desc: '14 credit bills from yesterday pending sign-off.', time: '5 hrs ago', icon: FileText },
  ];

  // ------------------------------------
  // Helper Components
  // ------------------------------------
  const KPICard = ({ label, value, trendStr, trendVal, icon: Icon, type }) => {
    // Colors: #1a3c6e brand blue, red for danger, amber for warning, green for success
    const isCritical = type === 'critical';
    const isSuccess = type === 'success';
    const isWarning = type === 'warning';

    const colorClass = isCritical ? 'text-red-600' : isSuccess ? 'text-green-600' : isWarning ? 'text-amber-500' : 'text-[#1a3c6e]';
    const bgClass = isCritical ? 'bg-red-50' : isSuccess ? 'bg-green-50' : isWarning ? 'bg-amber-50' : 'bg-[#1a3c6e]/10';
    const borderClass = isCritical ? 'border-red-200' : isSuccess ? 'border-green-200' : isWarning ? 'border-amber-200' : 'border-slate-200';

    return (
      <div className="bg-white border border-slate-200 rounded-lg p-4 flex flex-col justify-between">
        <div className="flex justify-between items-start mb-2">
          <span className="text-sm font-medium text-slate-500">{label}</span>
          <div className={`p-2 rounded-md ${bgClass}`}>
            <Icon className={`w-4 h-4 ${colorClass}`} />
          </div>
        </div>
        <div>
          <h3 className="text-2xl font-medium text-slate-900">{value}</h3>
          <div className="flex items-center gap-1 mt-1">
            {trendVal > 0 ? <ArrowUp className="w-3 h-3 text-green-600" /> : trendVal < 0 ? <ArrowDown className="w-3 h-3 text-red-600" /> : null}
            <span className={`text-xs font-medium ${trendVal > 0 ? 'text-green-600' : trendVal < 0 ? 'text-red-600' : 'text-slate-400'}`}>
              {trendStr}
            </span>
          </div>
        </div>
      </div>
    );
  };

  const severityStyles = {
    critical: { iconClass: 'text-red-600', bgClass: 'bg-red-50', borderClass: 'border-red-200' },
    warning: { iconClass: 'text-amber-500', bgClass: 'bg-amber-50', borderClass: 'border-amber-200' },
    info: { iconClass: 'text-[#1a3c6e]', bgClass: 'bg-blue-50', borderClass: 'border-blue-200' },
  };

  if (isLoading) return <div className="p-10 text-center animate-pulse text-slate-500 font-medium">Loading Dashboard...</div>;

  return (
    <div className="min-h-full bg-slate-50 p-6 space-y-6">
      
      {/* 1. KPI CARDS */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <KPICard label="Total SKUs in stock" value="12,450" trendStr="Stable vs yesterday" trendVal={0} icon={Box} type="info" />
        <KPICard label="Today's Sales" value="₹2,54,000" trendStr="12% vs yesterday" trendVal={1} icon={IndianRupee} type="success" />
        <KPICard label="Pending Prescriptions" value="24" trendStr="5 fewer than usual" trendVal={-1} icon={FileText} type="info" />
        <KPICard label="Low Stock Alerts" value="18" trendStr="Action required" trendVal={1} icon={AlertTriangle} type="critical" />
        <KPICard label="Expiring (30 Days)" value="45" trendStr="Needs review" trendVal={1} icon={Clock} type="warning" />
        <KPICard label="Active Patients" value="312" trendStr="8% vs yesterday" trendVal={1} icon={Users} type="success" />
      </div>

      {/* 2. QUICK ACTIONS */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        {[
          { label: 'New Sale', icon: ShoppingCart, path: '/sales' },
          { label: 'Scan Barcode', icon: Barcode, path: '/stocks' },
          { label: 'Add Stock', icon: Plus, path: '/stocks' },
          { label: 'Create PO', icon: FilePlus, path: '/purchase-orders' },
          { label: 'Pending Rx', icon: FileText, path: '/pending-prescriptions' },
          { label: 'Print Day Report', icon: Printer, path: '/reports' },
        ].map((action, idx) => (
          <button 
            key={idx}
            onClick={() => navigate(action.path)}
            className="flex items-center justify-center gap-2 bg-white border border-slate-200 py-3 rounded-lg hover:border-[#1a3c6e] hover:bg-slate-50 transition-colors text-sm font-medium text-slate-700"
          >
            <action.icon className="w-4 h-4 text-[#1a3c6e]" />
            {action.label}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* 3. ALERTS SUMMARY PANEL */}
        <div className="lg:col-span-5 bg-white border border-slate-200 rounded-lg flex flex-col h-full">
          <div className="px-5 py-4 border-b border-slate-200 flex justify-between items-center">
            <h2 className="text-base font-medium text-[#1a3c6e] flex items-center gap-2">
              <Activity className="w-4 h-4" /> Smart Alerts
            </h2>
          </div>
          <div className="flex-1 p-0 flex flex-col">
            <div className="divide-y divide-slate-100 flex-1">
              {mockAlerts.map(alert => {
                const styles = severityStyles[alert.severity];
                return (
                  <div key={alert.id} className="p-4 flex gap-4 hover:bg-slate-50 transition-colors">
                    <div className={`shrink-0 w-10 h-10 rounded-md flex items-center justify-center ${styles.bgClass} ${styles.borderClass} border`}>
                      <alert.icon className={`w-5 h-5 ${styles.iconClass}`} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex justify-between items-start mb-1">
                        <h4 className="text-sm font-medium text-slate-900 truncate">{alert.title}</h4>
                        <span className="text-xs font-normal text-slate-400 whitespace-nowrap ml-2">{alert.time}</span>
                      </div>
                      <p className="text-sm font-normal text-slate-500 line-clamp-1">{alert.desc}</p>
                    </div>
                  </div>
                );
              })}
            </div>
            <button className="w-full py-3 border-t border-slate-200 text-sm font-medium text-[#1a3c6e] hover:bg-slate-50 transition-colors flex items-center justify-center gap-1">
              View All Alerts <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* 4. REVENUE WIDGET */}
        <div className="lg:col-span-7 bg-white border border-slate-200 rounded-lg flex flex-col">
          <div className="px-5 py-4 border-b border-slate-200 flex justify-between items-center">
            <h2 className="text-base font-medium text-[#1a3c6e]">7-Day Revenue Trend</h2>
          </div>
          <div className="p-6 flex-1 flex flex-col">
            <div className="flex gap-6 mb-6">
              <div className="bg-slate-50 border border-slate-200 rounded-lg px-4 py-3 flex-1">
                <span className="text-xs font-medium text-slate-500 block mb-1">Today's Total</span>
                <span className="text-xl font-medium text-slate-900">₹2,54,000</span>
              </div>
              <div className="bg-slate-50 border border-slate-200 rounded-lg px-4 py-3 flex-1">
                <span className="text-xs font-medium text-slate-500 block mb-1">This Week</span>
                <span className="text-xl font-medium text-slate-900">₹12,23,000</span>
              </div>
              <div className="bg-slate-50 border border-slate-200 rounded-lg px-4 py-3 flex-1">
                <span className="text-xs font-medium text-slate-500 block mb-1">This Month</span>
                <span className="text-xl font-medium text-slate-900">₹45,80,000</span>
              </div>
            </div>

            <div className="flex-1 min-h-[250px] mb-6">
              <div style={{ width: '100%', height: 250 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={mockRevenueData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#64748b' }} dy={10} />
                    <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#64748b' }} tickFormatter={(val) => `₹${val/1000}k`} />
                    <Tooltip 
                      cursor={{ fill: '#f8fafc' }} 
                      contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0', boxShadow: 'none' }}
                      formatter={(value) => [`₹${value.toLocaleString('en-IN')}`, 'Revenue']}
                    />
                    <Bar dataKey="revenue" radius={[4, 4, 0, 0]} maxBarSize={40}>
                      {mockRevenueData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={index === mockRevenueData.length - 1 ? '#1a3c6e' : '#cbd5e1'} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="border-t border-slate-200 pt-5">
              <span className="text-xs font-medium text-slate-500 block mb-3">Revenue Breakdown (Today)</span>
              <div className="flex h-4 rounded-full overflow-hidden mb-2 border border-slate-200">
                <div className="bg-[#1a3c6e] h-full" style={{ width: '65%' }} title="OTC Sales: 65%"></div>
                <div className="bg-amber-500 h-full" style={{ width: '25%' }} title="Credit Sales: 25%"></div>
                <div className="bg-teal-500 h-full" style={{ width: '10%' }} title="Insurance Claims: 10%"></div>
              </div>
              <div className="flex justify-between text-xs font-medium text-slate-500">
                <div className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-[#1a3c6e]"></div>OTC Sales (65%)</div>
                <div className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-amber-500"></div>Credit Sales (25%)</div>
                <div className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-teal-500"></div>Insurance (10%)</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
