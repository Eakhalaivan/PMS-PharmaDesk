import { create } from 'zustand';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';

const DEBOUNCE_MS = 400;

export const useMedicineStore = create((set, get) => ({
  // Data
  medicines: [],
  totalElements: 0,
  totalPages: 0,
  loading: false,
  error: null,

  // Filters (server-side)
  page: 0,
  pageSize: 20,
  searchTerm: '',
  drugClassFilter: 'ALL',
  scheduleFilter: 'ALL',

  // Internal debounce timer
  _searchTimer: null,

  // Actions
  setPage: (page) => {
    set({ page });
    get().fetchMedicines();
  },

  setPageSize: (pageSize) => {
    set({ pageSize, page: 0 });
    get().fetchMedicines();
  },

  setSearch: (searchTerm) => {
    set({ searchTerm, page: 0 });
    const prev = get()._searchTimer;
    if (prev) clearTimeout(prev);
    const timer = setTimeout(() => get().fetchMedicines(), DEBOUNCE_MS);
    set({ _searchTimer: timer });
  },

  setDrugClassFilter: (drugClassFilter) => {
    set({ drugClassFilter, page: 0 });
    get().fetchMedicines();
  },

  setScheduleFilter: (scheduleFilter) => {
    set({ scheduleFilter, page: 0 });
    get().fetchMedicines();
  },

  fetchMedicines: async () => {
    const { page, pageSize, searchTerm, drugClassFilter, scheduleFilter } = get();
    set({ loading: true, error: null });
    try {
      const res = await pharmacyService.getMedicines({
        page,
        size: pageSize,
        search: searchTerm || undefined,
        drugClass: drugClassFilter !== 'ALL' ? drugClassFilter : undefined,
        schedule: scheduleFilter !== 'ALL' ? scheduleFilter : undefined,
      });
      const payload = res?.data ?? res;
      // Handle Spring Page wrapper or flat array
      if (payload?.content) {
        set({
          medicines: payload.content,
          totalElements: payload.totalElements ?? 0,
          totalPages: payload.totalPages ?? 0,
        });
      } else {
        const list = Array.isArray(payload) ? payload : [];
        set({ medicines: list, totalElements: list.length, totalPages: 1 });
      }
    } catch (err) {
      set({ error: 'Failed to load medicines' });
      toast.error('Failed to load medicines');
    } finally {
      set({ loading: false });
    }
  },

  createMedicine: async (formData) => {
    try {
      await pharmacyService.createMedicine(formData);
      toast.success('Medicine added successfully');
      get().fetchMedicines();
      return true;
    } catch {
      toast.error('Failed to add medicine');
      return false;
    }
  },

  updateMedicine: async (id, formData) => {
    try {
      await pharmacyService.updateMedicine(id, formData);
      toast.success('Medicine updated successfully');
      get().fetchMedicines();
      return true;
    } catch {
      toast.error('Failed to update medicine');
      return false;
    }
  },
}));
