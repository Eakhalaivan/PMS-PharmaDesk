import { create } from 'zustand';
import pharmacyService from '../utils/pharmacyService';
import { toast } from 'react-hot-toast';

export const useSalesStore = create((set, get) => ({
  // Pharmacy Sales State
  salesList: [],
  salesLoading: false,
  salesError: false,
  salesPage: 0,
  salesTotalElements: 0,
  salesTotalPages: 0,
  salesSearchTerm: '',
  salesDateRange: { from: null, to: null },
  _salesSearchTimer: null,

  // Direct Pharmacy Sales State
  directSalesList: [],
  directSalesLoading: false,
  directSalesError: false,
  directSalesPage: 0,
  directSalesTotalElements: 0,
  directSalesTotalPages: 0,
  directSalesSearchTerm: '',
  directSalesDateRange: { from: null, to: null },
  _directSalesSearchTimer: null,

  // Actions for Pharmacy Sales
  setSalesSearch: (term) => {
    set({ salesSearchTerm: term });
    const timer = get()._salesSearchTimer;
    if (timer) clearTimeout(timer);
    
    set({
      _salesSearchTimer: setTimeout(() => {
        set({ salesPage: 0 });
        get().fetchSales();
      }, 400)
    });
  },

  setSalesDateRange: (range) => {
    set({ salesDateRange: range, salesPage: 0 });
    get().fetchSales();
  },

  setSalesPage: (page) => {
    set({ salesPage: page });
    get().fetchSales();
  },

  fetchSales: async () => {
    const { salesPage, salesSearchTerm, salesDateRange } = get();
    set({ salesLoading: true, salesError: false });
    try {
      const params = {
        page: salesPage,
        size: 20,
        searchTerm: salesSearchTerm || '',
        fromDate: salesDateRange.from ? salesDateRange.from.toISOString() : '',
        toDate: salesDateRange.to ? salesDateRange.to.toISOString() : ''
      };
      
      const res = await pharmacyService.api.get('/pharmacy/sales', { params });
      
      set({ 
        salesList: res.data?.data?.content || [],
        salesTotalElements: res.data?.data?.totalElements || 0,
        salesTotalPages: res.data?.data?.totalPages || 0,
        salesLoading: false 
      });
    } catch (err) {
      set({ salesError: true, salesLoading: false });
    }
  },

  // Actions for Direct Pharmacy Sales
  setDirectSalesSearch: (term) => {
    set({ directSalesSearchTerm: term });
    const timer = get()._directSalesSearchTimer;
    if (timer) clearTimeout(timer);
    
    set({
      _directSalesSearchTimer: setTimeout(() => {
        set({ directSalesPage: 0 });
        get().fetchDirectSales();
      }, 400)
    });
  },

  setDirectSalesDateRange: (range) => {
    set({ directSalesDateRange: range, directSalesPage: 0 });
    get().fetchDirectSales();
  },

  setDirectSalesPage: (page) => {
    set({ directSalesPage: page });
    get().fetchDirectSales();
  },

  fetchDirectSales: async () => {
    const { directSalesPage, directSalesSearchTerm, directSalesDateRange } = get();
    set({ directSalesLoading: true, directSalesError: false });
    try {
      const params = {
        page: directSalesPage,
        size: 50, // Get more since we might filter locally if backend doesn't support billType
        searchTerm: directSalesSearchTerm || '',
        fromDate: directSalesDateRange.from ? directSalesDateRange.from.toISOString() : '',
        toDate: directSalesDateRange.to ? directSalesDateRange.to.toISOString() : ''
      };
      
      const res = await pharmacyService.api.get('/pharmacy/sales', { params });
      const salesData = res.data?.data?.content || res.data?.data || [];
      const otcSales = Array.isArray(salesData) ? salesData.filter(s => s.billType === 'OTC') : [];
      
      set({ 
        directSalesList: otcSales,
        directSalesTotalElements: otcSales.length,
        directSalesTotalPages: Math.ceil(otcSales.length / 50) || 1,
        directSalesLoading: false 
      });
    } catch (err) {
      set({ directSalesError: true, directSalesLoading: false });
    }
  },

  // Dispense Worklists State
  worklists: [],
  worklistsLoading: false,
  worklistsError: false,
  worklistsSearchTerm: '',
  worklistsDateRange: { from: null, to: null },
  _worklistsSearchTimer: null,

  setWorklistsSearch: (term) => {
    set({ worklistsSearchTerm: term });
    const timer = get()._worklistsSearchTimer;
    if (timer) clearTimeout(timer);
    
    set({
      _worklistsSearchTimer: setTimeout(() => {
        get().fetchWorklists();
      }, 400)
    });
  },

  setWorklistsDateRange: (range) => {
    set({ worklistsDateRange: range });
    get().fetchWorklists();
  },

  fetchWorklists: async () => {
    set({ worklistsLoading: true, worklistsError: false });
    try {
      // Assuming endpoint, modify if different
      const res = await pharmacyService.api.get('/pharmacy/prescriptions/dispense-worklists');
      const data = res.data?.data || [];
      const term = get().worklistsSearchTerm.toLowerCase();
      const dateRange = get().worklistsDateRange;
      
      const filtered = data.filter(w => {
        const matchesSearch = !term || w.patientName?.toLowerCase().includes(term) || w.prescriptionNumber?.toLowerCase().includes(term) || w.id?.toString().includes(term);
        
        let matchesFrom = true;
        let matchesTo = true;
        
        if (dateRange.from || dateRange.to) {
          const prDate = new Date(w.prescriptionDate);
          const normalizedPrDate = new Date(prDate.getFullYear(), prDate.getMonth(), prDate.getDate()).getTime();
          if (dateRange.from) {
             matchesFrom = normalizedPrDate >= new Date(dateRange.from.getFullYear(), dateRange.from.getMonth(), dateRange.from.getDate()).getTime();
          }
          if (dateRange.to) {
             matchesTo = normalizedPrDate <= new Date(dateRange.to.getFullYear(), dateRange.to.getMonth(), dateRange.to.getDate()).getTime();
          }
        }
        return matchesSearch && matchesFrom && matchesTo;
      });

      set({ worklists: filtered, worklistsLoading: false });
    } catch (err) {
      set({ worklistsError: true, worklistsLoading: false });
    }
  },

  // Pending Prescriptions State
  prescriptions: [],
  prescriptionsLoading: false,
  prescriptionsError: false,
  prescriptionsSearchTerm: '',
  prescriptionsDateRange: { from: null, to: null },
  _prescriptionsSearchTimer: null,

  setPrescriptionsSearch: (term) => {
    set({ prescriptionsSearchTerm: term });
    const timer = get()._prescriptionsSearchTimer;
    if (timer) clearTimeout(timer);
    
    set({
      _prescriptionsSearchTimer: setTimeout(() => {
        get().fetchPrescriptions();
      }, 400)
    });
  },

  setPrescriptionsDateRange: (range) => {
    set({ prescriptionsDateRange: range });
    get().fetchPrescriptions();
  },

  fetchPrescriptions: async () => {
    set({ prescriptionsLoading: true, prescriptionsError: false });
    try {
      const res = await pharmacyService.api.get('/pharmacy/prescriptions/pending');
      const data = res.data?.data || [];
      const term = get().prescriptionsSearchTerm.toLowerCase();
      const dateRange = get().prescriptionsDateRange;
      
      const filtered = data.filter(p => {
        const matchesSearch = !term || p.patientName?.toLowerCase().includes(term) || p.uhid?.toLowerCase().includes(term) || p.id?.toString().includes(term);
        
        let matchesFrom = true;
        let matchesTo = true;
        
        if (dateRange.from || dateRange.to) {
          const prDate = new Date(p.prescriptionDate);
          const normalizedPrDate = new Date(prDate.getFullYear(), prDate.getMonth(), prDate.getDate()).getTime();
          if (dateRange.from) {
             matchesFrom = normalizedPrDate >= new Date(dateRange.from.getFullYear(), dateRange.from.getMonth(), dateRange.from.getDate()).getTime();
          }
          if (dateRange.to) {
             matchesTo = normalizedPrDate <= new Date(dateRange.to.getFullYear(), dateRange.to.getMonth(), dateRange.to.getDate()).getTime();
          }
        }
        return matchesSearch && matchesFrom && matchesTo;
      });

      set({ prescriptions: filtered, prescriptionsLoading: false });
    } catch (err) {
      set({ prescriptionsError: true, prescriptionsLoading: false });
    }
  }
}));
