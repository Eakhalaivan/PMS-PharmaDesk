import React, { useState, useEffect } from 'react';
import { Plus, Search, Eye, Edit3, Pill, Save, CheckCircle, Barcode, AlertTriangle, ShieldAlert } from 'lucide-react';
import ModuleFilterBar from '../components/ui/ModuleFilterBar';
import DataTable from '../components/ui/DataTable';
import Pagination from '../components/ui/Pagination';
import AppModal from '../components/ui/AppModal';
import Badge from '../components/ui/Badge';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';
import { cn } from '../utils/cn';

const TABS = ['Basic Info', 'Pricing & Tax', 'Stock Settings', 'Clinical Details', 'Storage & Handling', 'Barcode'];

const DRUG_CLASSES = ['Analgesic', 'Antibiotic', 'Antidiabetic', 'Antihypertensive', 'Antihistamine', 'Antifungal', 'Antiviral', 'Cardiac', 'Hormonal', 'Lipid-Lowering', 'Nutritional Supplement', 'Psychotropic', 'Vaccine', 'Others'];
const SCHEDULES = ['OTC', 'Schedule H', 'Schedule H1', 'Schedule X', 'Narcotic'];
const STORAGES = ['Room Temperature (15–25°C)', 'Refrigerated (2–8°C)', 'Frozen (below 0°C)', 'Cool and Dry', 'Protect from Light', 'Flammable / Special Handling'];
const CATEGORIES = ['Tablet', 'Capsule', 'Syrup', 'Injection', 'Ointment', 'Drops', 'Vial', 'Cream', 'Inhaler'];

export default function MedicineMaster() {
  const [medicines, setMedicines] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [activeModalTab, setActiveModalTab] = useState('Basic Info');
  const [selectedMedicineId, setSelectedMedicineId] = useState(null);

  // Table & Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [drugClassFilter, setDrugClassFilter] = useState('ALL');
  const [scheduleFilter, setScheduleFilter] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [visibleColumns, setVisibleColumns] = useState({
    code: true, hsn: false, mrp: true, stock: true, generic: true, manufacturer: true, barcode: false
  });

  const [formData, setFormData] = useState({
    name: '', genericName: '', medicineCode: '', manufacturer: '', supplierVendor: '',
    packSize: '', unit: 'Strip', category: 'Tablet', mrp: '', purchasePrice: '', salePrice: '',
    hsnCode: '', taxPercentage: 12.0, reorderLevel: 10, drugClass: 'Analgesic', storageConditions: 'Room Temperature (15–25°C)',
    schedule: 'OTC', substitutes: '', barcode: ''
  });

  useEffect(() => {
    fetchMedicines();
  }, []);

  const fetchMedicines = async () => {
    setLoading(true);
    try {
      const response = await pharmacyService.getMedicines();
      if (response.success || response.data) {
        setMedicines(response.data || response);
      }
    } catch (error) {
      toast.error('Failed to fetch medicines');
    } finally {
      setLoading(false);
    }
  };

  const openModal = (medicine = null) => {
    if (medicine) {
      setIsEditMode(true);
      setSelectedMedicineId(medicine.id);
      setFormData({
        name: medicine.name || '',
        genericName: medicine.genericName || '',
        medicineCode: medicine.medicineCode || '',
        manufacturer: medicine.manufacturer || '',
        supplierVendor: medicine.supplierVendor || '',
        packSize: medicine.packSize || '',
        unit: medicine.unit || 'Strip',
        category: medicine.category || 'Tablet',
        mrp: medicine.mrp || '',
        purchasePrice: medicine.purchasePrice || '',
        salePrice: medicine.salePrice || '',
        hsnCode: medicine.hsnCode || '',
        taxPercentage: medicine.taxPercentage || 0,
        reorderLevel: medicine.reorderLevel || 10,
        drugClass: medicine.drugClass || 'Analgesic',
        storageConditions: medicine.storageConditions || 'Room Temperature (15–25°C)',
        schedule: medicine.schedule || 'OTC',
        substitutes: medicine.substitutes || '',
        barcode: medicine.barcode || ''
      });
    } else {
      setIsEditMode(false);
      setSelectedMedicineId(null);
      setFormData({
        name: '', genericName: '', medicineCode: '', manufacturer: '', supplierVendor: '',
        packSize: '', unit: 'Strip', category: 'Tablet', mrp: '', purchasePrice: '', salePrice: '',
        hsnCode: '', taxPercentage: 12.0, reorderLevel: 10, drugClass: 'Analgesic', storageConditions: 'Room Temperature (15–25°C)',
        schedule: 'OTC', substitutes: '', barcode: ''
      });
    }
    setActiveModalTab('Basic Info');
    setIsModalOpen(true);
  };

  const handleSave = async () => {
    if (!formData.name || !formData.genericName || !formData.hsnCode) {
      toast.error('Please fill all mandatory fields (amber)');
      return;
    }
    
    try {
      if (isEditMode) {
        await pharmacyService.updateMedicine(selectedMedicineId, formData);
        toast.success('Medicine updated successfully!');
      } else {
        await pharmacyService.createMedicine(formData);
        toast.success('Medicine registered successfully!');
      }
      setIsModalOpen(false);
      fetchMedicines();
    } catch (error) {
      toast.error('Operation failed. Check details.');
    }
  };

  // Derived styling for preview
  const isHighAlert = ['Schedule H1', 'Schedule X', 'Narcotic'].includes(formData.schedule);
  const isColdChain = ['Refrigerated (2–8°C)', 'Frozen (below 0°C)'].includes(formData.storageConditions);

  // Filters
  const filteredMedicines = medicines.filter(m => {
    const s = searchTerm.toLowerCase();
    const matchesSearch = !searchTerm || (m.name?.toLowerCase().includes(s) || m.genericName?.toLowerCase().includes(s));
    const matchesClass = drugClassFilter === 'ALL' || m.drugClass === drugClassFilter;
    const matchesSchedule = scheduleFilter === 'ALL' || m.schedule === scheduleFilter;
    return matchesSearch && matchesClass && matchesSchedule;
  });

  const paginatedMedicines = pageSize === 'All' ? filteredMedicines : filteredMedicines.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  const columns = [
    { header: 'S.No', render: (r, i) => <span className="text-slate-500 font-medium">{(currentPage - 1) * (pageSize === 'All' ? 0 : pageSize) + i + 1}</span> },
    { header: 'Code', accessor: 'medicineCode', render: (r) => <span className="font-mono text-xs">{r.medicineCode || '-'}</span> },
    { header: 'Medicine Name', render: (r) => <span className="font-medium text-slate-900 whitespace-nowrap">{r.name}</span> },
    { header: 'Generic Name', render: (r) => <span className="text-slate-600 whitespace-nowrap">{r.genericName}</span> },
    { header: 'Manufacturer', accessor: 'manufacturer', render: (r) => <span className="text-slate-600 whitespace-nowrap">{r.manufacturer || '-'}</span> },
    { header: 'Category', accessor: 'category', render: (r) => <span className="text-slate-600">{r.category || '-'}</span> },
    { header: 'Unit', accessor: 'unit', render: (r) => <span className="text-slate-600">{r.unit || '-'}</span> },
    { header: 'Stock Count', render: (r) => (
      <Badge variant={(r.currentStock || 0) <= (r.reorderLevel || 10) ? 'danger' : 'success'}>
        {r.currentStock || 0}
      </Badge>
    )},
    { header: 'MRP', render: (r) => <span className="text-sm font-medium">₹{r.mrp || 0}</span> },
    { header: 'GST %', render: (r) => <span className="text-sm text-slate-600">{r.taxPercentage || 0}%</span> },
    { header: 'Schedule', render: (r) => (
      <div className={cn("text-xs font-bold whitespace-nowrap", ['Schedule H1', 'Schedule X', 'Narcotic'].includes(r.schedule) ? "text-red-600" : "text-slate-500")}>
        {r.schedule || 'OTC'}
      </div>
    )},
    { header: 'Action', render: (row) => (
      <div className="flex gap-2">
        <button onClick={() => openModal(row)} className="p-1.5 text-blue-600 hover:bg-blue-50 rounded border border-transparent transition-colors">
          <Edit3 className="w-4 h-4" />
        </button>
      </div>
    )}
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-medium tracking-tight text-slate-900">Medicine Master</h2>
        <p className="text-sm text-slate-500 font-normal">Central registry of all medicines, pricing, clinical details, and stock triggers.</p>
      </div>

      <div className="bg-white p-4 rounded-lg border border-slate-200 flex flex-wrap gap-4 items-center shadow-sm">
        <div className="flex-1 min-w-[250px]">
          <ModuleFilterBar onSearch={setSearchTerm} searchValue={searchTerm} hideDateRange={true} />
        </div>
        <select value={drugClassFilter} onChange={(e) => setDrugClassFilter(e.target.value)} className="px-3 py-2 bg-slate-50 border border-slate-200 rounded-md text-sm outline-none">
          <option value="ALL">All Drug Classes</option>
          {DRUG_CLASSES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        <select value={scheduleFilter} onChange={(e) => setScheduleFilter(e.target.value)} className="px-3 py-2 bg-slate-50 border border-slate-200 rounded-md text-sm outline-none">
          <option value="ALL">All Schedules</option>
          {SCHEDULES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        


        <button onClick={() => openModal()} className="px-5 py-2 bg-[#1a3c6e] text-white rounded-md text-sm font-medium hover:bg-[#122b50] flex items-center gap-2">
          <Plus className="w-4 h-4" /> Add Medicine
        </button>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg overflow-hidden shadow-sm">
        {loading && medicines.length === 0 ? (
          <div className="p-10 text-center text-slate-500">Loading registry...</div>
        ) : (
          <>
            <DataTable columns={columns} data={paginatedMedicines} striped />
            {filteredMedicines.length > 0 && (
              <Pagination totalRecords={filteredMedicines.length} currentPage={currentPage} pageSize={pageSize} onPageChange={setCurrentPage} onPageSizeChange={setPageSize} />
            )}
          </>
        )}
      </div>

      <AppModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        title={isEditMode ? "Edit Medicine Record" : "Register New Medicine"}
        maxWidth="sm:max-w-6xl"
        footer={
          <div className="flex justify-end gap-3 w-full border-t border-slate-100 pt-4">
            <button onClick={() => setIsModalOpen(false)} className="px-6 py-2 border border-slate-200 rounded-md text-sm font-medium text-slate-700 hover:bg-slate-50">Cancel</button>
            <button onClick={handleSave} className="px-8 py-2 bg-[#1a3c6e] text-white rounded-md text-sm font-medium hover:bg-[#122b50] flex items-center gap-2">
              <Save className="w-4 h-4" /> {isEditMode ? 'Save Changes' : 'Register Medicine'}
            </button>
          </div>
        }
      >
        <div className="flex flex-col lg:flex-row gap-6">
          {/* LEFT COLUMN: FORM */}
          <div className="flex-1 border border-slate-200 rounded-lg bg-white overflow-hidden flex flex-col h-[600px]">
            {/* TABS */}
            <div className="flex overflow-x-auto border-b border-slate-200 bg-slate-50">
              {TABS.map(tab => (
                <button
                  key={tab}
                  onClick={() => setActiveModalTab(tab)}
                  className={cn("px-4 py-3 text-sm font-medium whitespace-nowrap border-b-2 transition-colors outline-none", 
                    activeModalTab === tab ? "border-[#1a3c6e] text-[#1a3c6e] bg-white" : "border-transparent text-slate-500 hover:text-slate-800"
                  )}
                >
                  {tab}
                </button>
              ))}
            </div>

            {/* TAB CONTENT */}
            <div className="p-6 overflow-y-auto flex-1">
              
              {activeModalTab === 'Basic Info' && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Brand Name <span className="text-amber-500">*</span></label>
                    <input type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full px-3 py-2 border border-amber-200 rounded-md bg-amber-50/20 focus:outline-none focus:border-amber-400" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Generic Name <span className="text-amber-500">*</span></label>
                    <input type="text" value={formData.genericName} onChange={e => setFormData({...formData, genericName: e.target.value})} className="w-full px-3 py-2 border border-amber-200 rounded-md bg-amber-50/20 focus:outline-none focus:border-amber-400" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Medicine Code</label>
                    <input type="text" value={formData.medicineCode} placeholder="Auto-generated if empty" disabled={isEditMode && formData.medicineCode} onChange={e => setFormData({...formData, medicineCode: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md bg-slate-50 font-mono" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Manufacturer</label>
                    <input type="text" value={formData.manufacturer} onChange={e => setFormData({...formData, manufacturer: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none focus:border-[#1a3c6e]" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Primary Vendor</label>
                    <input type="text" value={formData.supplierVendor} onChange={e => setFormData({...formData, supplierVendor: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none focus:border-[#1a3c6e]" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Form Category</label>
                    <select value={formData.category} onChange={e => setFormData({...formData, category: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none">
                      {CATEGORIES.map(c => <option key={c}>{c}</option>)}
                    </select>
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Unit of Measure</label>
                    <select value={formData.unit} onChange={e => setFormData({...formData, unit: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none">
                      <option>Strip</option><option>Bottle</option><option>Vial</option><option>Ampoule</option><option>Tube</option>
                    </select>
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Pack Size</label>
                    <input type="text" placeholder="e.g. 10 tablets/strip" value={formData.packSize} onChange={e => setFormData({...formData, packSize: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none focus:border-[#1a3c6e]" />
                  </div>
                </div>
              )}

              {activeModalTab === 'Pricing & Tax' && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Purchase Price (per unit)</label>
                    <input type="number" step="0.01" value={formData.purchasePrice} onChange={e => setFormData({...formData, purchasePrice: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none focus:border-[#1a3c6e]" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Sale Price (per unit)</label>
                    <input type="number" step="0.01" value={formData.salePrice} onChange={e => setFormData({...formData, salePrice: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none focus:border-[#1a3c6e]" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">MRP (per unit)</label>
                    <input type="number" step="0.01" value={formData.mrp} onChange={e => setFormData({...formData, mrp: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none focus:border-[#1a3c6e]" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">HSN Code <span className="text-amber-500">*</span></label>
                    <input type="text" maxLength={6} placeholder="6-digit compliance code" value={formData.hsnCode} onChange={e => setFormData({...formData, hsnCode: e.target.value})} className="w-full px-3 py-2 border border-amber-200 rounded-md bg-amber-50/20 font-mono outline-none focus:border-amber-400" />
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">GST Percentage</label>
                    <select value={formData.taxPercentage} onChange={e => setFormData({...formData, taxPercentage: parseFloat(e.target.value)})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none">
                      <option value="0">0%</option>
                      <option value="5">5%</option>
                      <option value="12">12%</option>
                      <option value="18">18%</option>
                    </select>
                  </div>
                </div>
              )}

              {activeModalTab === 'Stock Settings' && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Reorder Level Alert Threshold</label>
                    <input type="number" value={formData.reorderLevel} onChange={e => setFormData({...formData, reorderLevel: parseInt(e.target.value)})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none focus:border-[#1a3c6e]" />
                    <p className="text-xs text-slate-500">System alerts when stock falls below this quantity.</p>
                  </div>
                </div>
              )}

              {activeModalTab === 'Clinical Details' && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div className="space-y-1.5">
                      <label className="text-sm font-medium text-slate-700">Drug Class</label>
                      <select value={formData.drugClass} onChange={e => setFormData({...formData, drugClass: e.target.value})} className="w-full px-3 py-2 border border-slate-200 rounded-md outline-none">
                        {DRUG_CLASSES.map(c => <option key={c}>{c}</option>)}
                      </select>
                    </div>
                    <div className="space-y-1.5">
                      <label className="text-sm font-medium text-slate-700">Schedule / Regulatory Class</label>
                      <select value={formData.schedule} onChange={e => setFormData({...formData, schedule: e.target.value})} className={cn("w-full px-3 py-2 rounded-md outline-none border", isHighAlert ? "border-red-300 bg-red-50 text-red-900" : "border-slate-200")}>
                        {SCHEDULES.map(c => <option key={c}>{c}</option>)}
                      </select>
                      {isHighAlert && <p className="text-xs text-red-600 font-medium flex items-center gap-1 mt-1"><AlertTriangle className="w-3 h-3"/> Mandatory compliance logging required at dispensing.</p>}
                    </div>
                  </div>
                  <div className="space-y-1.5 pt-4 border-t border-slate-100">
                    <label className="text-sm font-medium text-blue-600 flex items-center gap-2">Substitute Links</label>
                    <p className="text-xs text-slate-500 mb-2">Enter comma-separated Medicine Codes or IDs of direct substitutes.</p>
                    <input type="text" placeholder="e.g. MED-1045, MED-2091" value={formData.substitutes} onChange={e => setFormData({...formData, substitutes: e.target.value})} className="w-full px-3 py-2 border border-blue-200 rounded-md outline-none focus:border-blue-500 font-mono" />
                  </div>
                </div>
              )}

              {activeModalTab === 'Storage & Handling' && (
                <div className="space-y-5">
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Storage Conditions</label>
                    <select value={formData.storageConditions} onChange={e => setFormData({...formData, storageConditions: e.target.value})} className={cn("w-full px-3 py-2 rounded-md outline-none border", isColdChain ? "border-blue-300 bg-blue-50" : "border-slate-200")}>
                      {STORAGES.map(c => <option key={c}>{c}</option>)}
                    </select>
                  </div>
                  {isColdChain && (
                    <div className="p-4 bg-blue-50 border border-blue-200 rounded-md flex items-start gap-3">
                      <ShieldAlert className="w-5 h-5 text-blue-600 shrink-0" />
                      <div>
                        <h4 className="text-sm font-medium text-blue-900">Cold Chain Monitoring Enabled</h4>
                        <p className="text-xs text-blue-700 mt-1">This item will be flagged for temperature log tracking in the inventory module.</p>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {activeModalTab === 'Barcode' && (
                <div className="space-y-5">
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-slate-700">Item Barcode (EAN-13 / Custom)</label>
                    <div className="flex gap-2">
                      <input type="text" placeholder="Scan or type barcode" value={formData.barcode} onChange={e => setFormData({...formData, barcode: e.target.value})} className="flex-1 px-3 py-2 border border-slate-200 rounded-md outline-none font-mono" />
                      <button className="px-4 py-2 bg-blue-50 text-blue-600 border border-blue-200 rounded-md text-sm font-medium flex items-center gap-2 hover:bg-blue-100">
                        <Barcode className="w-4 h-4" /> Scan
                      </button>
                    </div>
                  </div>
                </div>
              )}

            </div>
          </div>

          {/* RIGHT COLUMN: PREVIEW CARD */}
          <div className="w-full lg:w-80 shrink-0 bg-white border border-slate-200 rounded-lg p-5 flex flex-col h-[600px] overflow-y-auto">
            <h3 className="text-xs font-medium text-slate-400 uppercase tracking-widest mb-4">Registry Preview</h3>
            
            <div className={cn("rounded-lg border p-4 mb-4", isHighAlert ? "border-red-200 bg-red-50/30" : "border-slate-200")}>
              <div className="flex justify-between items-start mb-2">
                <span className="text-xs font-mono font-medium text-slate-500 bg-white border border-slate-200 px-1.5 rounded">{formData.medicineCode || 'MED-XXXX'}</span>
                {isHighAlert && <Badge variant="danger">{formData.schedule}</Badge>}
              </div>
              <h2 className="text-xl font-medium text-slate-900 leading-tight mb-1">{formData.name || 'Medicine Name'}</h2>
              <p className="text-sm text-[#1a3c6e] font-medium">{formData.genericName || 'Generic Salt Name'}</p>
              
              <div className="mt-4 pt-4 border-t border-slate-200/60 grid grid-cols-2 gap-3 text-sm">
                <div>
                  <span className="block text-[10px] text-slate-400 uppercase">Class</span>
                  <span className="font-medium text-slate-700">{formData.drugClass}</span>
                </div>
                <div>
                  <span className="block text-[10px] text-slate-400 uppercase">Category</span>
                  <span className="font-medium text-slate-700">{formData.category} ({formData.unit})</span>
                </div>
                <div className="col-span-2">
                  <span className="block text-[10px] text-slate-400 uppercase">Storage</span>
                  <span className="font-medium text-slate-700">{formData.storageConditions}</span>
                </div>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between border-b border-slate-100 pb-2">
                <span className="text-sm text-slate-500">MRP</span>
                <span className="text-sm font-medium text-slate-900">₹{formData.mrp || '0.00'}</span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2">
                <span className="text-sm text-slate-500">GST</span>
                <span className="text-sm font-medium text-slate-900">{formData.taxPercentage}% (HSN: {formData.hsnCode || '---'})</span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2">
                <span className="text-sm text-slate-500">Pack</span>
                <span className="text-sm font-medium text-slate-900">{formData.packSize || '-'}</span>
              </div>
              {formData.barcode && (
                <div className="pt-2 flex flex-col items-center">
                  <Barcode className="w-16 h-8 text-slate-800" />
                  <span className="text-xs font-mono mt-1 text-slate-500">{formData.barcode}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </AppModal>
    </div>
  );
}
