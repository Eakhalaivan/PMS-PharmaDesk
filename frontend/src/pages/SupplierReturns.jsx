import React, { useState, useEffect } from 'react';
import { ArrowLeft, Plus, RotateCcw, CheckCircle2, Truck, Trash2 } from 'lucide-react';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';

const RETURN_REASONS = ['Near Expiry', 'Damaged on Receipt', 'Wrong Item', 'Excess Stock'];
const STATUS_CONFIG = {
  INITIATED:             { cls: 'bg-slate-100 text-slate-600', label: 'Initiated' },
  DISPATCHED:            { cls: 'bg-blue-100 text-blue-700', label: 'Dispatched' },
  CREDIT_NOTE_RECEIVED:  { cls: 'bg-amber-100 text-amber-700', label: 'Credit Note Received' },
  SETTLED:               { cls: 'bg-emerald-100 text-emerald-700', label: 'Settled' },
};

export default function SupplierReturns({ onBack }) {
  const [returns, setReturns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [suppliers, setSuppliers] = useState([]);
  const [grns, setGrns] = useState([]);
  const [form, setForm] = useState({
    supplierId: '', grnId: '', reason: '', transportDetails: '', expectedCreditValue: '',
    items: [{ medicineName: '', batchNumber: '', returnQuantity: '', unitPrice: '' }]
  });

  useEffect(() => {
    fetchReturns();
    pharmacyService.getSuppliers().then(r => r.success && setSuppliers(r.data)).catch(() => {});
    pharmacyService.getGrns().then(r => r.success && setGrns(r.data)).catch(() => {});
  }, []);

  const fetchReturns = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getSupplierReturns();
      if (res.success) setReturns(res.data);
    } catch { toast.error('Failed to load returns'); }
    finally { setLoading(false); }
  };

  const setItem = (idx, field, val) => {
    setForm(prev => ({
      ...prev,
      items: prev.items.map((it, i) => i === idx ? { ...it, [field]: val } : it)
    }));
  };

  const addItem = () => setForm(prev => ({
    ...prev,
    items: [...prev.items, { medicineName: '', batchNumber: '', returnQuantity: '', unitPrice: '' }]
  }));

  const removeItem = (idx) => setForm(prev => ({
    ...prev,
    items: prev.items.filter((_, i) => i !== idx)
  }));

  const handleCreate = async () => {
    if (!form.supplierId) { toast.error('Select a supplier'); return; }
    if (!form.grnId) { toast.error('Select a GRN to link'); return; }

    try {
      const payload = {
        supplier: { id: parseInt(form.supplierId) },
        goodsReceiptNote: { id: parseInt(form.grnId) },
        reason: form.reason,
        transportDetails: form.transportDetails,
        expectedCreditValue: parseFloat(form.expectedCreditValue) || null,
        status: 'INITIATED',
        items: form.items.map(it => ({
          batchNumber: it.batchNumber,
          returnQuantity: parseInt(it.returnQuantity) || 0,
          unitPrice: parseFloat(it.unitPrice) || null,
          lineTotal: (parseInt(it.returnQuantity) || 0) * (parseFloat(it.unitPrice) || 0)
        }))
      };

      const res = await pharmacyService.createSupplierReturn(payload);
      if (res.success) {
        toast.success('Return initiated and stock deducted');
        setShowCreate(false);
        fetchReturns();
      }
    } catch { toast.error('Failed to create return'); }
  };

  const handleStatusUpdate = async (id, status) => {
    const creditNote = status === 'CREDIT_NOTE_RECEIVED'
      ? window.prompt('Enter credit note number:')
      : null;
    try {
      const res = await pharmacyService.updateSupplierReturnStatus(id, status, creditNote);
      if (res.success) { toast.success('Status updated'); fetchReturns(); }
    } catch { toast.error('Failed to update'); }
  };

  const inputCls = "w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-400 bg-white";

  const nextStatus = { INITIATED: 'DISPATCHED', DISPATCHED: 'CREDIT_NOTE_RECEIVED', CREDIT_NOTE_RECEIVED: 'SETTLED' };

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <button onClick={onBack} className="p-1.5 hover:bg-slate-100 rounded-lg transition-colors">
          <ArrowLeft className="w-4 h-4 text-slate-500" />
        </button>
        <div className="flex-1">
          <h2 className="text-xl font-bold text-slate-800">Returns to Supplier</h2>
          <p className="text-xs text-slate-400">Track near-expiry, damaged, or excess stock returns and credit notes</p>
        </div>
        <button onClick={() => setShowCreate(!showCreate)}
          className="flex items-center gap-1.5 px-4 py-2 bg-amber-500 text-white text-sm font-bold rounded-xl hover:bg-amber-600 transition-all">
          <Plus className="w-4 h-4" /> Initiate Return
        </button>
      </div>

      {/* Create Return Form */}
      {showCreate && (
        <div className="bg-white rounded-xl border border-amber-100 p-5 space-y-4">
          <div className="text-sm font-bold text-slate-700">New Return to Supplier</div>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Supplier *</label>
              <select className={inputCls} value={form.supplierId} onChange={e => setForm(p => ({...p, supplierId: e.target.value}))}>
                <option value="">Select supplier</option>
                {suppliers.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Link GRN *</label>
              <select className={inputCls} value={form.grnId} onChange={e => setForm(p => ({...p, grnId: e.target.value}))}>
                <option value="">Select GRN</option>
                {grns.filter(g => g.status === 'CONFIRMED').map(g => <option key={g.id} value={g.id}>{g.grnNumber}</option>)}
              </select>
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Reason</label>
              <select className={inputCls} value={form.reason} onChange={e => setForm(p => ({...p, reason: e.target.value}))}>
                <option value="">Select reason</option>
                {RETURN_REASONS.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Expected Credit Value (₹)</label>
              <input type="number" className={inputCls} value={form.expectedCreditValue} onChange={e => setForm(p => ({...p, expectedCreditValue: e.target.value}))} placeholder="0.00" />
            </div>
            <div className="md:col-span-2">
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Transport / Courier Details</label>
              <input className={inputCls} value={form.transportDetails} onChange={e => setForm(p => ({...p, transportDetails: e.target.value}))} placeholder="Courier company, tracking number" />
            </div>
          </div>

          {/* Return Items */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Items to Return</div>
              <button onClick={addItem} className="text-xs font-bold text-amber-600 hover:text-amber-700 flex items-center gap-1">
                <Plus className="w-3 h-3" /> Add Item
              </button>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-100">
                    {['Medicine / Description','Batch Number','Return Qty','Unit Price (₹)',''].map(h => (
                      <th key={h} className="text-left px-3 py-2 text-[9px] font-bold text-slate-400 uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {form.items.map((it, idx) => (
                    <tr key={idx} className="border-b border-slate-50">
                      <td className="px-3 py-2">
                        <input className="w-full px-2 py-1.5 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-amber-500/20"
                          value={it.medicineName} onChange={e => setItem(idx, 'medicineName', e.target.value)} placeholder="Medicine name" />
                      </td>
                      <td className="px-3 py-2">
                        <input className="w-28 px-2 py-1.5 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-amber-500/20 font-mono"
                          value={it.batchNumber} onChange={e => setItem(idx, 'batchNumber', e.target.value)} placeholder="BTH001" />
                      </td>
                      <td className="px-3 py-2">
                        <input type="number" min="0" className="w-20 px-2 py-1.5 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-amber-500/20"
                          value={it.returnQuantity} onChange={e => setItem(idx, 'returnQuantity', e.target.value)} />
                      </td>
                      <td className="px-3 py-2">
                        <input type="number" min="0" className="w-24 px-2 py-1.5 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-amber-500/20"
                          value={it.unitPrice} onChange={e => setItem(idx, 'unitPrice', e.target.value)} />
                      </td>
                      <td className="px-3 py-2">
                        {form.items.length > 1 && (
                          <button onClick={() => removeItem(idx)} className="p-1 text-red-400 hover:text-red-600 hover:bg-red-50 rounded">
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="flex gap-3">
            <button onClick={() => setShowCreate(false)} className="px-4 py-2 border border-slate-200 text-sm font-bold text-slate-600 rounded-lg hover:bg-slate-50 transition-all">Cancel</button>
            <button onClick={handleCreate} className="px-5 py-2 bg-amber-500 text-white text-sm font-bold rounded-lg hover:bg-amber-600 transition-all flex items-center gap-2">
              <RotateCcw className="w-4 h-4" /> Initiate Return
            </button>
          </div>
        </div>
      )}

      {/* Returns List */}
      <div className="bg-white rounded-xl border border-slate-100 overflow-hidden">
        {loading ? (
          <div className="p-8 flex justify-center"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-amber-500" /></div>
        ) : returns.length === 0 ? (
          <div className="p-12 text-center">
            <RotateCcw className="w-10 h-10 text-slate-200 mx-auto mb-3" />
            <p className="text-sm font-bold text-slate-400">No returns initiated</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-100">
                  {['Return No.','Supplier','Linked GRN','Reason','Expected Credit','Status','Credit Note','Actions'].map(h => (
                    <th key={h} className="text-left px-4 py-3 text-[10px] font-bold text-slate-400 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {returns.map(r => {
                  const sc = STATUS_CONFIG[r.status] || STATUS_CONFIG.INITIATED;
                  const next = nextStatus[r.status];
                  return (
                    <tr key={r.id} className="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
                      <td className="px-4 py-3 text-xs font-mono font-bold text-slate-600">{r.returnNumber}</td>
                      <td className="px-4 py-3 text-xs font-bold text-slate-700">{r.supplier?.name || '—'}</td>
                      <td className="px-4 py-3 text-xs font-mono text-blue-500">{r.goodsReceiptNote?.grnNumber || '—'}</td>
                      <td className="px-4 py-3 text-xs text-slate-600">{r.reason || '—'}</td>
                      <td className="px-4 py-3 text-xs font-bold text-slate-700">
                        {r.expectedCreditValue ? `₹${Number(r.expectedCreditValue).toLocaleString()}` : '—'}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${sc.cls}`}>{sc.label}</span>
                      </td>
                      <td className="px-4 py-3 text-xs font-mono text-slate-500">{r.creditNoteNumber || '—'}</td>
                      <td className="px-4 py-3">
                        {next && (
                          <button onClick={() => handleStatusUpdate(r.id, next)}
                            className="px-2.5 py-1 bg-slate-100 text-slate-600 text-[10px] font-bold rounded-lg hover:bg-slate-200 transition-colors whitespace-nowrap">
                            → {STATUS_CONFIG[next]?.label}
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
