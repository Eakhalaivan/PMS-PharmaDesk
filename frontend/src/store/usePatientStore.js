import { create } from 'zustand';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';

const DEBOUNCE_MS = 400;

export const usePatientStore = create((set, get) => ({
  patients: [],
  loading: false,
  error: null,
  searchTerm: '',
  _searchTimer: null,

  setSearch: (searchTerm) => {
    set({ searchTerm });
    const prev = get()._searchTimer;
    if (prev) clearTimeout(prev);
    const timer = setTimeout(() => get().fetchPatients(), DEBOUNCE_MS);
    set({ _searchTimer: timer });
  },

  fetchPatients: async () => {
    set({ loading: true, error: null });
    try {
      const res = await pharmacyService.getPatients();
      const list = res?.data ?? res ?? [];
      set({ patients: Array.isArray(list) ? list : [] });
    } catch {
      set({ error: 'Failed to load patients' });
      toast.error('Failed to fetch patients');
    } finally {
      set({ loading: false });
    }
  },

  createPatient: async (formData) => {
    try {
      const res = await pharmacyService.createPatient(formData);
      if (res?.success) {
        toast.success('Patient added');
        get().fetchPatients();
        return true;
      }
    } catch {
      toast.error('Failed to add patient');
    }
    return false;
  },

  updatePatient: async (id, formData) => {
    try {
      const res = await pharmacyService.updatePatient(id, formData);
      if (res?.success) {
        toast.success('Patient updated');
        get().fetchPatients();
        return true;
      }
    } catch {
      toast.error('Failed to update patient');
    }
    return false;
  },

  deletePatient: async (id) => {
    try {
      const res = await pharmacyService.deletePatient(id);
      if (res?.success) {
        toast.success('Patient deleted');
        get().fetchPatients();
        return true;
      }
    } catch {
      toast.error('Failed to delete patient');
    }
    return false;
  },

  filteredPatients: () => {
    const { patients, searchTerm } = get();
    if (!searchTerm) return patients;
    const s = searchTerm.toLowerCase();
    return patients.filter(p => 
      p.name?.toLowerCase().includes(s) || 
      p.uhid?.toLowerCase().includes(s)
    );
  },
}));
