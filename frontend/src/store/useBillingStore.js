import { create } from 'zustand';
import pharmacyService from '../utils/pharmacyService';
import { toast } from 'react-hot-toast';

export const useBillingStore = create((set, get) => ({
  // Credit Bills State
  creditBillsList: [],
  creditBillsLoading: false,

  fetchCreditBills: async () => {
    set({ creditBillsLoading: true });
    try {
      const response = await pharmacyService.getCreditBills();
      if (response && response.success) {
        set({ creditBillsList: Array.isArray(response.data) ? response.data : [], creditBillsLoading: false });
      } else {
        set({ creditBillsList: [], creditBillsLoading: false });
      }
    } catch (error) {
      console.error('Credit Bills Error:', error);
      set({ creditBillsList: [], creditBillsLoading: false });
    }
  },

  // Insurance Claims State
  claims: [],
  providers: [],
  claimsLoading: false,

  loadClaimsData: async () => {
    set({ claimsLoading: true });
    try {
      const cRes = await pharmacyService.getInsuranceClaims();
      const pRes = await pharmacyService.getInsuranceProviders();
      
      set({
        claims: cRes.data || cRes || [],
        providers: pRes.data || pRes || [],
        claimsLoading: false
      });
    } catch {
      toast.error('Failed to load insurance claims data');
      set({ claimsLoading: false });
    }
  },

  // Pharmacy Advances State
  advancesList: [],
  advancesLoading: false,

  fetchAdvances: async () => {
    set({ advancesLoading: true });
    try {
      const response = await pharmacyService.getAllAdvances();
      if (response && response.success) {
        set({ advancesList: Array.isArray(response.data) ? response.data : [], advancesLoading: false });
      } else {
        set({ advancesList: [], advancesLoading: false });
      }
    } catch (error) {
      console.error('Pharmacy Advances Error:', error);
      set({ advancesList: [], advancesLoading: false });
    }
  }
}));
