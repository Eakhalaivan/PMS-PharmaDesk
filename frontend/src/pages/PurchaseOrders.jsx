import React, { useState, useEffect } from 'react';
import { Search, Plus, Eye, FileText, CheckCircle, XCircle, Trash2, Edit3, Loader2 } from 'lucide-react';
import ModuleFilterBar from '../components/ui/ModuleFilterBar';
import DataTable from '../components/ui/DataTable';
import Pagination from '../components/ui/Pagination';
import AppModal from '../components/ui/AppModal';
import Badge from '../components/ui/Badge';
import { toast } from 'react-hot-toast';
import { usePageData } from '../hooks/usePageData';
import useDebounce from '../hooks/useDebounce';
import TableSkeleton from '../components/ui/TableSkeleton';
import ErrorBanner from '../components/ui/ErrorBanner';
import { useSearchParams, useNavigate } from 'react-router-dom';
import pharmacyService from '../utils/pharmacyService';
import { useAuth } from '../context/AuthContext';
import { ROLES } from '../config/roles.config';

export default function PurchaseOrders() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const { activeRole } = useAuth();
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState(searchParams.get('search') || '');
  const debouncedSearch = useDebounce(searchTerm, 500);
  
  const [dateRange, setDateRange] = useState({ 
    from: searchParams.get('from') ? new Date(searchParams.get('from')) : null, 
    to: searchParams.get('to') ? new Date(searchParams.get('to')) : null 
  });
  const [statusFilter, setStatusFilter] = useState(searchParams.get('status') || 'ALL');

  // Form State
  const [supplierId, setSupplierId] = useState('');
  const [suppliers, setSuppliers] = useState([]);
  const [expectedDeliveryDate, setExpectedDeliveryDate] = useState('');
  const [notes, setNotes] = useState('');
  const [taxPercent, setTaxPercent] = useState(18);
  const [items, setItems] = useState([
    { id: 1, medicineId: null, medicineName: '', qty: 1, unitPrice: 0 }
  ]);
  const [searchResults, setSearchResults] = useState([]);
  const [activeSearchIdx, setActiveSearchIdx] = useState(null);

  // Sync back to URL when filters change
  useEffect(() => {
    setSearchParams(prev => {
      if (debouncedSearch) prev.set('search', debouncedSearch); else prev.delete('search');
      if (dateRange.from) prev.set('from', dateRange.from.toISOString()); else prev.delete('from');
      if (dateRange.to) prev.set('to', dateRange.to.toISOString()); else prev.delete('to');
      if (statusFilter !== 'ALL') prev.set('status', statusFilter); else prev.delete('status');
      prev.set('page', '0'); // reset to page 1 on filter change
      return prev;
    }, { replace: true });
  }, [debouncedSearch, dateRange.from, dateRange.to, statusFilter, setSearchParams]);

  const { items: poList, isLoading, isError, page, totalElements, goToPage, refetch } = usePageData(
    'purchase-orders',
    '/pharmacy/purchase-orders',
    { 
      searchTerm: debouncedSearch, 
      fromDate: dateRange.from?.toISOString(), 
      toDate: dateRange.to?.toISOString(),
      status: statusFilter === 'ALL' ? undefined : statusFilter
    }
  );

  useEffect(() => {
    // Load suppliers for dropdown
    pharmacyService.getSuppliers().then(res => setSuppliers(res.data?.content || res.data || []));
  }, []);

  const handleStockSearch = async (val, idx) => {
    if (val.length < 2) {
      setSearchResults([]);
      setActiveSearchIdx(null);
      return;
    }
    setActiveSearchIdx(idx);
    try {
      const response = await pharmacyService.searchStocks(val);
      setSearchResults(response?.data || response || []);
    } catch (error) {
      console.error(error);
    }
  };

  const selectMedicine = (stock, idx) => {
    const newItems = [...items];
    newItems[idx] = {
      ...newItems[idx],
      medicineId: stock.medicine?.id || stock.id,
      medicineName: stock.medicine?.name || stock.name || '',
      unitPrice: stock.purchaseRate || 0
    };
    setItems(newItems);
    setSearchResults([]);
    setActiveSearchIdx(null);
  };

  const resetModal = () => {
    setSupplierId('');
    setExpectedDeliveryDate('');
    setNotes('');
    setTaxPercent(18);
    setItems([{ id: 1, medicineId: null, medicineName: '', qty: 1, unitPrice: 0 }]);
  };

  const calculateSubtotal = () => items.reduce((acc, item) => acc + (Number(item.unitPrice) * Number(item.qty)), 0);
  const calculateTaxAmount = () => (calculateSubtotal() * taxPercent) / 100;
  const calculateGrandTotal = () => calculateSubtotal() + calculateTaxAmount();

  const handleSavePO = async () => {
    if (!supplierId) return toast.error('Please select a supplier');
    const validItems = items.filter(i => i.medicineId && i.qty > 0 && i.unitPrice > 0);
    if (validItems.length === 0) return toast.error('Add at least one valid item');

    setIsSubmitting(true);
    const payload = {
      supplier: { id: supplierId },
      expectedDeliveryDate,
      notes,
      taxAmount: calculateTaxAmount(),
      subtotal: calculateSubtotal(),
      totalAmount: calculateGrandTotal(),
      items: validItems.map(i => ({ 
        medicine: { id: i.medicineId }, 
        quantity: Number(i.qty), 
        estimatedUnitPrice: Number(i.unitPrice) 
      }))
    };

    try {
      // Assuming a POST endpoint exists
      const response = await pharmacyService.api.post('/pharmacy/purchase-orders', payload);
      toast.success('Purchase Order created successfully');
      setIsModalOpen(false);
      resetModal();
      refetch();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create PO');
    } finally {
      setIsSubmitting(false);
    }
  };

  const columns = [
    { header: 'PO No', accessor: 'poNumber' },
    { header: 'Supplier', accessor: 'supplierName' },
    { header: 'Order Date', render: (row) => new Date(row.orderDate || Date.now()).toLocaleDateString('en-IN') },
    { header: 'Expected Delivery', render: (row) => row.expectedDeliveryDate ? new Date(row.expectedDeliveryDate).toLocaleDateString('en-IN') : '-' },
    { header: 'Total Amount', render: (row) => <span className="font-bold">₹{Number(row.totalAmount || 0).toFixed(2)}</span> },
    {
      header: 'Status', render: (row) => {
        const variants = { PENDING: 'warning', APPROVED: 'primary', RECEIVED: 'success', CANCELLED: 'danger' };
        return <Badge variant={variants[row.status] || 'default'}>{row.status || 'PENDING'}</Badge>;
      }
    },
    {
      header: 'Action', render: (row) => (
        <div className="flex items-center gap-2">
          <button 
            title="View Details"
            onClick={() => navigate(`/purchase-orders/${row.id || row.poNumber}`)} 
            className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg"
          >
            <Eye className="w-4 h-4" />
          </button>
        </div>
      )
    },
  ];

  if (activeRole !== ROLES.SYSTEM_ADMIN) {
    return <div className="p-10 text-center text-red-500 font-bold">Unauthorized Access</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">Purchase Orders</h2>
        <p className="text-sm text-gray-500 font-medium">Manage and track supplier purchase orders</p>
      </div>

      <div className="flex flex-wrap items-center gap-4 bg-white p-4 rounded-2xl shadow-sm border border-gray-100">
        <div className="flex-1 min-w-[200px]">
          <ModuleFilterBar
            onSearch={setSearchTerm}
            searchValue={searchTerm}
            dateRange={dateRange}
            onDateChange={(type, val) => setDateRange(prev => ({ ...prev, [type]: val }))}
            hideDateRange={false}
          />
        </div>
        <select 
          value={statusFilter} 
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl outline-none font-medium text-sm text-slate-700"
        >
          <option value="ALL">All Status</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="RECEIVED">Received</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
        <button 
          onClick={() => setIsModalOpen(true)}
          className="px-4 py-2 bg-primary text-white rounded-xl text-sm font-bold flex items-center gap-2 hover:bg-blue-700 shadow-md shadow-primary/20 transition-all"
        >
          <Plus className="w-4 h-4" /> New PO
        </button>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {isError ? (
          <div className="p-6">
            <ErrorBanner onRetry={refetch} message="Failed to load purchase orders. Check connection." />
          </div>
        ) : isLoading ? (
          <TableSkeleton rows={5} columns={7} />
        ) : !isLoading && !isError && poList.length === 0 ? (
          <div className="p-10 text-center flex flex-col items-center">
            <div className="w-16 h-16 bg-blue-50 text-blue-300 rounded-full flex items-center justify-center mb-4">
              <FileText className="w-8 h-8" />
            </div>
            <p className="text-gray-500 font-semibold text-lg">No Purchase Orders Found</p>
            <p className="text-sm text-gray-400">Click '+ New PO' to create one.</p>
          </div>
        ) : (
          <>
            <DataTable columns={columns} data={poList} hover striped />
            {totalElements > 0 && (
              <Pagination
                totalRecords={totalElements}
                currentPage={page + 1}
                pageSize={20}
                onPageChange={(p) => goToPage(p - 1)}
              />
            )}
          </>
        )}
      </div>

      {/* New PO Modal */}
      <AppModal
        isOpen={isModalOpen}
        onClose={() => { setIsModalOpen(false); resetModal(); }}
        title="Create Purchase Order"
        maxWidth="sm:max-w-5xl"
        footer={
          <div className="flex justify-end gap-3 w-full">
            <button onClick={() => { setIsModalOpen(false); resetModal(); }} className="px-6 py-2 border rounded-xl text-sm font-bold text-gray-600 hover:bg-gray-50">
              Cancel
            </button>
            <button 
              onClick={handleSavePO} 
              disabled={isSubmitting}
              className="px-8 py-2 bg-primary text-white rounded-xl text-sm font-bold flex items-center gap-2 shadow-lg shadow-primary/20 disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              {isSubmitting ? 'Creating...' : 'Create PO'}
            </button>
          </div>
        }
      >
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 bg-slate-50 p-6 rounded-2xl border border-slate-100">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">Supplier</label>
              <select value={supplierId} onChange={e => setSupplierId(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 outline-none bg-white">
                <option value="">-- Select Supplier --</option>
                {suppliers.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">Expected Delivery</label>
              <input type="date" value={expectedDeliveryDate} onChange={e => setExpectedDeliveryDate(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 outline-none bg-white" />
            </div>
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">Notes</label>
              <input type="text" placeholder="Internal notes..." value={notes} onChange={e => setNotes(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 outline-none bg-white" />
            </div>
          </div>

          <div className="border border-gray-100 rounded-2xl overflow-hidden shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-slate-800 text-white text-[11px] uppercase tracking-widest">
                  <tr>
                    <th className="px-4 py-3 text-left">Medicine Name</th>
                    <th className="px-4 py-3 text-center w-32">Qty</th>
                    <th className="px-4 py-3 text-right w-32">Unit Price (₹)</th>
                    <th className="px-4 py-3 text-right w-32">Total</th>
                    <th className="px-4 py-3 text-center w-12"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {items.map((item, idx) => (
                    <tr key={item.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 relative">
                        <input
                          type="text"
                          placeholder="Search medicine..."
                          value={item.medicineName}
                          onChange={(e) => {
                            const val = e.target.value;
                            const newItems = [...items];
                            newItems[idx].medicineName = val;
                            setItems(newItems);
                            handleStockSearch(val, idx);
                          }}
                          className="w-full bg-transparent outline-none font-medium"
                        />
                        {searchResults.length > 0 && activeSearchIdx === idx && item.medicineName.length > 1 && (
                          <div className="absolute z-50 left-0 top-full mt-1 w-80 bg-white shadow-2xl border border-blue-100 rounded-xl overflow-hidden max-h-60 overflow-y-auto">
                            {searchResults.map((stock) => (
                              <div key={stock.id} onClick={() => selectMedicine(stock, idx)} className="px-4 py-3 hover:bg-blue-50 cursor-pointer border-b">
                                <div className="font-bold">{stock.medicine?.name || stock.name}</div>
                              </div>
                            ))}
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <input type="number" min="1" value={item.qty} onChange={(e) => {
                          const newItems = [...items];
                          newItems[idx].qty = e.target.value;
                          setItems(newItems);
                        }} className="w-full text-center border border-slate-200 rounded-lg py-1.5 outline-none" />
                      </td>
                      <td className="px-4 py-3">
                        <input type="number" min="0" step="0.01" value={item.unitPrice} onChange={(e) => {
                          const newItems = [...items];
                          newItems[idx].unitPrice = e.target.value;
                          setItems(newItems);
                        }} className="w-full text-right border border-slate-200 rounded-lg py-1.5 outline-none" />
                      </td>
                      <td className="px-4 py-3 text-right font-bold text-slate-700">
                        ₹{(Number(item.qty) * Number(item.unitPrice)).toFixed(2)}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <button onClick={() => setItems(items.filter(s => s.id !== item.id))} className="text-slate-300 hover:text-red-500">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <button 
              onClick={() => setItems([...items, { id: Date.now(), medicineId: null, medicineName: '', qty: 1, unitPrice: 0 }])} 
              className="w-full py-3 bg-blue-50/50 hover:bg-blue-50 text-primary text-xs font-bold uppercase tracking-widest border-t transition-colors"
            >
              + Add Row
            </button>
          </div>

          <div className="flex justify-end pt-4">
            <div className="w-full md:w-80 space-y-3 p-4 bg-slate-50 rounded-2xl border border-slate-200">
              <div className="flex justify-between text-sm">
                <span className="text-slate-500">Subtotal</span>
                <span className="font-bold">₹{calculateSubtotal().toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm items-center">
                <span className="text-slate-500">Tax (%)</span>
                <input 
                  type="number" 
                  value={taxPercent} 
                  onChange={e => setTaxPercent(e.target.value)} 
                  className="w-16 px-2 py-1 text-right border rounded bg-white outline-none"
                />
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-slate-500">Tax Amount</span>
                <span className="font-bold text-amber-600">₹{calculateTaxAmount().toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-lg pt-3 border-t border-slate-200 font-bold">
                <span className="text-gray-800">Grand Total</span>
                <span className="text-primary">₹{calculateGrandTotal().toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </AppModal>
    </div>
  );
}
