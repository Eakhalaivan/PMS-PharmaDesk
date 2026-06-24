import React, { useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { ArrowUpRight, ArrowDownRight } from 'lucide-react';
import ModuleFilterBar from '../components/ui/ModuleFilterBar';
import DataTable from '../components/ui/DataTable';
import Pagination from '../components/ui/Pagination';
import Badge from '../components/ui/Badge';

const mockData = [
  { id: 1, name: 'Amoxicillin 500mg', category: 'Tablet', velocity: 'Fast', abcGroup: 'A', qtySold: 1205, salesAmt: 145000, returnsQty: 12, netSales: 143560, margin: 25, purchasePrice: 90, sellingPrice: 120, leadTime: 3, icuQty: 200, genQty: 800, pedQty: 205 },
  { id: 2, name: 'Dolo 650mg', category: 'Tablet', velocity: 'Fast', abcGroup: 'C', qtySold: 980, salesAmt: 45000, returnsQty: 5, netSales: 44775, margin: 15, purchasePrice: 38, sellingPrice: 45, leadTime: 2, icuQty: 80, genQty: 900, pedQty: 0 },
  { id: 3, name: 'Cough Syrup 100ml', category: 'Syrup', velocity: 'Medium', abcGroup: 'B', qtySold: 450, salesAmt: 85000, returnsQty: 2, netSales: 84630, margin: 20, purchasePrice: 150, sellingPrice: 188, leadTime: 5, icuQty: 50, genQty: 100, pedQty: 300 },
];

const trendData = [
  { month: 'Jan 2026', revenue: 380000, units: 14000 },
  { month: 'Feb 2026', revenue: 405000, units: 15500 },
  { month: 'Mar 2026', revenue: 430000, units: 16000 },
  { month: 'Apr 2026', revenue: 490000, units: 18000 },
  { month: 'May 2026', revenue: 485000, units: 17500 },
  { month: 'Jun 2026', revenue: 520000, units: 19000 },
];

const TABS = [
  { id: 'landing', label: 'Analytics Landing' },
  { id: 'velocity', label: 'Velocity (Fast/Slow)' },
  { id: 'abc', label: 'ABC Analysis' },
  { id: 'mom', label: 'Month Comparison (MoM)' },
  { id: 'categories', label: 'Categories & Suppliers' },
  { id: 'wards', label: 'Clinical Patterns & Wards' },
  { id: 'reports', label: 'Efficiency & Reports' }
];

export default function ProductSalesPerformance() {
  const [activeTab, setActiveTab] = useState('landing');
  const [dateFilter, setDateFilter] = useState('This Month');
  const [searchTerm, setSearchTerm] = useState('');

  const filteredData = mockData.filter(row => {
    const s = searchTerm.toLowerCase();
    return (!searchTerm || row.name.toLowerCase().includes(s) || row.category.toLowerCase().includes(s));
  });

  const columnsDetail = [
    { header: 'S.No', render: (_, i) => i + 1 },
    { header: 'Medicine Name', accessor: 'name' },
    { header: 'Category', accessor: 'category' },
    { header: 'Velocity', render: (row) => <Badge variant={row.velocity === 'Fast' ? 'success' : row.velocity === 'Medium' ? 'warning' : 'danger'}>{row.velocity}</Badge> },
    { header: 'ABC Group', render: (row) => <Badge variant={row.abcGroup === 'A' ? 'primary' : row.abcGroup === 'B' ? 'info' : 'secondary'}>Group {row.abcGroup}</Badge> },
    { header: 'Qty Sold', accessor: 'qtySold' },
    { header: 'Gross Sales', render: (row) => `₹${row.salesAmt.toLocaleString()}` },
    { header: 'Returns Qty', accessor: 'returnsQty' },
    { header: 'Net Sales', render: (row) => <span className="font-bold text-primary">₹{row.netSales.toLocaleString()}</span> },
    { header: 'Margin %', render: (row) => `${row.margin}%` },
    { header: 'Purchase Price', render: (row) => `₹${row.purchasePrice}` },
    { header: 'Selling Price', render: (row) => `₹${row.sellingPrice}` },
    { header: 'Lead Time', render: (row) => `${row.leadTime} days` },
  ];

  return (
    <div className="space-y-6 pb-10">
      {/* Header section matching screenshot exactly */}
      <div className="flex items-start justify-between">
        <div className="flex flex-col gap-1">
          <h2 className="text-[22px] font-bold text-slate-800 tracking-tight">Product Sales Performance & Analytics Engine</h2>
          <p className="text-[13px] text-slate-400 font-medium tracking-wide">Track velocity, lock-in asset costs, ABC groups, ward distributions, and purchase efficiencies.</p>
        </div>
        
        {/* Date Filter Pills */}
        <div className="flex items-center bg-white border border-slate-100 rounded-lg p-1 shadow-sm">
          {['Today', 'This Week', 'This Month', 'This Quarter'].map(period => (
            <button
              key={period}
              onClick={() => setDateFilter(period)}
              className={`px-4 py-1.5 text-xs font-bold transition-all rounded-md ${
                dateFilter === period 
                  ? 'bg-blue-50 text-blue-600 shadow-sm' 
                  : 'text-slate-400 hover:text-slate-600 hover:bg-slate-50'
              }`}
            >
              {period}
            </button>
          ))}
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="flex items-center border-b border-slate-200 overflow-x-auto no-scrollbar pt-2">
        {TABS.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`px-5 py-3 text-[13px] font-bold whitespace-nowrap transition-colors relative ${
              activeTab === tab.id 
                ? 'text-blue-600' 
                : 'text-slate-400 hover:text-slate-600'
            }`}
          >
            {tab.label}
            {activeTab === tab.id && (
              <div className="absolute bottom-0 left-0 w-full h-[2px] bg-blue-600" />
            )}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'landing' && (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
          
          {/* KPI Grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
            
            {/* Total Sales Revenue */}
            <div className="bg-white border border-slate-100 rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.1em] mb-3">Total Sales Revenue</p>
              <h3 className="text-2xl font-black text-blue-600 tracking-tight mb-3">₹482,560.00</h3>
              <div className="flex items-center gap-1.5 text-[11px]">
                <span className="text-slate-400 font-medium">Prev: ₹461,200</span>
                <span className="flex items-center text-emerald-500 font-bold">
                  <ArrowUpRight className="w-3 h-3" /> 4.6%
                </span>
              </div>
            </div>

            {/* Total Units Dispensed */}
            <div className="bg-white border border-slate-100 rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.1em] mb-3">Total Units Dispensed</p>
              <h3 className="text-2xl font-black text-slate-800 tracking-tight mb-3">18,603 Units</h3>
              <div className="flex items-center gap-1.5 text-[11px]">
                <span className="text-slate-400 font-medium">Prev: 17,920</span>
                <span className="flex items-center text-emerald-500 font-bold">
                  <ArrowUpRight className="w-3 h-3" /> 3.8%
                </span>
              </div>
            </div>

            {/* Transactions */}
            <div className="bg-white border border-slate-100 rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.1em] mb-3">Transactions</p>
              <h3 className="text-2xl font-black text-slate-800 tracking-tight mb-3">3,650 Invoices</h3>
              <div className="flex items-center gap-1.5 text-[11px]">
                <span className="text-slate-400 font-medium">Prev: 3,510</span>
                <span className="flex items-center text-emerald-500 font-bold">
                  <ArrowUpRight className="w-3 h-3" /> 4.0%
                </span>
              </div>
            </div>

            {/* Avg Ticket Value */}
            <div className="bg-white border border-slate-100 rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.1em] mb-3">Avg Ticket Value</p>
              <h3 className="text-2xl font-black text-slate-800 tracking-tight mb-3">₹132.21</h3>
              <div className="flex items-center gap-1.5 text-[11px]">
                <span className="text-slate-400 font-medium">Prev: ₹131</span>
                <span className="flex items-center text-emerald-500 font-bold">
                  <ArrowUpRight className="w-3 h-3" /> 0.6%
                </span>
              </div>
            </div>

            {/* Credit Notes Value */}
            <div className="bg-white border border-slate-100 rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.1em] mb-3">Credit Notes Value</p>
              <h3 className="text-2xl font-black text-red-600 tracking-tight mb-3">₹18,450.00</h3>
              <div className="flex items-center gap-1.5 text-[11px]">
                <span className="text-slate-400 font-medium">Prev: ₹19,200</span>
                <span className="flex items-center text-red-500 font-bold">
                  <ArrowDownRight className="w-3 h-3" /> -3.9%
                </span>
              </div>
            </div>

            {/* Net Pharmacy Revenue */}
            <div className="bg-white border border-slate-100 rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.1em] mb-3">Net Pharmacy Revenue</p>
              <h3 className="text-2xl font-black text-emerald-600 tracking-tight mb-3">₹464,110.00</h3>
              <div className="flex items-center gap-1.5 text-[11px]">
                <span className="text-slate-400 font-medium">Prev: ₹442,000</span>
                <span className="flex items-center text-emerald-500 font-bold">
                  <ArrowUpRight className="w-3 h-3" /> 5.0%
                </span>
              </div>
            </div>

          </div>

          {/* Revenue Trend Line Chart */}
          <div className="bg-white border border-slate-100 rounded-xl p-6 shadow-sm">
            <div className="flex items-center justify-between mb-8">
              <h3 className="text-xs font-bold text-slate-500 uppercase tracking-[0.15em]">Revenue Trend Line Chart (This Month)</h3>
              <p className="text-[11px] font-medium text-slate-400 italic">30-day baseline simulation</p>
            </div>

            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={trendData} margin={{ top: 5, right: 20, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis 
                    dataKey="month" 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fill: '#94a3b8', fontSize: 10, fontWeight: 600 }}
                    dy={10}
                  />
                  <YAxis 
                    yAxisId="left"
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fill: '#94a3b8', fontSize: 10, fontWeight: 600 }}
                    tickFormatter={(value) => value === 0 ? '0' : value}
                    domain={[0, 600000]}
                    ticks={[0, 150000, 300000, 450000, 600000]}
                    dx={-10}
                  />
                  <Tooltip 
                    contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                    itemStyle={{ fontSize: '12px', fontWeight: 'bold' }}
                  />
                  <Legend 
                    verticalAlign="top" 
                    height={36} 
                    iconType="circle"
                    iconSize={8}
                    wrapperStyle={{ fontSize: '11px', fontWeight: 600, color: '#64748b' }}
                    formatter={(value, entry) => (
                      <span style={{ color: entry.color, marginLeft: '4px' }}>{value}</span>
                    )}
                  />
                  <Line 
                    yAxisId="left"
                    type="monotone" 
                    dataKey="revenue" 
                    name="Sales Revenue (₹)" 
                    stroke="#3b82f6" 
                    strokeWidth={2}
                    dot={false}
                    activeDot={{ r: 6, fill: '#3b82f6', strokeWidth: 0 }}
                  />
                  <Line 
                    yAxisId="left"
                    type="monotone" 
                    dataKey="units" 
                    name="Units Sold" 
                    stroke="#22c55e" 
                    strokeWidth={2}
                    dot={false}
                    activeDot={{ r: 6, fill: '#22c55e', strokeWidth: 0 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}

      {/* Fallback for other tabs */}
      {activeTab !== 'landing' && (
        <div className="animate-in fade-in slide-in-from-bottom-2 duration-300">
           <ModuleFilterBar 
            onSearch={setSearchTerm}
            searchValue={searchTerm}
          />
          <div className="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden mt-4">
            <DataTable columns={columnsDetail} data={filteredData} hover striped />
            <Pagination totalRecords={filteredData.length} currentPage={1} pageSize={10} onPageChange={() => {}} onPageSizeChange={() => {}} />
          </div>
        </div>
      )}

    </div>
  );
}
