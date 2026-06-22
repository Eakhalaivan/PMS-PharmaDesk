import React, { useState, useEffect } from 'react';
import { CalendarX, RefreshCw, AlertTriangle, ArrowLeftRight, Trash2, ShieldAlert } from 'lucide-react';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';

export default function ExpiryTracker() {
  const [batches, setBatches] = useState([]);
  const [summary, setSummary] = useState(null);
  const [returns, setReturns] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [returnQty, setReturnQty] = useState('');
  const [selectedBatch, setSelectedBatch] = useState(null);
  const [remarks, setRemarks] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const bRes = await pharmacyService.getExpiryBatches();
      const sRes = await pharmacyService.getExpirySummary();
      const rRes = await pharmacyService.getExpiryReturns();
      
      setBatches(bRes.data || bRes || []);
      setSummary(sRes.data || sRes || null);
      setReturns(rRes.data || rRes || []);
    } catch (err) {
      toast.error('Failed to load expiry tracking data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleReturn = async (e) => {
    e.preventDefault();
    if (!selectedBatch || !returnQty || parseInt(returnQty) <= 0) {
      toast.error('Please enter a valid return quantity');
      return;
    }
    if (parseInt(returnQty) > selectedBatch.quantity) {
      toast.error('Return quantity exceeds batch quantity');
      return;
    }

    setSubmitting(true);
    try {
      const res = await pharmacyService.initiateExpiryReturn({
        batchId: selectedBatch.id,
        returnQuantity: parseInt(returnQty),
        remarks: remarks
      });
      if (res.success || res.id) {
        toast.success('Expiry return recorded successfully');
        setSelectedBatch(null);
        setReturnQty('');
        setRemarks('');
        loadData();
      } else {
        toast.error('Failed to record return');
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error recording return');
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusBadge = (batch) => {
    const days = batch.daysToExpiry;
    if (days < 0) {
      return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-100 text-red-700 border border-red-200">EXPIRED</span>;
    } else if (days <= 30) {
      return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-700 border border-amber-200">CRITICAL ({days}d)</span>;
    } else if (days <= 90) {
      return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-100 text-blue-700 border border-blue-200">WARNING ({days}d)</span>;
    }
    return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-700 border border-emerald-200">SAFE ({days}d)</span>;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Expiry & Batch Tracker</h2>
          <p className="text-sm text-slate-400">Track drug expiries, near-expiry alerts, and supplier return workflows.</p>
        </div>
        <button onClick={loadData} disabled={loading} className="p-2 border border-slate-200 rounded-xl hover:bg-slate-50 transition-colors">
          <RefreshCw className={`w-4 h-4 text-slate-600 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Summary Cards */}
      {summary && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="p-4 bg-white border border-slate-100 rounded-xl">
            <div className="text-[10px] font-bold text-slate-400 uppercase block mb-1">Total Batches</div>
            <div className="text-xl font-bold text-slate-800">{summary.totalBatches || 0}</div>
          </div>
          <div className="p-4 bg-white border border-red-100 rounded-xl bg-red-50/10">
            <div className="text-[10px] font-bold text-red-500 uppercase block mb-1">Expired Batches</div>
            <div className="text-xl font-bold text-red-700">{summary.expiredBatches || 0}</div>
          </div>
          <div className="p-4 bg-white border border-amber-100 rounded-xl bg-amber-50/10">
            <div className="text-[10px] font-bold text-amber-500 uppercase block mb-1">Near Expiry (≤ 30 Days)</div>
            <div className="text-xl font-bold text-amber-700">{summary.nearExpiry30Days || 0}</div>
          </div>
          <div className="p-4 bg-white border border-slate-100 rounded-xl">
            <div className="text-[10px] font-bold text-slate-400 uppercase block mb-1">Near Expiry (≤ 90 Days)</div>
            <div className="text-xl font-bold text-slate-800">{summary.nearExpiry90Days || 0}</div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Batches Table */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-100 overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-700">Batch Expiry Ledger</h3>
            <span className="text-xs text-slate-400">{batches.length} active batches</span>
          </div>
          <div className="overflow-auto max-h-[500px]">
            <table className="w-full text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 sticky top-0">
                  <th className="text-left px-4 py-3 font-bold text-slate-500 uppercase tracking-wider">Medicine</th>
                  <th className="text-left px-4 py-3 font-bold text-slate-500 uppercase tracking-wider">Batch No</th>
                  <th className="text-left px-4 py-3 font-bold text-slate-500 uppercase tracking-wider">Expiry</th>
                  <th className="text-right px-4 py-3 font-bold text-slate-500 uppercase tracking-wider">Stock Qty</th>
                  <th className="text-center px-4 py-3 font-bold text-slate-500 uppercase tracking-wider">Status</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody>
                {batches.map(batch => (
                  <tr key={batch.id} className="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
                    <td className="px-4 py-3 font-bold text-slate-700">
                      <div>{batch.medicineName}</div>
                      <div className="text-[10px] text-slate-400 font-mono">{batch.medicineCode}</div>
                    </td>
                    <td className="px-4 py-3 font-mono text-slate-600">{batch.batchNumber}</td>
                    <td className="px-4 py-3 text-slate-500">{batch.expiryDate}</td>
                    <td className="px-4 py-3 text-right font-bold text-slate-700">{batch.quantity}</td>
                    <td className="px-4 py-3 text-center">{getStatusBadge(batch)}</td>
                    <td className="px-4 py-3 text-right">
                      {batch.quantity > 0 && (
                        <button
                          onClick={() => setSelectedBatch(batch)}
                          className="px-2.5 py-1 bg-red-600 text-white text-[10px] font-bold rounded-lg hover:bg-red-700 transition-colors"
                        >
                          Return Supplier
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
                {batches.length === 0 && (
                  <tr>
                    <td colSpan="6" className="text-center py-10 text-slate-400">No batches currently tracked.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Return Forms & History */}
        <div className="space-y-6">
          {selectedBatch ? (
            <div className="bg-white rounded-xl border border-red-100 p-5 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold text-red-800 flex items-center gap-1.5">
                  <ShieldAlert className="w-4 h-4" /> Initiate Return
                </h3>
                <button onClick={() => setSelectedBatch(null)} className="text-xs text-slate-400 hover:text-slate-600">Cancel</button>
              </div>
              <div className="p-3 bg-red-50 border border-red-100 rounded-lg text-xs space-y-1">
                <div><span className="font-bold">Item:</span> {selectedBatch.medicineName}</div>
                <div><span className="font-bold">Batch:</span> {selectedBatch.batchNumber}</div>
                <div><span className="font-bold">Max Available Qty:</span> {selectedBatch.quantity}</div>
                <div><span className="font-bold">Supplier:</span> {selectedBatch.supplierName || 'N/A'}</div>
              </div>
              <form onSubmit={handleReturn} className="space-y-3">
                <div>
                  <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Return Quantity *</label>
                  <input
                    type="number"
                    min="1"
                    max={selectedBatch.quantity}
                    value={returnQty}
                    onChange={e => setReturnQty(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-red-500/20 focus:border-red-400 bg-white"
                  />
                </div>
                <div>
                  <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Reason / Remarks</label>
                  <textarea
                    rows="3"
                    value={remarks}
                    onChange={e => setRemarks(e.target.value)}
                    className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-red-500/20 focus:border-red-400 bg-white resize-none"
                    placeholder="Short expiry / Expired stock return..."
                  />
                </div>
                <button
                  type="submit"
                  disabled={submitting}
                  className="w-full py-2 bg-red-600 hover:bg-red-700 text-white text-xs font-bold rounded-lg transition-colors flex items-center justify-center gap-1.5 disabled:opacity-50"
                >
                  <ArrowLeftRight className="w-3.5 h-3.5" /> Submit Return Workflow
                </button>
              </form>
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-slate-100 p-5 text-center py-10">
              <CalendarX className="w-10 h-10 text-slate-300 mx-auto mb-2" />
              <p className="text-xs font-bold text-slate-400">Select a batch from the list to initiate a return to the supplier.</p>
            </div>
          )}

          {/* Returns History */}
          <div className="bg-white rounded-xl border border-slate-100 overflow-hidden">
            <div className="px-5 py-4 border-b border-slate-100">
              <h3 className="text-sm font-bold text-slate-700">Recent Returns</h3>
            </div>
            <div className="divide-y divide-slate-50 max-h-[300px] overflow-auto">
              {returns.map(ret => (
                <div key={ret.id} className="p-4 text-xs space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-slate-700">{ret.batchNumber}</span>
                    <span className="font-mono text-slate-400">{ret.returnDate}</span>
                  </div>
                  <div className="text-slate-500">Qty Returned: <span className="font-bold">{ret.returnQuantity}</span></div>
                  {ret.remarks && <div className="text-[10px] text-slate-400 italic">"{ret.remarks}"</div>}
                </div>
              ))}
              {returns.length === 0 && (
                <div className="p-4 text-center text-slate-400 text-xs">No returns recorded.</div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
