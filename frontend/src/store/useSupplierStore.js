import { create } from 'zustand';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';

const DEBOUNCE_MS = 400;

export const useSupplierStore = create((set, get) => ({
  suppliers: [],
  loading: false,
  error: null,
  searchTerm: '',
  filterType: '',
  filterStatus: '',
  _searchTimer: null,

  setSearch: (searchTerm) => {
    set({ searchTerm });
    const prev = get()._searchTimer;
    if (prev) clearTimeout(prev);
    const timer = setTimeout(() => get().fetchSuppliers(), DEBOUNCE_MS);
    set({ _searchTimer: timer });
  },

  setFilterType: (filterType) => {
    set({ filterType });
    get().fetchSuppliers();
  },

  setFilterStatus: (filterStatus) => {
    set({ filterStatus });
    get().fetchSuppliers();
  },

  fetchSuppliers: async () => {
    const { searchTerm, filterType, filterStatus } = get();
    set({ loading: true, error: null });
    try {
      const res = await pharmacyService.getSuppliers({
        search: searchTerm || undefined,
        type: filterType || undefined,
        status: filterStatus || undefined,
      });
      const list = res?.data?.content ?? res?.data ?? res ?? [];
      set({ suppliers: Array.isArray(list) ? list : [] });
    } catch {
      set({ error: 'Failed to load suppliers' });
      toast.error('Failed to load suppliers');
    } finally {
      set({ loading: false });
    }
  },

  createSupplier: async (formData) => {
    try {
      const res = await pharmacyService.createSupplier(formData);
      if (res?.success) {
        toast.success('Supplier added!');
        get().fetchSuppliers();
        return true;
      }
    } catch {
      toast.error('Failed to add supplier');
    }
    return false;
  },

  updateSupplier: async (id, formData) => {
    try {
      const res = await pharmacyService.updateSupplier(id, formData);
      if (res?.success) {
        toast.success('Supplier updated!');
        get().fetchSuppliers();
        return true;
      }
    } catch {
      toast.error('Failed to update supplier');
    }
    return false;
  },

  deleteSupplier: async (id) => {
    try {
      const res = await pharmacyService.deleteSupplier(id);
      if (res?.success) {
        toast.success('Supplier deleted');
        get().fetchSuppliers();
        return true;
      }
    } catch {
      toast.error('Failed to delete supplier');
    }
    return false;
  },

  // Missing in instructions, but used in component
  filteredSuppliers: () => {
      // Component filters it itself now
      return get().suppliers;
  }
}));
