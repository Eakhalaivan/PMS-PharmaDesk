import { create } from 'zustand';
import { toast } from 'react-hot-toast';
import api from '../utils/api';

const POLL_INTERVAL_MS = 30_000;

export const useUserStore = create((set, get) => ({
  users: [],
  loading: false,
  error: null,
  searchTerm: '',
  _pollTimer: null,

  setSearch: (searchTerm) => set({ searchTerm }),

  // Derived: filtered client-side (users list is small, no need for server search)
  filteredUsers: () => {
    const { users, searchTerm } = get();
    if (!searchTerm) return users;
    const s = searchTerm.toLowerCase();
    return users.filter(u =>
      u.name?.toLowerCase().includes(s) ||
      u.username?.toLowerCase().includes(s) ||
      u.email?.toLowerCase().includes(s)
    );
  },

  fetchUsers: async () => {
    set({ loading: true, error: null });
    try {
      const res = await api.get('/auth/users');
      const list = res.data?.data ?? res.data ?? [];
      set({ users: Array.isArray(list) ? list : [] });
    } catch {
      set({ error: 'Failed to load users' });
      toast.error('Failed to fetch users');
    } finally {
      set({ loading: false });
    }
  },

  startPolling: () => {
    get().fetchUsers();
    const timer = setInterval(() => get().fetchUsers(), POLL_INTERVAL_MS);
    set({ _pollTimer: timer });
  },

  stopPolling: () => {
    const { _pollTimer } = get();
    if (_pollTimer) {
      clearInterval(_pollTimer);
      set({ _pollTimer: null });
    }
  },

  createUser: async (formData) => {
    try {
      const res = await api.post('/auth/users', formData);
      if (res.data?.success) {
        toast.success('User created!');
        get().fetchUsers();
        return { success: true, data: res.data };
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create user');
    }
    return { success: false };
  },

  updateUser: async (id, formData) => {
    try {
      const res = await api.put(`/auth/users/${id}`, formData);
      if (res.data?.success) {
        toast.success('User updated!');
        get().fetchUsers();
        return true;
      }
    } catch {
      toast.error('Failed to update user');
    }
    return false;
  },

  toggleUserStatus: async (user) => {
    try {
      await api.put(`/auth/users/${user.id}/status`);
      toast.success(`User ${user.status === 'ACTIVE' ? 'suspended' : 'activated'}`);
      get().fetchUsers();
      return true;
    } catch {
      toast.error('Failed to update status');
    }
    return false;
  },
}));
