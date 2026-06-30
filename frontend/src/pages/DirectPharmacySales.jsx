import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { Search, Plus, Eye, Printer, XCircle, Trash2, CheckCircle } from 'lucide-react';
import ModuleFilterBar from '../components/ui/ModuleFilterBar';
import DataTable from '../components/ui/DataTable';
import Pagination from '../components/ui/Pagination';
import AppModal from '../components/ui/AppModal';
import Badge from '../components/ui/Badge';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';
import PharmacyInvoice from '../components/pharmacy/PharmacyInvoice';
import { useDirectSalesStore } from '../store/useDirectSalesStore';
import { useShallow } from 'zustand/react/shallow';
import { usePOSStore } from '../store/usePOSStore';

export default function DirectPharmacySales() {
  const {
    directSalesList: salesList,
    directSalesLoading: loading,
    directSalesSearchTerm: searchTerm,
    directSalesDateRange: dateRange,
    setDirectSalesSearch: setSearchTerm,
    setDirectSalesDateRange: setDateRange,
    fetchDirectSales: fetchSales
  } = useDirectSalesStore(useShallow(state => ({
    directSalesList: state.directSalesList,
    directSalesLoading: state.directSalesLoading,
    directSalesSearchTerm: state.directSalesSearchTerm,
    directSalesDateRange: state.directSalesDateRange,
    setDirectSalesSearch: state.setDirectSalesSearch,
    setDirectSalesDateRange: state.setDirectSalesDateRange,
    fetchDirectSales: state.fetchDirectSales
  })));

  const posStore = usePOSStore(useShallow(state => ({
    patientName: state.patientName,
    doctor: state.doctor,
    paymentType: state.paymentType,
    rows: state.rows,
    patientSearchResults: state.patientSearchResults,
    setField: state.setField,
    resetForm: state.resetForm,
    addRow: state.addRow,
    removeRow: state.removeRow,
    searchPatients: state.searchPatients,
    selectPatient: state.selectPatient,
    handleNameChange: state.handleNameChange,
    selectStock: state.selectStock,
    updateQty: state.updateQty
  })));
  const location = useLocation();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [billToDelete, setBillToDelete] = useState(null);
  const [selectedInvoice, setSelectedInvoice] = useState(null);

  useEffect(() => {
    fetchSales();
  }, [location.key]);

  const calculateSubtotal = () => posStore.rows.reduce((acc, row) => acc + ((Number(row.rate) || 0) * (Number(row.qty) || 0)), 0);
  const calculateGST = () => posStore.rows.reduce((acc, row) => acc + (((Number(row.rate) || 0) * (Number(row.qty) || 0) * (Number(row.gst) || 0)) / 100), 0);
  const calculateNet = () => posStore.rows.reduce((acc, row) => acc + (row.amount || 0), 0);

  const saveBill = async (options = { shouldPrint: false }) => {
    const validItems = posStore.rows.filter(i => i.stockId && (Number(i.qty) > 0));
    if (validItems.length === 0) { toast.error('Add at least one medicine'); return; }

    const payload = {
      patientName: posStore.patientName || 'Walk-in',
      doctorName: 'Self (OTC)',
      billType: 'OTC',
      paymentMode: (posStore.paymentType || 'CASH').toUpperCase(),
      items: validItems.map(item => ({ 
        stockId: item.stockId, 
        quantity: Number(item.qty),
        unitPrice: Number(item.rate),
        gstPercent: Number(item.gst)
      }))
    };

    try {
      const response = await pharmacyService.createSale(payload);
      const billData = response.data || response;
      if (response.success || billData?.id) {
        toast.success('OTC Sale completed!');
        setIsModalOpen(false);
        posStore.resetForm();
        fetchSales();
        
        if (options.shouldPrint) {
           setSelectedInvoice(billData);
           setIsInvoiceModalOpen(true);
        }
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save OTC sale');
    }
  };

  const cancelBill = async () => {
    if (!billToDelete) return;
    try {
      const response = await pharmacyService.deleteSale(billToDelete);
      if (response.success) {
        toast.success('Sale cancelled and stock reverted');
        setIsDeleteModalOpen(false);
        setBillToDelete(null);
        fetchSales();
      }
    } catch (error) {
      toast.error('Failed to cancel sale');
    }
  };

  const columns = [
    { header: 'S.No', render: (_, i) => i + 1 },
    { header: 'Receipt No', accessor: 'billNumber' },
    { header: 'Customer Name', accessor: 'patientName' },
    { header: 'Sale Date', render: (row) => new Date(row.billingDate).toLocaleDateString('en-IN') },
    { header: 'Total Amount', render: (row) => <span className="font-bold text-gray-900">₹{Number(row.netAmount).toFixed(2)}</span> },
    { header: 'Paid Amount', render: (row) => `₹${Number(row.amountPaid).toFixed(2)}` },
    { header: 'Mode', render: (row) => <Badge variant="info">{row.paymentMode}</Badge> },
    { header: 'Action', render: (row) => (
      <div className="flex items-center gap-2">
        <button 
          title="View" 
          onClick={() => { setSelectedInvoice(row); setIsInvoiceModalOpen(true); }}
          className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
        >
          <Eye className="w-4 h-4" />
        </button>
        <button 
          title="Print" 
          onClick={() => { setSelectedInvoice(row); setIsInvoiceModalOpen(true); }}
          className="p-1.5 text-gray-600 hover:bg-gray-50 rounded-lg transition-colors"
        >
          <Printer className="w-4 h-4" />
        </button>
        <button 
          title="Cancel" 
          onClick={() => { setBillToDelete(row.id); setIsDeleteModalOpen(true); }}
          className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
        >
          <XCircle className="w-4 h-4" />
        </button>
      </div>
    )}
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">Direct Sales (OTC)</h2>
        <p className="text-sm text-gray-500 font-medium">Manage over-the-counter transactions for walk-in customers</p>
      </div>

      <ModuleFilterBar 
        onSearch={setSearchTerm}
        searchValue={searchTerm}
        dateRange={dateRange}
        onDateChange={(type, val) => setDateRange(prev => ({ ...prev, [type]: val }))}
        actions={[
          { label: 'New OTC Entry', icon: Plus, variant: 'primary', onClick: () => setIsModalOpen(true) }
        ]}
      />

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <DataTable 
          columns={columns} 
          data={salesList} 
          hover 
          striped 
        />
        <Pagination totalRecords={salesList.length} currentPage={1} pageSize={10} onPageChange={() => {}} onPageSizeChange={() => {}} />
      </div>

      {/* Entry Modal */}
      <AppModal
        isOpen={isModalOpen}
        onClose={() => { setIsModalOpen(false); posStore.resetForm(); }}
        title="Direct OTC Entry"
        maxWidth="sm:max-w-6xl"
        footer={
          <div className="flex justify-between items-center w-full">
            <button 
              onClick={() => { setIsModalOpen(false); posStore.resetForm(); }} 
              className="px-6 py-2 border border-gray-200 rounded-xl text-sm font-bold text-gray-500 hover:bg-gray-50 transition-all flex items-center gap-2"
            >
              <XCircle className="w-4 h-4" /> Cancel
            </button>
            <div className="flex gap-3">
              <button 
                onClick={() => saveBill({ shouldPrint: false })} 
                className="px-6 py-2 border border-slate-200 rounded-xl text-sm font-bold text-slate-600 hover:bg-slate-50 transition-all"
              >
                Save Only
              </button>
              <button 
                onClick={() => saveBill({ shouldPrint: true })} 
                className="px-8 py-2 bg-primary text-white rounded-xl text-sm font-bold shadow-lg shadow-primary/20 hover:bg-blue-700 transition-all flex items-center gap-2 hover:scale-[1.02] active:scale-[0.98]"
              >
                <CheckCircle className="w-4 h-4" /> Save & Print Bill
              </button>
            </div>
          </div>
        }
      >
        <div className="space-y-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 bg-slate-50 p-6 rounded-2xl border border-slate-100">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Customer Name (Optional)</label>
              <input 
                type="text" 
                value={posStore.patientName}
                onChange={(e) => posStore.setField('patientName', e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 outline-none focus:ring-2 focus:ring-primary/20 bg-white" 
                placeholder="Walk-in" 
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Mobile Number</label>
              <input 
                type="text" 
                value={posStore.uhid || ''}
                onChange={(e) => posStore.setField('uhid', e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 outline-none focus:ring-2 focus:ring-primary/20 bg-white" 
                placeholder="Contact number" 
              />
            </div>
          </div>

          <div className="space-y-4">
            <div className="border border-gray-100 rounded-2xl overflow-hidden shadow-sm">
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-[#1e293b] text-white text-[11px] uppercase tracking-widest">
                    <tr>
                      <th className="px-4 py-3 text-left">Medicine Name</th>
                      <th className="px-4 py-3 text-left">Batch</th>
                      <th className="px-4 py-3 text-left">Expiry</th>
                      <th className="px-4 py-3 text-center w-24">Qty</th>
                      <th className="px-4 py-3 text-right">Rate</th>
                      <th className="px-4 py-3 text-center w-20">GST %</th>
                      <th className="px-4 py-3 text-right">Amount</th>
                      <th className="px-4 py-3 text-center w-12"></th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {posStore.rows.map((item, idx) => (
                      <tr key={item.id} className="hover:bg-slate-50/50 transition-colors">
                        <td className="px-4 py-3 relative">
                          <input 
                            type="text" 
                            value={item.codeName}
                            onChange={(e) => posStore.handleNameChange(idx, e.target.value)}
                            placeholder="Search medicine..." 
                            className="w-full bg-transparent outline-none focus:text-primary font-bold placeholder:text-slate-300 placeholder:font-normal" 
                          />
                          {item.searchResults?.length > 0 && (
                            <div className="absolute z-50 left-0 top-full mt-1 w-96 bg-white shadow-2xl border border-blue-100 rounded-xl overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200">
                                <div className="bg-slate-50 px-4 py-2 text-[10px] font-bold text-slate-400 border-b border-slate-100 uppercase tracking-widest">
                                  Available Stock ({item.searchResults.length})
                                </div>
                                {item.searchResults.map((stock) => (
                                  <div
                                    key={stock.id}
                                    onClick={() => posStore.selectStock(idx, stock)}
                                    className="px-4 py-3 hover:bg-primary hover:text-white cursor-pointer border-b last:border-0 transition-all group"
                                  >
                                    <div className="font-bold group-hover:text-white">{stock.medicine?.name}</div>
                                    <div className="flex items-center gap-2 text-[10px] opacity-70 mt-0.5 group-hover:text-white/80">
                                      <span className="font-mono bg-slate-100 group-hover:bg-white/20 px-1 rounded">BATCH: {stock.batchNumber}</span>
                                      <span>EXP: {stock.expiryDate}</span>
                                      <span className="font-bold">STOCK: {stock.quantityAvailable}</span>
                                    </div>
                                  </div>
                                ))}
                            </div>
                          )}
                        </td>
                        <td className="px-4 py-3 text-xs text-slate-500 uppercase font-mono">{item.batchNo || <span className="text-slate-300">-</span>}</td>
                        <td className="px-4 py-3 text-xs text-slate-500">{item.expiryDate || <span className="text-slate-300">-</span>}</td>
                        <td className="px-4 py-3">
                          <input 
                            type="number" 
                            value={item.qty}
                            onChange={(e) => posStore.updateQty(idx, e.target.value)}
                            className="w-full text-center border border-slate-200 rounded-lg py-1 outline-none focus:border-primary font-bold" 
                          />
                        </td>
                        <td className="px-4 py-3 text-right">
                          {item.rate}
                        </td>
                        <td className="px-4 py-3 text-center text-amber-600 font-bold text-xs">
                          {item.gst} <span className="text-amber-400">%</span>
                        </td>
                        <td className="px-4 py-3 text-right font-bold text-slate-900">₹{Number(item.amount).toFixed(2)}</td>
                        <td className="px-4 py-3 text-center">
                          <button onClick={() => posStore.removeRow(idx)} className="p-1 text-slate-300 hover:text-red-500"><Trash2 className="w-4 h-4"/></button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <button onClick={posStore.addRow} className="w-full py-3 bg-slate-50 text-primary text-xs font-bold uppercase tracking-widest hover:bg-slate-100 transition-all border-t border-slate-100">+ Add Medicine Row</button>
            </div>
          </div>

          <div className="flex flex-col md:flex-row gap-8 items-start justify-between">
            <div className="flex-1 w-full space-y-4">
               <div className="w-64 space-y-1.5">
                  <label className="text-xs font-bold text-gray-500 uppercase tracking-wider">Payment Mode</label>
                  <select 
                    value={posStore.paymentType}
                    onChange={(e) => posStore.setField('paymentType', e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 outline-none focus:ring-2 focus:ring-primary/20 bg-white font-semibold"
                  >
                    <option value="Cash">Cash</option>
                    <option value="Card">Card</option>
                    <option value="UPI">UPI / QR Code</option>
                  </select>
               </div>
               <div className="space-y-1.5">
                  <label className="text-xs font-bold text-gray-500 uppercase tracking-wider">Remarks</label>
                  <textarea className="w-full h-20 px-4 py-2 rounded-xl border border-slate-200 outline-none focus:ring-2 focus:ring-primary/20 bg-white resize-none text-sm" placeholder="Any special notes for this OTC sale..." />
               </div>
            </div>
            
            <div className="w-full md:w-80 space-y-3 p-6 bg-slate-900 text-white rounded-2xl shadow-xl">
               <div className="flex justify-between text-xs text-slate-400 uppercase tracking-widest font-bold">
                 <span>Subtotal</span>
                 <span>₹{calculateSubtotal().toFixed(2)}</span>
               </div>
               <div className="flex justify-between text-xs text-slate-400 uppercase tracking-widest font-bold">
                 <span>GST Total</span>
                 <span>₹{calculateGST().toFixed(2)}</span>
               </div>
               <div className="border-t border-white/10 pt-3 flex justify-between text-xl font-black">
                 <span className="tracking-tighter uppercase">Total Amount</span>
                 <span className="text-blue-400">₹{calculateNet().toFixed(2)}</span>
               </div>
            </div>
          </div>
        </div>
      </AppModal>

      {/* Invoice Print Modal */}
      <AppModal
        isOpen={isInvoiceModalOpen}
        onClose={() => setIsInvoiceModalOpen(false)}
        size="xl"
        title="Tax Invoice"
      >
        <PharmacyInvoice 
          bill={selectedInvoice} 
          onClose={() => setIsInvoiceModalOpen(false)} 
        />
      </AppModal>

      {/* Delete Confirmation Modal */}
      <AppModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Confirm Sale Cancellation"
        maxWidth="sm:max-w-md"
        footer={
          <div className="flex gap-3 w-full">
            <button onClick={() => setIsDeleteModalOpen(false)} className="flex-1 px-6 py-2 border border-gray-200 rounded-xl text-sm font-bold text-gray-500 hover:bg-gray-50 transition-all">No, Keep Sale</button>
            <button onClick={cancelBill} className="flex-1 px-6 py-2 bg-red-600 text-white rounded-xl text-sm font-bold shadow-lg shadow-red-200 hover:bg-red-700 transition-all">Yes, Cancel Sale</button>
          </div>
        }
      >
        <div className="p-8 text-center text-gray-500">
           <XCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
           <p className="font-bold text-gray-900 mb-1">Are you sure?</p>
           <p className="text-sm">This will cancel the OTC receipt and return items to stock. This action cannot be undone.</p>
        </div>
      </AppModal>
    </div>
  );
}
