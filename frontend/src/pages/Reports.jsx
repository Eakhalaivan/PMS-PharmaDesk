import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  BarChart2, Search, Star, StarOff, Clock, Calendar, Download,
  FileSpreadsheet, FileText, Mail, MessageSquare, Bell, ChevronRight,
  X, Filter, RefreshCcw, Plus, Trash2, ToggleLeft, ToggleRight,
  TrendingUp, Package, ShoppingCart, DollarSign, Shield, Users, LayoutGrid,
  AlertTriangle, CheckCircle2, Info, Settings, Eye, Printer
} from 'lucide-react';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';
import { exportToPDF, exportToExcel } from '../utils/reportExport';

// ── REPORT CATALOG ─────────────────────────────────────────────────────────────
const REPORT_CATALOG = [
  // SALES
  {
    id: 'daily-summary', category: 'sales', name: 'Daily Sales Summary',
    desc: 'Total bills, cash vs credit split, GST collected, net revenue',
    roles: ['SYSTEM_ADMIN','SUPERVISOR','PHARMACIST'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales/summary?from=${from}&to=${to}`),
    columns: ['date','billCount','cashBills','creditBills','totalRevenue','totalTax','totalDiscount','netRevenue'],
    headers: ['Period','# Bills','Cash Bills','Credit Bills','Total Revenue','GST Collected','Discounts','Net Revenue']
  },
  {
    id: 'itemised-register', category: 'sales', name: 'Itemised Sales Register',
    desc: 'Every bill line item with medicine, quantity, rate, discount, GST',
    roles: ['SYSTEM_ADMIN','SUPERVISOR','PHARMACIST'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales/itemised?from=${from}&to=${to}`),
    columns: ['billNumber','date','patient','doctor','medicine','hsnCode','quantity','unitPrice','discount','tax','netAmount'],
    headers: ['Bill No.','Date','Patient','Doctor','Medicine','HSN','Qty','Rate','Discount','GST','Net Amt']
  },
  {
    id: 'medicine-wise-sales', category: 'sales', name: 'Medicine-wise Sales Report',
    desc: 'Total units sold and revenue per medicine for the period',
    roles: ['SYSTEM_ADMIN','SUPERVISOR','PHARMACIST'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales/medicine-wise?from=${from}&to=${to}`),
    columns: ['medicine','unitsSold','revenue','tax'],
    headers: ['Medicine','Units Sold','Revenue (₹)','GST (₹)']
  },
  {
    id: 'credit-sales', category: 'sales', name: 'Credit Sales Report',
    desc: 'All credit bills with outstanding balance and payment status',
    roles: ['SYSTEM_ADMIN','SUPERVISOR','PHARMACIST','ACCOUNTS'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales/credit?from=${from}&to=${to}`),
    columns: ['billNumber','date','patient','netAmount','paidAmount','balanceAmount','status'],
    headers: ['Bill No.','Date','Patient','Net Amt','Paid','Balance','Status']
  },
  {
    id: 'cancelled-bills', category: 'sales', name: 'Cancelled Bills Report',
    desc: 'All voided transactions with reason and authorising user',
    roles: ['SYSTEM_ADMIN','SUPERVISOR'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales/cancelled?from=${from}&to=${to}`),
    columns: ['billNumber','date','patient','amount','cancelledBy'],
    headers: ['Bill No.','Date','Patient','Amount','Cancelled By']
  },

  // STOCK
  {
    id: 'current-stock', category: 'stock', name: 'Current Stock Position',
    desc: 'All medicines with quantity, batch, reorder level, stock status',
    roles: ['SYSTEM_ADMIN','SUPERVISOR','STOREKEEPER','PHARMACIST'], hasDateRange: false,
    endpoint: () => pharmacyService.api.get('/pharmacy/reports/stock'),
    columns: ['medicine','category','batch','quantity','unitPrice','mrp','expiry','supplier','value'],
    headers: ['Medicine','Category','Batch','Qty Available','Purchase Rate','MRP','Expiry','Supplier','Stock Value']
  },
  {
    id: 'expiry-report', category: 'stock', name: 'Expiry Report',
    desc: 'Medicines expiring within configured days – Critical (≤15d), Warning (≤30d), Early Alert (≤60d)',
    roles: ['SYSTEM_ADMIN','SUPERVISOR','STOREKEEPER','PHARMACIST'], hasDateRange: false,
    extraFilters: [{ key: 'days', label: 'Within Days', type: 'number', default: 60 }],
    endpoint: (_, __, extra) => pharmacyService.api.get(`/pharmacy/reports/stock/expiry?days=${extra?.days || 60}`),
    columns: ['medicine','batch','expiry','quantity','supplier','daysLeft','urgency'],
    headers: ['Medicine','Batch','Expiry Date','Qty','Supplier','Days Left','Status']
  },
  {
    id: 'slow-moving', category: 'stock', name: 'Slow-Moving Stock Report',
    desc: 'Medicines with dispensing below configured threshold in the period',
    roles: ['SYSTEM_ADMIN','SUPERVISOR','STOREKEEPER'], hasDateRange: true,
    extraFilters: [{ key: 'threshold', label: 'Min Units Sold', type: 'number', default: 5 }],
    endpoint: (from, to, extra) => pharmacyService.api.get(`/pharmacy/reports/stock/slow-moving?from=${from}&to=${to}&threshold=${extra?.threshold || 5}`),
    columns: ['medicine','soldInPeriod'],
    headers: ['Medicine','Units Sold in Period']
  },

  // PURCHASE
  {
    id: 'purchase-register', category: 'purchase', name: 'Purchase Register (GRN)',
    desc: 'All goods receipts with supplier, invoice number, batch, quantity, value',
    roles: ['SYSTEM_ADMIN','SUPERVISOR','STOREKEEPER'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/purchase/register?from=${from}&to=${to}`),
    columns: ['grnNumber','date','supplier','invoiceNumber','status','itemCount'],
    headers: ['GRN No.','Date','Supplier','Invoice No.','Status','Items']
  },
  {
    id: 'outstanding-payables', category: 'purchase', name: 'Outstanding Payables',
    desc: 'Pending invoices grouped by aging buckets (0-30, 31-60, 61+ days)',
    roles: ['SYSTEM_ADMIN','ACCOUNTS'], hasDateRange: false,
    endpoint: () => pharmacyService.api.get('/pharmacy/reports/purchase/payables'),
    columns: ['invoiceNumber','supplier','totalAmount','status','daysOld','agingBucket'],
    headers: ['Invoice No.','Supplier','Amount','Status','Days Old','Aging Bucket']
  },
  {
    id: 'supplier-performance', category: 'purchase', name: 'Supplier Performance Scorecard',
    desc: 'All suppliers ranked by overall score – delivery, fill rate, quality, accuracy',
    roles: ['SYSTEM_ADMIN','SUPERVISOR'], hasDateRange: false,
    endpoint: () => pharmacyService.api.get('/pharmacy/reports/supplier/performance'),
    columns: ['supplier','overallScore','onTimeDelivery','orderFillRate','qualityRejection','invoiceAccuracy'],
    headers: ['Supplier','Overall Score','On-Time Delivery %','Fill Rate %','Rejection %','Invoice Accuracy %']
  },

  // FINANCIAL / GST
  {
    id: 'tax-summary', category: 'gst', name: 'GST Summary Report',
    desc: 'Output GST collected, ITC on purchases, net GST payable – ready for filing',
    roles: ['SYSTEM_ADMIN','ACCOUNTS'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/tax?from=${from}&to=${to}`),
    columns: ['period','totalAmount','taxableAmount','cgst','sgst','igst','totalTax','billCount'],
    headers: ['Period','Total Revenue','Taxable Value','CGST','SGST','IGST','Total GST','Bills']
  },
  {
    id: 'gst-sales-register', category: 'gst', name: 'GST Sales Register (GSTR-1)',
    desc: 'All sales with HSN code, taxable value, CGST/SGST/IGST per line – GSTR-1 format',
    roles: ['SYSTEM_ADMIN','ACCOUNTS'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/gst/sales?from=${from}&to=${to}`),
    columns: ['billNumber','date','patient','hsnCode','medicine','quantity','unitPrice','taxableValue','cgst','sgst','igst','totalGst'],
    headers: ['Bill No.','Date','Buyer','HSN','Item','Qty','Rate','Taxable Value','CGST','SGST','IGST','Total GST']
  },

  // COMPLIANCE
  {
    id: 'schedule-h-register', category: 'compliance', name: 'Schedule H/H1 Dispensing Register',
    desc: 'All Schedule H and H1 dispensing with patient, doctor, Rx details – mandatory register',
    roles: ['SYSTEM_ADMIN','COMPLIANCE'], hasDateRange: true, isRestricted: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales/itemised?from=${from}&to=${to}`),
    columns: ['billNumber','date','patient','doctor','medicine','quantity','unitPrice'],
    headers: ['Sr. No.','Date','Patient Name','Doctor','Rx Medicine','Qty Dispensed','Rate']
  },
  {
    id: 'narcotic-register', category: 'compliance', name: 'Narcotic & Psychotropic Register',
    desc: 'Legally mandatory NDPS register with running balance and reconciliation',
    roles: ['SYSTEM_ADMIN','COMPLIANCE','NARCOTIC_REGISTER'], hasDateRange: true, isRestricted: true, isNarcotic: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales/itemised?from=${from}&to=${to}`),
    columns: ['billNumber','date','patient','doctor','medicine','quantity','unitPrice'],
    headers: ['Sr. No.','Date','Patient','Age/Gender','IP No.','Ward/Bed','Doctor','Reg No.','Rx No.','Qty Prescribed','Qty Dispensed','Batch','Opening Bal','Closing Bal','Pharmacist']
  },
  {
    id: 'drug-license-compliance', category: 'compliance', name: 'Drug License Compliance Report',
    desc: 'All suppliers with DL expiry dates, flagging licenses expiring within 60 days',
    roles: ['SYSTEM_ADMIN','COMPLIANCE','SUPERVISOR'], hasDateRange: false, isRestricted: true,
    endpoint: () => pharmacyService.getSuppliers(),
    columns: ['name','supplierCode','drugLicenseNumber','drugLicenseExpiry','status'],
    headers: ['Supplier','Code','Drug License No.','Expiry Date','Status']
  },
  {
    id: 'user-activity-audit', category: 'compliance', name: 'User Activity Audit Report',
    desc: 'All user login/logout, transactions, adjustments, and approvals per user per shift',
    roles: ['SYSTEM_ADMIN'], hasDateRange: true, isRestricted: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales?from=${from}&to=${to}`),
    columns: ['billNumber','date','patient','amount','status'],
    headers: ['Action','User','Date/Time','Details','Module']
  },

  // CLINICAL
  {
    id: 'prescription-fulfilment', category: 'clinical', name: 'Prescription Fulfilment Rate',
    desc: 'Percentage of prescription line items fully dispensed vs stockout',
    roles: ['SYSTEM_ADMIN','PHARMACIST'], hasDateRange: true,
    endpoint: (from, to) => pharmacyService.api.get(`/pharmacy/reports/sales?from=${from}&to=${to}`),
    columns: ['billNumber','date','patient','doctorName','amount','status'],
    headers: ['Bill No.','Date','Patient','Doctor','Amount','Status']
  },
];

const CATEGORIES = [
  { id: 'all', label: 'All Reports', icon: LayoutGrid, color: 'text-slate-600' },
  { id: 'sales', label: 'Sales', icon: TrendingUp, color: 'text-blue-600' },
  { id: 'stock', label: 'Stock & Inventory', icon: Package, color: 'text-emerald-600' },
  { id: 'purchase', label: 'Purchase & Supplier', icon: ShoppingCart, color: 'text-cyan-600' },
  { id: 'gst', label: 'Financial & GST', icon: DollarSign, color: 'text-violet-600' },
  { id: 'compliance', label: 'Compliance & Regulatory', icon: Shield, color: 'text-purple-600' },
  { id: 'clinical', label: 'Clinical', icon: Users, color: 'text-rose-600' },
  { id: 'schedules', label: 'Scheduled Reports', icon: Bell, color: 'text-amber-600' },
];

const today = new Date().toISOString().split('T')[0];
const monthStart = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0];
const fmtDateTime = (d) => d ? `${d}T00:00:00` : '';
const fmtDateTimeEnd = (d) => d ? `${d}T23:59:59` : '';
const fmtDate = (d) => {
  if (!d) return '—';
  const dt = new Date(d);
  return `${String(dt.getDate()).padStart(2,'0')}-${String(dt.getMonth()+1).padStart(2,'0')}-${dt.getFullYear()}`;
};

const urgencyBadge = (u) => ({
  EXPIRED:     'bg-red-100 text-red-700 border border-red-200',
  CRITICAL:    'bg-red-50 text-red-600 border border-red-200',
  WARNING:     'bg-amber-50 text-amber-600 border border-amber-200',
  EARLY_ALERT: 'bg-blue-50 text-blue-600 border border-blue-200',
})[u] || 'bg-slate-100 text-slate-600';

// ─── EXPORT UTILITIES (jsPDF + SheetJS) ────────────────────────────────────────
async function doExportPDF(report, data, filters) {
  const { jsPDF } = await import('jspdf');
  const autoTable = (await import('jspdf-autotable')).default;
  const doc = new jsPDF({ orientation: data.length > 100 ? 'l' : 'p', unit: 'mm' });

  // Letterhead
  doc.setFontSize(14); doc.setFont('helvetica','bold');
  doc.text('PharmaCare Hospital Pharmacy', 14, 16);
  doc.setFontSize(8); doc.setFont('helvetica','normal'); doc.setTextColor(100);
  doc.text('Drug License: DL-MH-123456  |  GSTIN: 27AAAAA0000A1Z5  |  Tel: +91 22 1234 5678', 14, 22);
  doc.setDrawColor(59, 130, 246); doc.setLineWidth(0.5); doc.line(14, 25, doc.internal.pageSize.width - 14, 25);

  // Title
  doc.setFontSize(12); doc.setFont('helvetica','bold'); doc.setTextColor(30, 41, 59);
  doc.text(report.name, 14, 33);
  doc.setFontSize(8); doc.setFont('helvetica','normal'); doc.setTextColor(100);
  doc.text(`Filters: ${filters}    |    Generated: ${new Date().toLocaleString('en-IN')}`, 14, 39);

  // Table
  autoTable(doc, {
    head: [report.headers],
    body: data.map(row => report.columns.map(col => {
      const val = row[col];
      if (val == null) return '—';
      if (typeof val === 'number') return val.toFixed ? val.toFixed(2) : String(val);
      if (typeof val === 'string' && val.includes('T')) {
        try { return fmtDate(val); } catch { return val; }
      }
      return String(val);
    })),
    startY: 44,
    theme: 'grid',
    headStyles: { fillColor: [59, 130, 246], textColor: 255, fontStyle: 'bold', fontSize: 7 },
    bodyStyles: { fontSize: 7, cellPadding: 2 },
    alternateRowStyles: { fillColor: [248, 250, 252] },
    margin: { left: 14, right: 14 },
    didDrawPage: (d) => {
      const pg = doc.internal.getNumberOfPages();
      doc.setFontSize(7); doc.setTextColor(150);
      doc.text(`Page ${d.pageNumber} of ${pg}`, doc.internal.pageSize.width - 25, doc.internal.pageSize.height - 8);
      doc.text('Authorised Signatory: ___________________', 14, doc.internal.pageSize.height - 8);
    }
  });

  doc.save(`${report.id}_${today}.pdf`);
  toast.success('PDF downloaded');
}

async function doExportExcel(report, data) {
  const XLSX = await import('xlsx');
  const wb = XLSX.utils.book_new();

  // Data sheet
  const dataRows = data.map(row => {
    const obj = {};
    report.headers.forEach((h, i) => {
      const val = row[report.columns[i]];
      obj[h] = val ?? '';
    });
    return obj;
  });
  const ws = XLSX.utils.json_to_sheet(dataRows);
  ws['!cols'] = report.headers.map(() => ({ wch: 18 }));
  XLSX.utils.book_append_sheet(wb, ws, 'Data');

  // Summary sheet
  const summaryData = [
    ['Report', report.name],
    ['Generated', new Date().toLocaleString('en-IN')],
    ['Total Records', data.length],
    ['Pharmacy', 'PharmaCare Hospital Pharmacy'],
    ['GSTIN', '27AAAAA0000A1Z5'],
  ];
  const ws2 = XLSX.utils.aoa_to_sheet(summaryData);
  XLSX.utils.book_append_sheet(wb, ws2, 'Summary');

  XLSX.writeFile(wb, `${report.id}_${today}.xlsx`);
  toast.success('Excel downloaded');
}

// ─── SCHEDULE DRAWER ──────────────────────────────────────────────────────────
function ScheduleDrawer({ report, onClose, onSaved }) {
  const [form, setForm] = useState({
    scheduleName: `${report.name} – Auto`,
    reportType: report.id,
    reportCategory: report.category,
    frequency: 'DAILY',
    deliveryTime: '08:00',
    channels: 'EMAIL',
    emailRecipients: '',
    whatsappNumbers: '',
    fileFormats: 'PDF',
    active: true,
    reportParams: ''
  });
  const f = k => e => setForm(p => ({ ...p, [k]: e.target.value }));
  const inputCls = "w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-400 bg-white";

  const handleSave = async () => {
    try {
      const res = await pharmacyService.api.post('/pharmacy/report-schedules', form);
      if (res.data?.success) { toast.success('Schedule saved!'); onSaved(); onClose(); }
    } catch { toast.error('Failed to save schedule'); }
  };

  return (
    <div className="fixed inset-0 z-50 flex">
      <div className="flex-1 bg-black/20" onClick={onClose} />
      <div className="w-[480px] bg-white shadow-2xl flex flex-col overflow-hidden">
        <div className="flex items-center justify-between p-5 border-b border-slate-100 bg-blue-50">
          <div>
            <div className="text-sm font-bold text-blue-800 flex items-center gap-2"><Bell className="w-4 h-4" /> Schedule Report Delivery</div>
            <div className="text-xs text-blue-600 mt-0.5">{report.name}</div>
          </div>
          <button onClick={onClose} className="p-1.5 hover:bg-blue-100 rounded-lg"><X className="w-4 h-4 text-blue-600" /></button>
        </div>

        <div className="flex-1 overflow-y-auto p-5 space-y-4">
          <div>
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Schedule Name</label>
            <input className={inputCls} value={form.scheduleName} onChange={f('scheduleName')} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Frequency</label>
              <select className={inputCls} value={form.frequency} onChange={f('frequency')}>
                {['DAILY','WEEKLY','FORTNIGHTLY','MONTHLY','CUSTOM'].map(v => (
                  <option key={v} value={v}>{v.charAt(0) + v.slice(1).toLowerCase()}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Delivery Time</label>
              <input type="time" className={inputCls} value={form.deliveryTime} onChange={f('deliveryTime')} />
            </div>
          </div>
          {form.frequency === 'CUSTOM' && (
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">CRON Expression</label>
              <input className={`${inputCls} font-mono`} value={form.reportParams} onChange={f('reportParams')} placeholder="0 8 * * MON-FRI" />
            </div>
          )}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Delivery Channel</label>
              <select className={inputCls} value={form.channels} onChange={f('channels')}>
                <option value="EMAIL">Email only</option>
                <option value="WHATSAPP">WhatsApp only</option>
                <option value="BOTH">Email + WhatsApp</option>
              </select>
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">File Format</label>
              <select className={inputCls} value={form.fileFormats} onChange={f('fileFormats')}>
                <option value="PDF">PDF only</option>
                <option value="EXCEL">Excel only</option>
                <option value="BOTH">PDF + Excel</option>
              </select>
            </div>
          </div>
          {(form.channels === 'EMAIL' || form.channels === 'BOTH') && (
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Email Recipients (comma-separated)</label>
              <input className={inputCls} value={form.emailRecipients} onChange={f('emailRecipients')} placeholder="admin@pharmacy.com, accounts@pharmacy.com" />
            </div>
          )}
          {(form.channels === 'WHATSAPP' || form.channels === 'BOTH') && (
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">WhatsApp Numbers (comma-separated)</label>
              <input className={inputCls} value={form.whatsappNumbers} onChange={f('whatsappNumbers')} placeholder="+919876543210, +919876543211" />
            </div>
          )}
          <div className="p-3 bg-blue-50 border border-blue-100 rounded-lg text-xs text-blue-700">
            <strong>Note:</strong> WhatsApp delivery sends a PDF attachment with a summary message (report name, period, key metric). Failed deliveries auto-retry after 30 minutes.
          </div>
        </div>

        <div className="p-5 border-t border-slate-100 flex gap-3">
          <button onClick={onClose} className="flex-1 px-4 py-2.5 border border-slate-200 text-sm font-bold text-slate-600 rounded-xl hover:bg-slate-50 transition-all">Cancel</button>
          <button onClick={handleSave} className="flex-1 px-4 py-2.5 bg-blue-600 text-white text-sm font-bold rounded-xl hover:bg-blue-700 transition-all flex items-center justify-center gap-2">
            <Bell className="w-4 h-4" /> Save Schedule
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── REPORT PREVIEW PANEL ─────────────────────────────────────────────────────
function ReportPreviewPanel({ report, onClose, onSchedule }) {
  const [from, setFrom] = useState(monthStart);
  const [to, setTo] = useState(today);
  const [extraFilters, setExtraFilters] = useState(
    Object.fromEntries((report.extraFilters || []).map(ef => [ef.key, ef.default]))
  );
  const [data, setData] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);

  const generate = useCallback(async () => {
    setLoading(true);
    try {
      const res = await report.endpoint(
        fmtDateTime(from), fmtDateTimeEnd(to), extraFilters
      );
      const payload = res.data?.data ?? res.data;
      if (Array.isArray(payload)) {
        setData(payload);
        setSummary(null);
      } else if (payload && typeof payload === 'object') {
        setSummary(payload);
        setData([]);
      }
    } catch { toast.error('Failed to generate report'); }
    finally { setLoading(false); }
  }, [report, from, to, extraFilters]);

  useEffect(() => { generate(); }, []);

  const filterStr = `${from} to ${to}${Object.entries(extraFilters).map(([k,v]) => `, ${k}: ${v}`).join('')}`;

  const renderCell = (row, col, header) => {
    const val = row[col];
    if (val == null) return <span className="text-slate-300">—</span>;
    if (col === 'urgency') return <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${urgencyBadge(val)}`}>{val?.replace('_',' ')}</span>;
    if (col === 'status') return <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
      val === 'PAID' || val === 'ACTIVE' || val === 'CONFIRMED' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' :
      val === 'PENDING' || val === 'DRAFT' ? 'bg-amber-50 text-amber-700 border border-amber-200' :
      val === 'CANCELLED' || val === 'EXPIRED' ? 'bg-red-50 text-red-700 border border-red-200' :
      'bg-slate-100 text-slate-600'
    }`}>{val}</span>;
    if (typeof val === 'number') return <span className="font-mono text-slate-700">{Number.isInteger(val) ? val.toLocaleString() : `₹${val.toFixed(2)}`}</span>;
    if (typeof val === 'string' && val.includes('T')) return <span className="text-slate-500">{fmtDate(val)}</span>;
    return <span className="text-slate-700">{String(val)}</span>;
  };

  // Compute totals for numeric columns
  const numericCols = data.length > 0 ? report.columns.filter(col => typeof data[0][col] === 'number') : [];

  return (
    <div className="space-y-4">
      {/* Panel header */}
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <button onClick={onClose} className="text-slate-400 hover:text-slate-600 transition-colors"><X className="w-4 h-4" /></button>
            <h3 className="text-base font-bold text-slate-800">{report.name}</h3>
            {report.isRestricted && <span className="px-2 py-0.5 bg-purple-100 text-purple-700 text-[10px] font-bold rounded-full border border-purple-200">ACCESS CONTROLLED</span>}
          </div>
          <p className="text-xs text-slate-400 ml-6 mt-0.5">{report.desc}</p>
        </div>
        <div className="flex gap-2 flex-shrink-0">
          <button onClick={() => doExportExcel(report, data)}
            className="flex items-center gap-1.5 px-3 py-1.5 border border-emerald-200 text-emerald-700 text-xs font-bold rounded-lg hover:bg-emerald-50 transition-colors">
            <FileSpreadsheet className="w-3.5 h-3.5" /> Excel
          </button>
          <button onClick={() => doExportPDF(report, data, filterStr)}
            className="flex items-center gap-1.5 px-3 py-1.5 border border-red-200 text-red-700 text-xs font-bold rounded-lg hover:bg-red-50 transition-colors">
            <FileText className="w-3.5 h-3.5" /> PDF
          </button>
          <button onClick={onSchedule}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white text-xs font-bold rounded-lg hover:bg-blue-700 transition-colors">
            <Bell className="w-3.5 h-3.5" /> Schedule
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-3 p-3 bg-slate-50 border border-slate-200 rounded-xl">
        {report.hasDateRange && (
          <>
            <div className="flex items-center gap-2">
              <Calendar className="w-3.5 h-3.5 text-slate-400" />
              <input type="date" value={from} onChange={e => setFrom(e.target.value)}
                className="px-2 py-1.5 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500/20" />
              <span className="text-xs text-slate-400 font-bold">to</span>
              <input type="date" value={to} onChange={e => setTo(e.target.value)}
                className="px-2 py-1.5 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500/20" />
            </div>
            <div className="flex gap-1">
              {[['Today','today'],['This Month','month'],['Last 7d','week']].map(([label, preset]) => (
                <button key={preset} onClick={() => {
                  const n = new Date();
                  if (preset === 'today') { setFrom(today); setTo(today); }
                  else if (preset === 'month') { setFrom(monthStart); setTo(today); }
                  else { setFrom(new Date(n.setDate(n.getDate()-7)).toISOString().split('T')[0]); setTo(today); }
                }} className="px-2 py-1 text-[10px] font-bold bg-white border border-slate-200 rounded hover:bg-blue-50 hover:text-blue-600 hover:border-blue-200 transition-colors">{label}</button>
              ))}
            </div>
          </>
        )}
        {(report.extraFilters || []).map(ef => (
          <div key={ef.key} className="flex items-center gap-2">
            <label className="text-[10px] font-bold text-slate-400 uppercase">{ef.label}:</label>
            <input type={ef.type} value={extraFilters[ef.key] ?? ef.default}
              onChange={e => setExtraFilters(p => ({ ...p, [ef.key]: e.target.value }))}
              className="w-20 px-2 py-1.5 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500/20" />
          </div>
        ))}
        <button onClick={generate} disabled={loading}
          className="px-4 py-1.5 bg-blue-600 text-white text-xs font-bold rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-1.5 disabled:opacity-50">
          <RefreshCcw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          {loading ? 'Generating…' : 'Generate Report'}
        </button>
        <span className="ml-auto text-xs text-slate-400 self-center">{data.length} records</span>
      </div>

      {/* Summary card (for summary reports) */}
      {summary && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {Object.entries(summary).map(([k, v]) => (
            <div key={k} className="p-3 bg-slate-50 border border-slate-100 rounded-xl">
              <div className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">{k.replace(/([A-Z])/g, ' $1').trim()}</div>
              <div className="text-sm font-bold text-slate-800">{typeof v === 'number' ? (Number.isInteger(v) ? v.toLocaleString() : `₹${Number(v).toFixed(2)}`) : String(v)}</div>
            </div>
          ))}
        </div>
      )}

      {/* Data Table */}
      {data.length > 0 && (
        <div className="overflow-auto max-h-[400px] border border-slate-100 rounded-xl">
          <table className="w-full text-xs">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 sticky top-0">
                {report.headers.map(h => (
                  <th key={h} className="text-left px-3 py-2.5 font-bold text-slate-500 uppercase tracking-wider whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.map((row, i) => (
                <tr key={i} className={`border-b border-slate-50 hover:bg-blue-50/30 transition-colors ${i % 2 === 0 ? '' : 'bg-slate-50/30'}`}>
                  {report.columns.map(col => (
                    <td key={col} className="px-3 py-2 whitespace-nowrap">{renderCell(row, col, col)}</td>
                  ))}
                </tr>
              ))}
            </tbody>
            {numericCols.length > 0 && (
              <tfoot>
                <tr className="bg-slate-100 border-t border-slate-200 font-bold">
                  {report.columns.map(col => (
                    <td key={col} className="px-3 py-2 text-xs font-bold text-slate-700">
                      {numericCols.includes(col)
                        ? `₹${data.reduce((s, r) => s + (Number(r[col]) || 0), 0).toFixed(2)}`
                        : col === report.columns[0] ? 'TOTAL' : ''}
                    </td>
                  ))}
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      )}

      {!loading && data.length === 0 && !summary && (
        <div className="text-center py-10 text-slate-400">
          <BarChart2 className="w-10 h-10 mx-auto mb-2 text-slate-200" />
          <p className="text-sm font-bold">No data for selected filters</p>
          <p className="text-xs mt-1">Adjust the date range or click Generate Report</p>
        </div>
      )}
    </div>
  );
}

// ─── REPORT CARD ──────────────────────────────────────────────────────────────
function ReportCard({ report, isFav, onToggleFav, onOpen, onSchedule }) {
  const catIcon = CATEGORIES.find(c => c.id === report.category);
  const CatIcon = catIcon?.icon || BarChart2;
  return (
    <div className={`bg-white rounded-xl border p-4 flex flex-col gap-3 hover:shadow-sm transition-all cursor-default ${
      report.isNarcotic ? 'border-purple-200 bg-purple-50/20' : report.isRestricted ? 'border-purple-100' : 'border-slate-100'
    }`}>
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-center gap-2.5">
          <div className={`w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 ${
            report.isNarcotic ? 'bg-purple-100' : report.isRestricted ? 'bg-purple-50' : 'bg-slate-50'
          }`}>
            <CatIcon className={`w-4 h-4 ${catIcon?.color || 'text-slate-500'}`} />
          </div>
          <div>
            <div className="text-sm font-bold text-slate-700 leading-tight">{report.name}</div>
            {report.isNarcotic && (
              <span className="text-[9px] font-bold text-purple-600 uppercase tracking-wider bg-purple-100 px-1.5 py-0.5 rounded mt-0.5 inline-block">NDPS Mandatory</span>
            )}
          </div>
        </div>
        <button onClick={() => onToggleFav(report.id)}
          className={`p-1 rounded transition-colors flex-shrink-0 ${isFav ? 'text-amber-400 hover:text-amber-500' : 'text-slate-200 hover:text-amber-300'}`}>
          <Star className="w-3.5 h-3.5" fill={isFav ? 'currentColor' : 'none'} />
        </button>
      </div>
      <p className="text-[11px] text-slate-400 leading-relaxed">{report.desc}</p>
      <div className="flex gap-2 mt-auto pt-1">
        <button onClick={() => onOpen(report)}
          className="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 bg-blue-600 text-white text-xs font-bold rounded-lg hover:bg-blue-700 transition-colors">
          <Eye className="w-3 h-3" /> Generate
        </button>
        <button onClick={() => onSchedule(report)}
          className="px-3 py-2 border border-slate-200 text-slate-500 text-xs font-bold rounded-lg hover:bg-slate-50 transition-colors">
          <Bell className="w-3 h-3" />
        </button>
      </div>
    </div>
  );
}

// ─── SCHEDULES TAB ────────────────────────────────────────────────────────────
function SchedulesTab() {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetch = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.api.get('/pharmacy/report-schedules');
      if (res.data?.success) setSchedules(res.data.data);
    } catch { toast.error('Failed to load schedules'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetch(); }, []);

  const toggle = async (id) => {
    try {
      await pharmacyService.api.patch(`/pharmacy/report-schedules/${id}/toggle`);
      toast.success('Schedule toggled');
      fetch();
    } catch { toast.error('Failed to toggle'); }
  };

  const del = async (id) => {
    if (!window.confirm('Delete this schedule?')) return;
    try {
      await pharmacyService.api.delete(`/pharmacy/report-schedules/${id}`);
      toast.success('Deleted');
      fetch();
    } catch { toast.error('Failed to delete'); }
  };

  const channelBadge = { EMAIL: '✉️ Email', WHATSAPP: '💬 WhatsApp', BOTH: '✉️+💬 Both' };
  const statusBadge = { SENT: 'bg-emerald-100 text-emerald-700', FAILED: 'bg-red-100 text-red-700', PENDING: 'bg-amber-100 text-amber-700' };

  return (
    <div className="space-y-4">
      {loading ? (
        <div className="flex justify-center p-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500" /></div>
      ) : schedules.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-xl border border-slate-100">
          <Bell className="w-10 h-10 text-slate-200 mx-auto mb-3" />
          <p className="text-sm font-bold text-slate-400">No scheduled reports yet</p>
          <p className="text-xs text-slate-300 mt-1">Click the <Bell className="inline w-3 h-3" /> icon on any report card to create a schedule</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-slate-100 overflow-hidden">
          <table className="w-full text-xs">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100">
                {['Schedule Name','Report','Frequency','Time','Channel','Format','Last Sent','Status','Active',''].map(h => (
                  <th key={h} className="text-left px-4 py-3 font-bold text-slate-400 uppercase tracking-wider whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {schedules.map(sc => (
                <tr key={sc.id} className="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
                  <td className="px-4 py-3 font-bold text-slate-700">{sc.scheduleName}</td>
                  <td className="px-4 py-3 text-slate-500">{sc.reportType?.replace(/-/g,' ')}</td>
                  <td className="px-4 py-3"><span className="bg-blue-50 text-blue-700 border border-blue-100 px-2 py-0.5 rounded-full text-[10px] font-bold">{sc.frequency}</span></td>
                  <td className="px-4 py-3 font-mono text-slate-500">{sc.deliveryTime}</td>
                  <td className="px-4 py-3 text-slate-600">{channelBadge[sc.channels] || sc.channels}</td>
                  <td className="px-4 py-3"><span className="bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full text-[10px] font-bold">{sc.fileFormats}</span></td>
                  <td className="px-4 py-3 text-slate-400">{sc.lastSentAt ? fmtDate(sc.lastSentAt) : '—'}</td>
                  <td className="px-4 py-3">
                    {sc.lastSentStatus ? (
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${statusBadge[sc.lastSentStatus] || 'bg-slate-100 text-slate-600'}`}>
                        {sc.lastSentStatus}
                      </span>
                    ) : <span className="text-slate-300">—</span>}
                  </td>
                  <td className="px-4 py-3">
                    <button onClick={() => toggle(sc.id)} className="transition-colors">
                      {sc.active
                        ? <ToggleRight className="w-5 h-5 text-emerald-500" />
                        : <ToggleLeft className="w-5 h-5 text-slate-300" />}
                    </button>
                  </td>
                  <td className="px-4 py-3">
                    <button onClick={() => del(sc.id)} className="p-1 text-red-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors">
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

import OTPVerificationModal from '../components/auth/OTPVerificationModal';

// ─── MAIN REPORTS PAGE ────────────────────────────────────────────────────────
export default function Reports() {
  const [activeCategory, setActiveCategory] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [favourites, setFavourites] = useState(() => {
    try { return JSON.parse(localStorage.getItem('rpt_favs') || '[]'); } catch { return []; }
  });
  const [openReport, setOpenReport] = useState(null);
  const [scheduleReport, setScheduleReport] = useState(null);
  const [recentReports] = useState([]);
  const [isOtpOpen, setIsOtpOpen] = useState(false);
  const [pendingReport, setPendingReport] = useState(null);
  const [verifiedEmail, setVerifiedEmail] = useState(false);

  const toggleFav = (id) => {
    setFavourites(prev => {
      const next = prev.includes(id) ? prev.filter(f => f !== id) : [...prev, id];
      localStorage.setItem('rpt_favs', JSON.stringify(next));
      return next;
    });
  };

  const handleOpenReport = (report) => {
    if (report.isRestricted && !verifiedEmail) {
      setPendingReport(report);
      setIsOtpOpen(true);
    } else {
      setOpenReport(report);
    }
  };

  const filteredReports = REPORT_CATALOG.filter(r => {
    const matchCat = activeCategory === 'all' || activeCategory === 'schedules' || r.category === activeCategory;
    const matchSearch = !searchTerm ||
      r.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.desc.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.category.toLowerCase().includes(searchTerm.toLowerCase());
    return matchCat && matchSearch;
  });

  const favReports = REPORT_CATALOG.filter(r => favourites.includes(r.id));

  const catGroups = activeCategory === 'all'
    ? CATEGORIES.filter(c => c.id !== 'all' && c.id !== 'schedules')
    : [CATEGORIES.find(c => c.id === activeCategory)].filter(Boolean);

  return (
    <div className="space-y-5">
      {/* OTP verification */}
      <OTPVerificationModal
        isOpen={isOtpOpen}
        onClose={() => { setIsOtpOpen(false); setPendingReport(null); }}
        onVerifySuccess={() => { setVerifiedEmail(true); setOpenReport(pendingReport); setPendingReport(null); }}
      />

      {/* Page header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Reports & Analytics</h2>
          <p className="text-sm text-slate-400 mt-0.5">
            {REPORT_CATALOG.length} reports across {CATEGORIES.length - 2} categories — sales, stock, procurement, GST, compliance &amp; clinical
          </p>
        </div>
      </div>

      {/* Global Search */}
      <div className="relative">
        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-300" />
        <input value={searchTerm} onChange={e => setSearchTerm(e.target.value)}
          placeholder="Search reports by name or keyword — e.g. 'GSTR-1', 'narcotic', 'expiry', 'payables'…"
          className="w-full pl-11 pr-4 py-3 text-sm border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-400 bg-white shadow-sm" />
        {searchTerm && (
          <button onClick={() => setSearchTerm('')} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Category Tabs */}
      <div className="flex gap-1 overflow-x-auto pb-1">
        {CATEGORIES.map(cat => {
          const CatIcon = cat.icon;
          return (
            <button key={cat.id} onClick={() => setActiveCategory(cat.id)}
              className={`flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all flex-shrink-0 ${
                activeCategory === cat.id
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-white border border-slate-200 text-slate-500 hover:text-slate-700 hover:border-slate-300'
              }`}>
              <CatIcon className="w-3.5 h-3.5" />{cat.label}
              {cat.id !== 'all' && cat.id !== 'schedules' && (
                <span className={`text-[10px] px-1 rounded ${activeCategory === cat.id ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-400'}`}>
                  {REPORT_CATALOG.filter(r => r.category === cat.id).length}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* Report Preview (when open) */}
      {openReport && (
        <div className="bg-white rounded-xl border border-blue-100 p-5">
          <ReportPreviewPanel
            report={openReport}
            onClose={() => setOpenReport(null)}
            onSchedule={() => { setScheduleReport(openReport); }}
          />
        </div>
      )}

      {/* Schedules Tab Content */}
      {activeCategory === 'schedules' && !openReport && <SchedulesTab />}

      {/* Favourites */}
      {activeCategory !== 'schedules' && favReports.length > 0 && !searchTerm && (
        <div className="space-y-3">
          <div className="flex items-center gap-2">
            <Star className="w-4 h-4 text-amber-400" fill="currentColor" />
            <h3 className="text-sm font-bold text-slate-700">Favourites</h3>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
            {favReports.map(r => (
              <ReportCard key={r.id} report={r}
                isFav={favourites.includes(r.id)}
                onToggleFav={toggleFav}
                onOpen={handleOpenReport}
                onSchedule={setScheduleReport} />
            ))}
          </div>
        </div>
      )}

      {/* Category Groups */}
      {activeCategory !== 'schedules' && catGroups.map(cat => {
        if (!cat) return null;
        const catReports = filteredReports.filter(r => r.category === cat.id);
        if (catReports.length === 0) return null;
        const CatIcon = cat.icon;
        return (
          <div key={cat.id} className="space-y-3">
            <div className="flex items-center gap-2 pt-1">
              <CatIcon className={`w-4 h-4 ${cat.color}`} />
              <h3 className="text-sm font-bold text-slate-700">{cat.label}</h3>
              <span className="text-[10px] text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full font-bold">{catReports.length} reports</span>
              {cat.id === 'compliance' && (
                <span className="text-[10px] text-purple-600 bg-purple-100 px-2 py-0.5 rounded-full font-bold border border-purple-200 flex items-center gap-1">
                  <Shield className="w-2.5 h-2.5" /> Role-restricted
                </span>
              )}
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
              {catReports.map(r => (
                <ReportCard key={r.id} report={r}
                  isFav={favourites.includes(r.id)}
                  onToggleFav={toggleFav}
                  onOpen={handleOpenReport}
                  onSchedule={setScheduleReport} />
              ))}
            </div>
          </div>
        );
      })}

      {/* Empty search state */}
      {searchTerm && filteredReports.length === 0 && (
        <div className="text-center py-16">
          <Search className="w-10 h-10 text-slate-200 mx-auto mb-3" />
          <p className="text-sm font-bold text-slate-400">No reports match "{searchTerm}"</p>
          <p className="text-xs text-slate-300 mt-1">Try 'sales', 'expiry', 'GSTR', 'narcotic', or 'supplier'</p>
        </div>
      )}

      {/* Schedule Drawer */}
      {scheduleReport && (
        <ScheduleDrawer
          report={scheduleReport}
          onClose={() => setScheduleReport(null)}
          onSaved={() => {}}
        />
      )}
    </div>
  );
}
