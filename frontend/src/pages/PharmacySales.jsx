import React, { useState, useEffect } from 'react';
import { Search, Plus, Eye, Printer, XCircle, Trash2, PlusCircle, Barcode, CheckCircle } from 'lucide-react';
import ModuleFilterBar from '../components/ui/ModuleFilterBar';
import DataTable from '../components/ui/DataTable';
import Pagination from '../components/ui/Pagination';
import AppModal from '../components/ui/AppModal';
import Badge from '../components/ui/Badge';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';
import PharmacyInvoice from '../components/pharmacy/PharmacyInvoice';
import { usePharmacySalesStore } from '../store/usePharmacySalesStore';
import { usePOSStore } from '../store/usePOSStore';
import { useShallow } from 'zustand/react/shallow';
import TableSkeleton from '../components/ui/TableSkeleton';
import ErrorBanner from '../components/ui/ErrorBanner';

export default function PharmacySales() {
  const {
    salesList, salesLoading: isLoading, salesError: isError, salesPage: page, 
    salesTotalElements: totalElements, salesSearchTerm: searchTerm, salesDateRange: dateRange,
    setSalesSearch, setSalesDateRange, setSalesPage, fetchSales
  } = usePharmacySalesStore(useShallow(state => ({
    salesList: state.salesList,
    salesLoading: state.salesLoading,
    salesError: state.salesError,
    salesPage: state.salesPage,
    salesTotalElements: state.salesTotalElements,
    salesSearchTerm: state.salesSearchTerm,
    salesDateRange: state.salesDateRange,
    setSalesSearch: state.setSalesSearch,
    setSalesDateRange: state.setSalesDateRange,
    setSalesPage: state.setSalesPage,
    fetchSales: state.fetchSales
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

  useEffect(() => {
    fetchSales();
  }, []);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [billToDelete, setBillToDelete] = useState(null);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [barcodeInput, setBarcodeInput] = useState('');

  const handleBarcodeScan = async (e) => {
    if (e.key === 'Enter' && barcodeInput) {
      try {
        const response = await pharmacyService.getStockByBarcode(barcodeInput);
        if (response && response.success) {
          const stock = response.data;
          const existingIdx = posStore.rows.findIndex(item => item.stockId === stock.id);
          if (existingIdx > -1) {
            posStore.updateQty(existingIdx, Number(posStore.rows[existingIdx].qty || 0) + 1);
          } else {
            const emptyIdx = posStore.rows.findIndex(item => !item.stockId);
            if (emptyIdx > -1) {
              posStore.selectStock(emptyIdx, stock);
            } else {
              posStore.addRow();
              const newIdx = posStore.rows.length;
              setTimeout(() => posStore.selectStock(newIdx, stock), 0);
            }
          }
          setBarcodeInput('');
          toast.success(`${stock.medicine?.name} added`);
        } else {
          toast.error('Medicine not found for this barcode');
        }
      } catch (error) {
        toast.error('Barcode not found');
      }
    }
  };

  const calculateSubtotal = () => posStore.rows.reduce((acc, row) => acc + ((Number(row.rate) || 0) * (Number(row.qty) || 0)), 0);
  const calculateGST = () => posStore.rows.reduce((acc, row) => acc + (((Number(row.rate) || 0) * (Number(row.qty) || 0) * (Number(row.gst) || 0)) / 100), 0);
  const calculateNet = () => posStore.rows.reduce((acc, row) => acc + (row.amount || 0), 0);

  const saveBill = async (options = { shouldPrint: false }) => {
    if (!posStore.patientName) { toast.error('Please enter patient name'); return; }
    const validItems = posStore.rows.filter(i => i.stockId && (Number(i.qty) > 0));
    if (validItems.length === 0) { toast.error('Add at least one medicine'); return; }

    const paymentMode = posStore.paymentType === 'ADVANCE' ? 'CASH' : posStore.paymentType;
    const amountPaid = posStore.paymentType === 'ADVANCE' ? 0 : calculateNet();

    const payload = {
      patientName: posStore.patientName,
      doctorName: posStore.doctor,
      items: validItems.map(item => ({ 
        stockId: item.stockId, 
        quantity: Number(item.qty),
        unitPrice: Number(item.rate),
        gstPercent: Number(item.gst)
      })),
      paymentMode,
      discountAmount: 0,
      amountPaid,
      useAdvance: posStore.paymentType === 'ADVANCE'
    };

    try {
      const response = await pharmacyService.createSale(payload);
      if (response) {
        toast.success('Bill saved successfully!');
        setIsModalOpen(false);
        posStore.resetForm();
        fetchSales();
        if (options.shouldPrint) {
          const billData = response.data || response;
          const fullBill = await pharmacyService.getSaleByNumber(billData.billNumber);
          setSelectedInvoice(fullBill.data || fullBill);
          setIsInvoiceModalOpen(true);
        }
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save bill');
    }
  };

  const cancelBill = async () => {
    if (!billToDelete) return;
    try {
      const response = await pharmacyService.deleteSale(billToDelete);
      if (response.success) {
        toast.success(response.message || 'Bill cancelled successfully');
        setIsDeleteModalOpen(false);
        setBillToDelete(null);
        fetchSales();
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to cancel bill');
    }
  };

  const columns = [
    { header: 'S.No', render: (_, i) => (page * 20) + i + 1 },
    { header: 'Bill No', accessor: 'billNumber' },
    { header: 'Patient Name', accessor: 'patientName' },
    { header: 'Bill Date', render: (row) => new Date(row.billingDate).toLocaleDateString('en-IN') },
    { header: 'Total Amount', render: (row) => `₹ ${Number(row.netAmount).toFixed(2)}` },
    { header: 'Payment Mode', render: (row) => row.paymentMode || 'CASH' },
    {
      header: 'Status', render: (row) => (
        <Badge variant={row.status === 'PAID' ? 'success' : row.status === 'CANCELLED' ? 'danger' : 'warning'}>
          {row.status || 'PENDING'}
        </Badge>
      )
    },
    {
      header: 'Action', render: (row) => (
        <div className="flex items-center gap-2">
          <button title="View Invoice" onClick={() => { setSelectedInvoice(row); setIsInvoiceModalOpen(true); }} className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg">
            <Eye className="w-4 h-4" />
          </button>
          <button title="Print Invoice" onClick={() => { setSelectedInvoice(row); setIsInvoiceModalOpen(true); }} className="p-1.5 text-gray-600 hover:bg-gray-50 rounded-lg">
            <Printer className="w-4 h-4" />
          </button>
          {row.status !== 'CANCELLED' && (
            <button title="Cancel Bill" onClick={() => { setBillToDelete(row.id); setIsDeleteModalOpen(true); }} className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg">
              <XCircle className="w-4 h-4" />
            </button>
          )}
        </div>
      )
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">Pharmacy Sales List</h2>
        <p className="text-sm text-gray-500 font-medium">Manage and review all patient medicine bills</p>
      </div>

      <ModuleFilterBar
        onSearch={setSalesSearch}
        searchValue={searchTerm}
        dateRange={dateRange}
        onDateChange={(type, val) => setSalesDateRange({ ...dateRange, [type]: val })}
        actions={[
          { label: 'New Sale', icon: Plus, variant: 'primary', onClick: () => setIsModalOpen(true) }
        ]}
      />

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {isError ? (
          <div className="p-6">
            <ErrorBanner onRetry={fetchSales} />
          </div>
        ) : isLoading ? (
          <TableSkeleton rows={5} columns={8} />
        ) : !isLoading && !isError && salesList.length === 0 ? (
          <div className="p-10 text-center text-slate-500 font-semibold">No records found</div>
        ) : (
          <>
            <DataTable columns={columns} data={salesList} hover striped />
            {totalElements > 0 && (
              <Pagination
                totalRecords={totalElements}
                currentPage={page + 1}
                pageSize={20}
                onPageChange={(p) => setSalesPage(p - 1)}
              />
            )}
          </>
        )}
      </div>

      {/* New Sale Modal */}
      <AppModal
        isOpen={isModalOpen}
        onClose={() => { setIsModalOpen(false); posStore.resetForm(); }}
        title="Create New Pharmacy Sale"
        maxWidth="sm:max-w-6xl"
        footer={
          <div className="flex justify-between items-center w-full">
            <button onClick={() => { setIsModalOpen(false); posStore.resetForm(); }} className="px-6 py-2 border border-gray-200 rounded-xl text-sm font-bold text-gray-500 flex items-center gap-2 hover:bg-gray-50 transition-all">
              <XCircle className="w-4 h-4" /> Cancel
            </button>
            <div className="flex gap-3">
              <button onClick={() => saveBill({ shouldPrint: false })} className="px-6 py-2 border border-slate-200 rounded-xl text-sm font-bold text-slate-600 hover:bg-slate-50 transition-all">
                Save Only
              </button>
              <button onClick={() => saveBill({ shouldPrint: true })} className="px-8 py-2 bg-primary text-white rounded-xl text-sm font-bold flex items-center gap-2 shadow-lg shadow-primary/20 hover:bg-blue-700 transition-all">
                <Printer className="w-4 h-4" /> Save & Print Bill
              </button>
            </div>
          </div>
        }
      >
        <div className="space-y-8">
          {/* Patient Info */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 bg-slate-50 p-6 rounded-2xl border border-slate-100">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">Patient Name</label>
              <div className="relative">
                <input
                  type="text"
                  placeholder="Search Patient..."
                  value={posStore.patientName}
                  onChange={(e) => {
                    posStore.setField('patientName', e.target.value);
                    posStore.searchPatients(e.target.value);
                  }}
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 outline-none"
                />
                <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
                {posStore.patientSearchResults.length > 0 && (
                  <div className="absolute z-[60] left-0 top-full mt-1 w-full bg-white shadow-2xl border border-blue-100 rounded-xl overflow-hidden">
                    {posStore.patientSearchResults.map(p => (
                      <div key={p.id} onClick={() => { posStore.selectPatient(p); }} className="px-4 py-3 hover:bg-blue-50 cursor-pointer border-b">
                        <div className="font-bold text-slate-800">{p.name}</div>
                        <div className="text-[10px] text-slate-500 uppercase">UHID: {p.uhid} | PHONE: {p.phone}</div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">Doctor Name</label>
              <input 
                type="text" 
                value={posStore.doctor} 
                onChange={(e) => posStore.setField('doctor', e.target.value)} 
                placeholder="Doctor name..." 
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 outline-none" 
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">Ward / OPD</label>
              <input type="text" readOnly value="General OPD" className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-white/50 outline-none" />
            </div>
          </div>

          {/* Barcode Scan */}
          <div className="bg-blue-600 p-4 rounded-2xl flex items-center gap-4 shadow-lg shadow-blue-200">
            <Barcode className="w-6 h-6 text-white" />
            <input
              type="text"
              placeholder="Scan Barcode here..."
              value={barcodeInput}
              onChange={(e) => setBarcodeInput(e.target.value)}
              onKeyDown={handleBarcodeScan}
              autoFocus
              className="flex-1 bg-transparent border-b-2 border-white/30 text-white placeholder:text-white/60 py-2 outline-none text-lg font-bold"
            />
          </div>

          {/* Medicine Entry */}
          <div className="border border-gray-100 rounded-2xl overflow-visible shadow-sm">
            <div className="overflow-visible">
              <table className="w-full text-sm">
                <thead className="bg-[#1e293b] text-white text-[11px] uppercase tracking-widest">
                  <tr>
                    <th className="px-4 py-3 text-left">Medicine Name</th>
                    <th className="px-4 py-3 text-left">Batch</th>
                    <th className="px-4 py-3 text-center w-20">Qty</th>
                    <th className="px-4 py-3 text-right">Rate</th>
                    <th className="px-4 py-3 text-center w-16">GST%</th>
                    <th className="px-4 py-3 text-right">Amount</th>
                    <th className="px-4 py-3 text-center w-12"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {posStore.rows.map((item, idx) => (
                    <tr key={item.id} className="hover:bg-slate-50/50">
                      <td className="px-4 py-3 relative">
                        <input
                          type="text"
                          placeholder="Search medicine..."
                          value={item.codeName}
                          onChange={(e) => posStore.handleNameChange(idx, e.target.value)}
                          className="w-full bg-transparent outline-none font-medium"
                        />
                        {item.searchResults?.length > 0 && item.codeName.length > 1 && (
                          <div className="absolute z-[70] left-0 top-full mt-1 w-80 bg-white shadow-2xl border border-blue-100 rounded-xl overflow-hidden">
                            {item.searchResults.map((stock) => (
                              <div key={stock.id} onClick={() => posStore.selectStock(idx, stock)} className="px-4 py-3 hover:bg-blue-600 hover:text-white cursor-pointer border-b group">
                                <div className="font-bold group-hover:text-white">{stock.medicine?.name}</div>
                                <div className="text-[10px] opacity-70">BATCH: {stock.batchNumber} | STOCK: {stock.quantityAvailable}</div>
                              </div>
                            ))}
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-3 text-slate-500 uppercase">{item.batchNo || '-'}</td>
                      <td className="px-4 py-3">
                        <input 
                          type="number" 
                          value={item.qty} 
                          onChange={(e) => posStore.updateQty(idx, e.target.value)} 
                          className="w-full text-center border rounded-lg py-1" 
                        />
                      </td>
                      <td className="px-4 py-3 text-right">₹{Number(item.rate).toFixed(2)}</td>
                      <td className="px-4 py-3 text-center">{item.gst}%</td>
                      <td className="px-4 py-3 text-right font-bold">₹{Number(item.amount).toFixed(2)}</td>
                      <td className="px-4 py-3 text-center">
                        <button onClick={() => posStore.removeRow(idx)} className="text-slate-300 hover:text-red-500">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <button onClick={() => posStore.addRow()} className="w-full py-3 bg-slate-50 text-primary text-xs font-bold uppercase tracking-widest border-t hover:bg-slate-100 transition-all">
              + Add Medicine Row
            </button>
          </div>

          {/* Summary */}
          <div className="flex flex-col md:flex-row gap-8 items-start justify-between p-6 bg-slate-50 rounded-2xl border border-slate-100">
            <div className="flex-1 space-y-4">
              <label className="text-xs font-bold text-gray-500 uppercase tracking-wider">Payment Mode</label>
              <select 
                value={posStore.paymentType} 
                onChange={(e) => posStore.setField('paymentType', e.target.value)} 
                className="w-full max-w-xs px-4 py-2.5 rounded-xl border outline-none font-semibold"
              >
                <option value="Cash">Cash</option>
                <option value="Card">Card</option>
                <option value="UPI">UPI</option>
                <option value="ADVANCE">Advance Adjust</option>
              </select>
            </div>
            <div className="w-full md:w-80 space-y-3 p-6 bg-slate-900 text-white rounded-2xl shadow-xl">
              <div className="flex justify-between text-xs text-slate-400 uppercase tracking-widest font-bold">
                <span>Subtotal</span>
                <span>₹{calculateSubtotal().toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-xs text-slate-400 uppercase tracking-widest font-bold">
                <span>GST Amount</span>
                <span className="text-amber-400">₹{calculateGST().toFixed(2)}</span>
              </div>
              <div className="border-t border-white/10 pt-3 flex justify-between text-xl font-black">
                <span className="tracking-tighter uppercase">Net Amount</span>
                <span className="text-blue-400">₹{calculateNet().toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </AppModal>

      <AppModal isOpen={isInvoiceModalOpen} onClose={() => setIsInvoiceModalOpen(false)} maxWidth="sm:max-w-4xl" padding={false}>
        <PharmacyInvoice bill={selectedInvoice} onClose={() => setIsInvoiceModalOpen(false)} />
      </AppModal>

      <AppModal isOpen={isDeleteModalOpen} onClose={() => setIsDeleteModalOpen(false)} title="Confirm Cancellation" maxWidth="sm:max-w-md" footer={
          <div className="flex gap-3 w-full">
            <button onClick={() => setIsDeleteModalOpen(false)} className="flex-1 px-6 py-2 border rounded-xl font-bold text-gray-500">No, Keep Bill</button>
            <button onClick={cancelBill} className="flex-1 px-6 py-2 bg-red-600 text-white rounded-xl font-bold shadow-lg shadow-red-200">Yes, Cancel Bill</button>
          </div>
        }>
        <div className="p-8 text-center">
          <XCircle className="w-16 h-16 text-red-600 mx-auto mb-4" />
          <h3 className="text-xl font-bold mb-2">Are you sure?</h3>
          <p className="text-gray-500 text-sm">This action will cancel the bill and return stock to inventory.</p>
        </div>
      </AppModal>
    </div>
  );
}
