import React, { useState, useEffect } from 'react';
import { Search, ChevronDown, ChevronRight, AlertTriangle, ShieldAlert, Package, CheckCircle, Printer, Download, Plus, RotateCcw, Box } from 'lucide-react';
import ModuleFilterBar from '../components/ui/ModuleFilterBar';
import Pagination from '../components/ui/Pagination';
import Badge from '../components/ui/Badge';
import AppModal from '../components/ui/AppModal';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';
import { cn } from '../utils/cn';

export default function MedicineStock() {
  const [stocks, setStocks] = useState([]);
  const [medicines, setMedicines] = useState([]);
  const [valuation, setValuation] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Table state
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedMeds, setExpandedMeds] = useState(new Set());
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 15;

  // Modals
  const [isAdjustModalOpen, setIsAdjustModalOpen] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState(null);
  const [adjustForm, setAdjustForm] = useState({ quantity: 0, reason: 'Physical Count Correction', remarks: '' });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [stockRes, valRes, medRes] = await Promise.all([
        pharmacyService.getAllStocks(),
        pharmacyService.api.get('/pharmacy/stocks/valuation'),
        pharmacyService.getMedicines()
      ]);
      if (stockRes.success) setStocks(stockRes.data);
      if (valRes.data.success) setValuation(valRes.data.data);
      if (medRes.success) setMedicines(medRes.data);
    } catch (error) {
      toast.error('Failed to load stock data');
    } finally {
      setLoading(false);
    }
  };

  const toggleExpand = (medId) => {
    const newSet = new Set(expandedMeds);
    if (newSet.has(medId)) newSet.delete(medId);
    else newSet.add(medId);
    setExpandedMeds(newSet);
  };

  const handleAdjustSubmit = async () => {
    if (adjustForm.quantity === 0) return toast.error('Adjust quantity cannot be 0');
    try {
      await pharmacyService.api.post('/pharmacy/stocks/adjust', {
        medicineStock: { id: selectedBatch.id },
        adjustedQuantity: adjustForm.quantity,
        reason: adjustForm.reason,
        remarks: adjustForm.remarks
      });
      toast.success('Stock adjusted successfully');
      setIsAdjustModalOpen(false);
      fetchData();
    } catch (error) {
      toast.error('Adjustment failed');
    }
  };

  const runAutoPO = async () => {
    try {
      const res = await pharmacyService.api.post('/pharmacy/purchase-orders/auto-generate');
      toast.success(res.data.message || 'Auto POs generated');
    } catch (e) {
      toast.error('Failed to generate POs');
    }
  };

  // Group stocks by medicine
  const groupedStocks = {};
  stocks.forEach(s => {
    if (!s.medicine) return;
    const mId = s.medicine.id;
    if (!groupedStocks[mId]) {
      groupedStocks[mId] = { medicine: s.medicine, batches: [], totalQty: 0 };
    }
    groupedStocks[mId].batches.push(s);
    groupedStocks[mId].totalQty += (s.quantityAvailable || 0);
  });

  // Convert to array and filter
  let medicineGroups = Object.values(groupedStocks).filter(group => {
    const s = searchTerm.toLowerCase();
    return !searchTerm || 
      group.medicine.name?.toLowerCase().includes(s) || 
      group.medicine.medicineCode?.toLowerCase().includes(s);
  });

  // Calculate Expiry Status dynamically
  const getExpiryStatus = (expiryDate) => {
    if (!expiryDate) return { label: 'Valid', color: 'success', days: 999 };
    const days = Math.ceil((new Date(expiryDate) - new Date()) / (1000 * 60 * 60 * 24));
    if (days < 0) return { label: 'Expired', color: 'danger', days };
    if (days <= 15) return { label: 'Critical', color: 'danger', days };
    if (days <= 30) return { label: 'Near Expiry', color: 'warning', days };
    if (days <= 60) return { label: 'Expiring Soon', color: 'warning', days };
    return { label: 'Healthy', color: 'success', days };
  };

  const paginatedGroups = medicineGroups.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-end">
        <div className="flex flex-col gap-1">
          <h2 className="text-2xl font-medium tracking-tight text-slate-900">Stock Management</h2>
          <p className="text-sm text-slate-500 font-normal">Batch-level inventory tracking, auto-replenishment, and expiry monitoring.</p>
        </div>
        <div className="flex gap-3">
          <button onClick={runAutoPO} className="px-4 py-2 bg-slate-900 text-white rounded-md text-sm font-medium hover:bg-slate-800 flex items-center gap-2">
            <Package className="w-4 h-4" /> Run Auto-Reorder Check
          </button>
        </div>
      </div>

      {/* VALUATION KPIs */}
      <div className="grid grid-cols-4 gap-4">
        <div className="bg-white border border-slate-200 rounded-lg p-4 shadow-sm">
          <p className="text-[10px] font-medium text-slate-500 uppercase tracking-wider mb-1">Total Stock Value (Cost)</p>
          <p className="text-2xl font-medium text-slate-900">₹{valuation?.totalPurchaseValue?.toLocaleString() || '0.00'}</p>
        </div>
        <div className="bg-white border border-slate-200 rounded-lg p-4 shadow-sm">
          <p className="text-[10px] font-medium text-slate-500 uppercase tracking-wider mb-1">Total Stock Value (MRP)</p>
          <p className="text-2xl font-medium text-slate-900">₹{valuation?.totalMrpValue?.toLocaleString() || '0.00'}</p>
        </div>
        <div className="bg-white border border-amber-200 bg-amber-50/30 rounded-lg p-4 shadow-sm">
          <p className="text-[10px] font-medium text-amber-700 uppercase tracking-wider mb-1">Near Expiry Risk (&lt;30d)</p>
          <p className="text-2xl font-medium text-amber-600">₹{valuation?.nearExpiryValue?.toLocaleString() || '0.00'}</p>
        </div>
        <div className="bg-white border border-red-200 bg-red-50/30 rounded-lg p-4 shadow-sm">
          <p className="text-[10px] font-medium text-red-700 uppercase tracking-wider mb-1">Expired Value (Write Off)</p>
          <p className="text-2xl font-medium text-red-600">₹{valuation?.expiredValue?.toLocaleString() || '0.00'}</p>
        </div>
      </div>

      <div className="bg-white p-4 rounded-lg border border-slate-200 flex gap-4 items-center shadow-sm">
        <div className="flex-1 max-w-sm">
          <ModuleFilterBar onSearch={setSearchTerm} searchValue={searchTerm} hideDateRange={true} />
        </div>
        <button className="px-4 py-2 border border-slate-200 text-slate-700 rounded-md text-sm font-medium hover:bg-slate-50 flex items-center gap-2">
          <Download className="w-4 h-4" /> Export Report
        </button>
      </div>

      {/* BATCH LIST VIEW */}
      <div className="bg-white border border-slate-200 rounded-lg overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-8 text-center text-slate-500">Loading stock ledgers...</div>
        ) : (
          <div className="w-full">
            {/* Header */}
            <div className="grid grid-cols-12 gap-4 p-4 border-b border-slate-200 bg-slate-50 text-xs font-medium text-slate-500 uppercase tracking-wider">
              <div className="col-span-4">Medicine / Code</div>
              <div className="col-span-2">Drug Class</div>
              <div className="col-span-2 text-right">Reorder Level</div>
              <div className="col-span-2 text-right">Total Available</div>
              <div className="col-span-2 text-center">Status</div>
            </div>

            {/* Rows */}
            {paginatedGroups.map(group => {
              const med = group.medicine;
              const isExpanded = expandedMeds.has(med.id);
              const reorderLvl = med.reorderLevel || 0;
              const isLowStock = group.totalQty <= reorderLvl;
              const healthPct = reorderLvl === 0 ? 100 : Math.min(100, (group.totalQty / reorderLvl) * 50); // 50% width means at reorder level
              
              // Sort batches by Expiry (FEFO)
              const sortedBatches = [...group.batches].sort((a,b) => new Date(a.expiryDate) - new Date(b.expiryDate));

              return (
                <React.Fragment key={med.id}>
                  {/* Medicine Row */}
                  <div className={cn("grid grid-cols-12 gap-4 p-4 border-b border-slate-100 hover:bg-slate-50/80 transition-colors items-center cursor-pointer", isLowStock ? "bg-red-50/20" : "")} onClick={() => toggleExpand(med.id)}>
                    <div className="col-span-4 flex items-center gap-3">
                      <button className="p-1 text-slate-400 hover:text-slate-700 hover:bg-slate-200 rounded">
                        {isExpanded ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
                      </button>
                      <div>
                        <div className="font-medium text-slate-900">{med.name}</div>
                        <div className="text-xs text-slate-500 font-mono">{med.medicineCode}</div>
                      </div>
                    </div>
                    <div className="col-span-2 text-sm text-slate-700">{med.drugClass || '-'}</div>
                    <div className="col-span-2 text-right text-sm text-slate-500">{reorderLvl} {med.unit}</div>
                    <div className="col-span-2 text-right font-medium">
                      <span className={cn(isLowStock ? "text-red-600" : "text-slate-900")}>{group.totalQty} {med.unit}</span>
                      {/* Health Indicator Bar */}
                      <div className="w-full bg-slate-100 h-1.5 rounded-full mt-1.5 overflow-hidden flex">
                        <div className={cn("h-full", isLowStock ? "bg-red-500" : "bg-emerald-500")} style={{ width: `${healthPct}%` }} />
                      </div>
                    </div>
                    <div className="col-span-2 text-center">
                      {isLowStock ? (
                        <Badge variant="danger">Low Stock (Auto-PO)</Badge>
                      ) : (
                        <Badge variant="success">Healthy</Badge>
                      )}
                    </div>
                  </div>

                  {/* Expanded Batches */}
                  {isExpanded && (
                    <div className="col-span-12 bg-slate-50/80 border-b border-slate-200 px-12 py-4">
                      <table className="w-full text-sm text-left">
                        <thead className="text-xs text-slate-500 uppercase tracking-wider border-b border-slate-200/60">
                          <tr>
                            <th className="pb-3 font-medium">Batch No.</th>
                            <th className="pb-3 font-medium">Expiry Date</th>
                            <th className="pb-3 font-medium text-right">Available Qty</th>
                            <th className="pb-3 font-medium text-right">Purchase / MRP</th>
                            <th className="pb-3 font-medium">Status</th>
                            <th className="pb-3 font-medium text-right">Actions</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {sortedBatches.map(b => {
                            const exp = getExpiryStatus(b.expiryDate);
                            return (
                              <tr key={b.id} className="hover:bg-white transition-colors">
                                <td className="py-3 font-mono text-slate-700">{b.batchNumber}</td>
                                <td className="py-3 text-slate-700 flex items-center gap-2">
                                  {b.expiryDate} 
                                  <span className={cn("text-[10px] font-medium px-1.5 py-0.5 rounded", 
                                    exp.color === 'danger' ? 'bg-red-100 text-red-700' : 
                                    exp.color === 'warning' ? 'bg-amber-100 text-amber-700' : 'text-slate-400'
                                  )}>{exp.days}d</span>
                                </td>
                                <td className="py-3 text-right font-medium">{b.quantityAvailable}</td>
                                <td className="py-3 text-right">₹{b.purchaseRate} / ₹{b.sellingRate}</td>
                                <td className="py-3">
                                  <Badge variant={exp.color}>{exp.label}</Badge>
                                </td>
                                <td className="py-3 text-right space-x-2">
                                  <button onClick={() => { setSelectedBatch(b); setAdjustForm({...adjustForm, quantity: 0}); setIsAdjustModalOpen(true); }} className="text-xs font-medium text-blue-600 hover:underline">Adjust</button>
                                  <button className="text-xs font-medium text-slate-500 hover:text-slate-900">Labels</button>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  )}
                </React.Fragment>
              );
            })}
          </div>
        )}
        <Pagination totalRecords={medicineGroups.length} currentPage={currentPage} pageSize={pageSize} onPageChange={setCurrentPage} />
      </div>

      {/* Adjust Stock Modal */}
      <AppModal
        isOpen={isAdjustModalOpen}
        onClose={() => setIsAdjustModalOpen(false)}
        title="Adjust Stock Quantity"
        maxWidth="sm:max-w-md"
        footer={
          <div className="flex w-full gap-3">
            <button onClick={() => setIsAdjustModalOpen(false)} className="flex-1 px-4 py-2 border border-slate-200 rounded-md text-sm font-medium text-slate-700">Cancel</button>
            <button onClick={handleAdjustSubmit} className="flex-1 px-4 py-2 bg-[#1a3c6e] text-white rounded-md text-sm font-medium">Confirm Adjustment</button>
          </div>
        }
      >
        {selectedBatch && (
          <div className="space-y-4 p-2">
            <div className="bg-slate-50 p-3 rounded-md border border-slate-100 text-sm">
              <p><span className="text-slate-500">Medicine:</span> <span className="font-medium">{selectedBatch.medicine?.name}</span></p>
              <p><span className="text-slate-500">Batch:</span> <span className="font-mono">{selectedBatch.batchNumber}</span></p>
              <p><span className="text-slate-500">Current Qty:</span> <span className="font-medium">{selectedBatch.quantityAvailable}</span></p>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700">Adjustment Quantity (+/-)</label>
              <input type="number" value={adjustForm.quantity} onChange={e => setAdjustForm({...adjustForm, quantity: parseInt(e.target.value) || 0})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none focus:border-blue-500" />
              <p className="text-xs text-slate-500">Use negative numbers to deduct stock (e.g. -5 for damage).</p>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700">Reason</label>
              <select value={adjustForm.reason} onChange={e => setAdjustForm({...adjustForm, reason: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none">
                <option>Physical Count Correction</option>
                <option>Damage / Breakage</option>
                <option>Theft / Loss</option>
                <option>Sample / Internal Use</option>
                <option>Expiry Write-Off</option>
              </select>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700">Remarks (Optional)</label>
              <textarea value={adjustForm.remarks} onChange={e => setAdjustForm({...adjustForm, remarks: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none h-20 resize-none"></textarea>
            </div>
          </div>
        )}
      </AppModal>

    </div>
  );
}
