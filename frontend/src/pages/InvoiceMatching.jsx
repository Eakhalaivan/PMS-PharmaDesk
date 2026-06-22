import React, { useState, useEffect } from 'react';
import { ArrowLeft, Search, CheckCircle2, AlertCircle, XCircle, Info, Zap, Plus } from 'lucide-react';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';

const SEVERITY_CONFIG = {
  BLOCK:    { cls: 'bg-red-50 text-red-700 border border-red-200', icon: XCircle, label: 'BLOCK – Must resolve' },
  WARNING:  { cls: 'bg-amber-50 text-amber-700 border border-amber-200', icon: AlertCircle, label: 'WARNING – Supervisor approval required' },
  ADVISORY: { cls: 'bg-blue-50 text-blue-700 border border-blue-200', icon: Info, label: 'ADVISORY – Note only' },
  NONE:     { cls: 'bg-emerald-50 text-emerald-700 border border-emerald-200', icon: CheckCircle2, label: 'Matched' },
};

const STATUS_CONFIG = {
  PENDING_MATCH: { cls: 'bg-slate-100 text-slate-600', label: 'Pending Match' },
  MATCHED:       { cls: 'bg-emerald-100 text-emerald-700', label: 'Matched' },
  DISPUTED:      { cls: 'bg-red-100 text-red-700', label: 'Disputed' },
  PAID:          { cls: 'bg-violet-100 text-violet-700', label: 'Paid' },
};

export default function InvoiceMatching({ onBack }) {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [matching, setMatching] = useState(false);
  const [showCreate, setShowCreate] = useState(false);
  const [suppliers, setSuppliers] = useState([]);
  const [grns, setGrns] = useState([]);
  const [form, setForm] = useState({ supplierId: '', grnId: '', invoiceNumber: '', invoiceDate: '', gstinOnInvoice: '', totalAmount: '' });

  useEffect(() => {
    fetchInvoices();
    pharmacyService.getSuppliers().then(r => r.success && setSuppliers(r.data)).catch(() => {});
    pharmacyService.getGrns().then(r => r.success && setGrns(r.data)).catch(() => {});
  }, []);

  const fetchInvoices = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getSupplierInvoices();
      if (res.success) setInvoices(res.data);
    } catch { toast.error('Failed to load invoices'); }
    finally { setLoading(false); }
  };

  const handleMatch = async (invoiceId) => {
    setMatching(true);
    try {
      const res = await pharmacyService.matchSupplierInvoice(invoiceId);
      if (res.success) {
        toast.success('3-way matching completed');
        setSelected(res.data);
        fetchInvoices();
      }
    } catch { toast.error('Matching failed'); }
    finally { setMatching(false); }
  };

  const handleStatusUpdate = async (id, status) => {
    try {
      const res = await pharmacyService.updateSupplierInvoiceStatus(id, status);
      if (res.success) {
        toast.success(`Status updated to ${status}`);
        setSelected(res.data);
        fetchInvoices();
      }
    } catch { toast.error('Failed to update status'); }
  };

  const handleCreateInvoice = async () => {
    if (!form.supplierId || !form.invoiceNumber) {
      toast.error('Supplier and invoice number required');
      return;
    }
    try {
      const selectedGrn = grns.find(g => g.id === parseInt(form.grnId));
      const payload = {
        supplier: { id: parseInt(form.supplierId) },
        goodsReceiptNote: form.grnId ? { id: parseInt(form.grnId) } : null,
        purchaseOrder: selectedGrn?.purchaseOrder ? { id: selectedGrn.purchaseOrder.id } : null,
        invoiceNumber: form.invoiceNumber,
        invoiceDate: form.invoiceDate || null,
        gstinOnInvoice: form.gstinOnInvoice,
        totalAmount: parseFloat(form.totalAmount) || null,
        status: 'PENDING_MATCH'
      };
      const res = await pharmacyService.createSupplierInvoice(payload);
      if (res.success) {
        toast.success('Invoice created');
        setShowCreate(false);
        setForm({ supplierId: '', grnId: '', invoiceNumber: '', invoiceDate: '', gstinOnInvoice: '', totalAmount: '' });
        fetchInvoices();
      }
    } catch { toast.error('Failed to create invoice'); }
  };

  const inputCls = "w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-400 bg-white";

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <button onClick={onBack} className="p-1.5 hover:bg-slate-100 rounded-lg transition-colors">
          <ArrowLeft className="w-4 h-4 text-slate-500" />
        </button>
        <div className="flex-1">
          <h2 className="text-xl font-bold text-slate-800">Invoice Matching (3-Way)</h2>
          <p className="text-xs text-slate-400">Match PO → GRN → Supplier Invoice before releasing payment</p>
        </div>
        <button onClick={() => setShowCreate(true)}
          className="flex items-center gap-1.5 px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-xl hover:bg-blue-700 transition-all">
          <Plus className="w-4 h-4" /> Record Invoice
        </button>
      </div>

      {/* Create Invoice Panel */}
      {showCreate && (
        <div className="bg-white rounded-xl border border-blue-100 p-5 space-y-4">
          <div className="text-sm font-bold text-slate-700">Record New Supplier Invoice</div>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Supplier *</label>
              <select className={inputCls} value={form.supplierId} onChange={e => setForm(p => ({...p, supplierId: e.target.value}))}>
                <option value="">Select supplier</option>
                {suppliers.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Link GRN</label>
              <select className={inputCls} value={form.grnId} onChange={e => setForm(p => ({...p, grnId: e.target.value}))}>
                <option value="">Select GRN</option>
                {grns.map(g => <option key={g.id} value={g.id}>{g.grnNumber}</option>)}
              </select>
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Invoice Number *</label>
              <input className={inputCls} value={form.invoiceNumber} onChange={e => setForm(p => ({...p, invoiceNumber: e.target.value}))} placeholder="INV-2024-001" />
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Invoice Date</label>
              <input type="date" className={inputCls} value={form.invoiceDate} onChange={e => setForm(p => ({...p, invoiceDate: e.target.value}))} />
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">GSTIN on Invoice</label>
              <input className={`${inputCls} font-mono`} value={form.gstinOnInvoice} onChange={e => setForm(p => ({...p, gstinOnInvoice: e.target.value}))} placeholder="22AAAAA0000A1Z5" />
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Total Amount (₹)</label>
              <input type="number" className={inputCls} value={form.totalAmount} onChange={e => setForm(p => ({...p, totalAmount: e.target.value}))} placeholder="0.00" />
            </div>
          </div>
          <div className="flex gap-3">
            <button onClick={() => setShowCreate(false)} className="px-4 py-2 border border-slate-200 text-sm font-bold text-slate-600 rounded-lg hover:bg-slate-50 transition-all">Cancel</button>
            <button onClick={handleCreateInvoice} className="px-5 py-2 bg-blue-600 text-white text-sm font-bold rounded-lg hover:bg-blue-700 transition-all">Create Invoice</button>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Invoice List */}
        <div className="bg-white rounded-xl border border-slate-100 overflow-hidden">
          <div className="px-4 py-3 border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-wider">
            Invoices ({invoices.length})
          </div>
          {loading ? (
            <div className="p-8 flex justify-center"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500" /></div>
          ) : invoices.length === 0 ? (
            <div className="p-8 text-center text-sm text-slate-400">No invoices recorded</div>
          ) : (
            <div className="divide-y divide-slate-50">
              {invoices.map(inv => {
                const sc = STATUS_CONFIG[inv.status] || STATUS_CONFIG.PENDING_MATCH;
                return (
                  <div key={inv.id} onClick={() => setSelected(inv)}
                    className={`p-4 cursor-pointer hover:bg-slate-50/70 transition-colors ${selected?.id === inv.id ? 'bg-blue-50 border-l-2 border-blue-500' : ''}`}>
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="text-sm font-bold text-slate-700">{inv.invoiceNumber}</div>
                        <div className="text-xs text-slate-400 mt-0.5">{inv.supplier?.name}</div>
                        {inv.goodsReceiptNote && (
                          <div className="text-[10px] text-blue-500 font-mono mt-0.5">GRN: {inv.goodsReceiptNote.grnNumber}</div>
                        )}
                      </div>
                      <div className="text-right">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${sc.cls}`}>{sc.label}</span>
                        {inv.totalAmount && (
                          <div className="text-xs font-bold text-slate-700 mt-1">₹{Number(inv.totalAmount).toLocaleString()}</div>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Invoice Detail / Matching Panel */}
        <div className="bg-white rounded-xl border border-slate-100 overflow-hidden">
          {!selected ? (
            <div className="p-12 text-center text-slate-400">
              <Zap className="w-10 h-10 mx-auto mb-3 text-slate-200" />
              <p className="text-sm font-bold">Select an invoice to match</p>
            </div>
          ) : (
            <div>
              <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
                <div>
                  <div className="text-sm font-bold text-slate-700">{selected.invoiceNumber}</div>
                  <div className="text-xs text-slate-400">{selected.supplier?.name}</div>
                </div>
                <div className="flex gap-2">
                  {selected.status === 'PENDING_MATCH' && (
                    <button onClick={() => handleMatch(selected.id)} disabled={matching}
                      className="px-3 py-1.5 bg-blue-600 text-white text-xs font-bold rounded-lg hover:bg-blue-700 transition-all flex items-center gap-1.5 disabled:opacity-50">
                      <Zap className="w-3 h-3" /> {matching ? 'Matching…' : 'Run 3-Way Match'}
                    </button>
                  )}
                  {selected.status === 'MATCHED' && (
                    <button onClick={() => handleStatusUpdate(selected.id, 'PAID')}
                      className="px-3 py-1.5 bg-violet-600 text-white text-xs font-bold rounded-lg hover:bg-violet-700 transition-all">
                      Mark as Paid
                    </button>
                  )}
                  {selected.status === 'DISPUTED' && (
                    <button onClick={() => handleStatusUpdate(selected.id, 'MATCHED')}
                      className="px-3 py-1.5 bg-emerald-600 text-white text-xs font-bold rounded-lg hover:bg-emerald-700 transition-all">
                      Override & Match
                    </button>
                  )}
                </div>
              </div>

              {/* Match notes */}
              {selected.matchNotes && (
                <div className="mx-5 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700 space-y-1">
                  <div className="font-bold">Match Issues Detected:</div>
                  {selected.matchNotes.split('\n').map((note, i) => (
                    <div key={i} className="flex items-start gap-1.5"><AlertCircle className="w-3 h-3 flex-shrink-0 mt-0.5" />{note}</div>
                  ))}
                </div>
              )}

              {/* Invoice Summary */}
              <div className="px-5 py-3 space-y-2">
                <div className="grid grid-cols-2 gap-3">
                  {[
                    ['Invoice Date', selected.invoiceDate || '—'],
                    ['GSTIN on Invoice', selected.gstinOnInvoice || '—'],
                    ['Total Amount', selected.totalAmount ? `₹${Number(selected.totalAmount).toLocaleString()}` : '—'],
                    ['Status', (STATUS_CONFIG[selected.status] || STATUS_CONFIG.PENDING_MATCH).label],
                  ].map(([k, v]) => (
                    <div key={k} className="p-2.5 bg-slate-50 rounded-lg">
                      <div className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">{k}</div>
                      <div className="text-xs font-bold text-slate-700 mt-0.5">{v}</div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Line Item Discrepancies */}
              {selected.items?.length > 0 && (
                <div className="px-5 pb-5">
                  <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">Line Item Variances</div>
                  <div className="space-y-2">
                    {selected.items.map((item, i) => {
                      const sev = SEVERITY_CONFIG[item.discrepancyType === 'NONE' ? 'NONE' : (item.discrepancySeverity || 'NONE')];
                      const SevIcon = sev?.icon || CheckCircle2;
                      return (
                        <div key={i} className={`flex items-center gap-3 p-3 rounded-lg ${sev?.cls}`}>
                          <SevIcon className="w-4 h-4 flex-shrink-0" />
                          <div className="flex-1">
                            <div className="text-xs font-bold">{item.medicine?.name || 'Unknown medicine'}</div>
                            <div className="text-[10px]">Billed: {item.billedQuantity} × ₹{item.billedPrice}</div>
                          </div>
                          {item.discrepancyType && item.discrepancyType !== 'NONE' && (
                            <span className="text-[10px] font-bold uppercase">{item.discrepancyType?.replace('_', ' ')}</span>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {selected.items?.length === 0 && (
                <div className="px-5 pb-5 text-xs text-slate-400 text-center py-6">
                  Run 3-way matching to see item-level variances
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
