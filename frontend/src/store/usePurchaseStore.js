import { create } from 'zustand';
import pharmacyService from '../utils/pharmacyService';
import { toast } from 'react-hot-toast';

export const usePurchaseStore = create((set, get) => ({
  // Purchase Orders State
  poList: [],
  poLoading: false,
  poError: false,
  poPage: 0,
  poTotalElements: 0,
  poTotalPages: 0,
  poSearchTerm: '',
  poStatusFilter: 'ALL',
  poDateRange: { from: null, to: null },
  _poSearchTimer: null,

  setPoSearch: (term) => {
    set({ poSearchTerm: term });
    const timer = get()._poSearchTimer;
    if (timer) clearTimeout(timer);
    
    set({
      _poSearchTimer: setTimeout(() => {
        set({ poPage: 0 });
        get().fetchPurchaseOrders();
      }, 400)
    });
  },

  setPoDateRange: (range) => {
    set({ poDateRange: range, poPage: 0 });
    get().fetchPurchaseOrders();
  },

  setPoStatusFilter: (status) => {
    set({ poStatusFilter: status, poPage: 0 });
    get().fetchPurchaseOrders();
  },

  setPoPage: (page) => {
    set({ poPage: page });
    get().fetchPurchaseOrders();
  },

  fetchPurchaseOrders: async () => {
    const { poPage, poSearchTerm, poDateRange, poStatusFilter } = get();
    set({ poLoading: true, poError: false });
    try {
      const params = {
        page: poPage,
        size: 20,
        searchTerm: poSearchTerm || '',
        fromDate: poDateRange.from ? poDateRange.from.toISOString() : '',
        toDate: poDateRange.to ? poDateRange.to.toISOString() : '',
        status: poStatusFilter === 'ALL' ? undefined : poStatusFilter
      };
      
      const res = await pharmacyService.api.get('/pharmacy/purchase-orders', { params });
      
      set({ 
        poList: res.data?.data?.content || [],
        poTotalElements: res.data?.data?.totalElements || 0,
        poTotalPages: res.data?.data?.totalPages || 0,
        poLoading: false 
      });
    } catch (err) {
      set({ poError: true, poLoading: false });
    }
  },

  // Purchase Order Detail State
  selectedPo: null,
  poDetailLoading: false,
  poDetailError: false,

  fetchPoDetail: async (poId) => {
    set({ poDetailLoading: true, poDetailError: false });
    try {
      const res = await pharmacyService.api.get(`/pharmacy/purchase-orders/${poId}`);
      set({ selectedPo: res.data?.data || res.data, poDetailLoading: false });
    } catch (err) {
      set({ poDetailError: true, poDetailLoading: false });
      toast.error('Failed to load purchase order details');
    }
  },

  clearPoDetail: () => {
    set({ selectedPo: null, poDetailLoading: false, poDetailError: false });
  }

}));
