import { create } from 'zustand';
import { devtools, persist, createJSONStorage } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';

const nameChangeTimers = new Map();
const abortControllers = new Map();
let patientSearchTimer = null;

const createEmptyRow = () => ({
  id: Date.now() + Math.random(),
  stockId: null,
  codeName: '',
  genericName: '',
  uom: '',
  rack: '',
  totalQty: 0,
  batchQty: 0,
  qty: '',
  batchNo: '',
  expiryDate: '',
  rate: 0.0,
  gst: 0,
  discount: '',
  amount: 0.0,
  searchResults: [],
});

const getInitialState = () => ({
  // Header State
  visitType: 'OP',
  uhidSearch: '',
  visitSearch: '',

  // Patient Info State
  patientName: 'Walk-in',
  ageSex: '',
  uhid: '',
  doctor: '',
  insurance: '',
  patientType: '',
  pharmacy: 'OP Pharmacy',
  discountType: '%',
  discountCategory: '',
  location: '',
  companyName: '',
  gstNo: '',

  // Patient Autocomplete State
  patientSearchResults: [],
  isSearchingPatient: false,

  // Item rows State
  rows: [createEmptyRow()],
  isGenericSearch: false,
  barcodeSearch: '',

  // Payment State
  paymentType: 'Cash',
  isMultiplePayment: false,
  discount: 0,
  receiptAmount: 0,
  remarks: '',
  saving: false,
});

export const usePOSStore = create(
  devtools(
    persist(
      immer((set, get) => ({
        ...getInitialState(),

        setField: (field, value) => set(state => { state[field] = value; }),

        resetForm: () => {
          nameChangeTimers.forEach(t => clearTimeout(t));
          nameChangeTimers.clear();
          if (patientSearchTimer) clearTimeout(patientSearchTimer);
          abortControllers.forEach(ctrl => ctrl.abort());
          abortControllers.clear();
          
          set(state => {
            Object.assign(state, getInitialState());
          });
        },

        addRow: () => set(state => {
          state.rows.push(createEmptyRow());
        }),

        removeRow: (idx) => set(state => {
          if (state.rows.length === 1) return;
          if (nameChangeTimers.has(idx)) {
            clearTimeout(nameChangeTimers.get(idx));
            nameChangeTimers.delete(idx);
          }
          if (abortControllers.has(`row_${idx}`)) {
            abortControllers.get(`row_${idx}`).abort();
            abortControllers.delete(`row_${idx}`);
          }
          state.rows.splice(idx, 1);
        }),

        resetRow: (idx) => set(state => {
          state.rows[idx] = createEmptyRow();
        }),

        searchPatients: (query) => {
          if (query.trim().length < 2) {
            set(state => { state.patientSearchResults = []; });
            return;
          }
          
          if (patientSearchTimer) clearTimeout(patientSearchTimer);
          
          patientSearchTimer = setTimeout(async () => {
            if (abortControllers.has('patient')) {
              abortControllers.get('patient').abort();
            }
            const ctrl = new AbortController();
            abortControllers.set('patient', ctrl);
            
            set(state => { state.isSearchingPatient = true; });
            try {
              const res = await pharmacyService.searchPatients(query, { signal: ctrl.signal });
              const data = res?.data || res || [];
              set(state => {
                state.patientSearchResults = Array.isArray(data) ? data : [];
                state.isSearchingPatient = false;
              });
            } catch (err) {
              if (err.name === 'CanceledError' || err.code === 'ERR_CANCELED') return;
              console.error('Error searching patients:', err);
              toast.error('Failed to search patient records');
              set(state => {
                state.patientSearchResults = [];
                state.isSearchingPatient = false;
              });
            }
          }, 300);
        },

        selectPatient: (patient) => set(state => {
          state.patientName = patient.name || 'Walk-in';
          state.uhid = patient.uhid || '';
          state.uhidSearch = patient.name || '';
          state.patientSearchResults = [];
        }),

        handleNameChange: (idx, val) => {
          set(state => {
            if (state.rows[idx]) {
              state.rows[idx].codeName = val;
              state.rows[idx].searchResults = [];
            }
          });

          if (val.trim().length < 2) return;

          if (nameChangeTimers.has(idx)) {
            clearTimeout(nameChangeTimers.get(idx));
          }

          const timer = setTimeout(async () => {
            if (abortControllers.has(`row_${idx}`)) {
              abortControllers.get(`row_${idx}`).abort();
            }
            const ctrl = new AbortController();
            abortControllers.set(`row_${idx}`, ctrl);

            try {
              const res = await pharmacyService.searchStocks(val, { signal: ctrl.signal });
              const data = res?.data || res || [];
              set(state => {
                if (state.rows[idx]) {
                  state.rows[idx].searchResults = Array.isArray(data) ? data : [];
                }
              });
            } catch (err) {
              if (err.name === 'CanceledError' || err.code === 'ERR_CANCELED') return;
              console.error('Error searching stock:', err);
              set(state => {
                if (state.rows[idx]) {
                  state.rows[idx].searchResults = [];
                }
              });
            }
          }, 300);

          nameChangeTimers.set(idx, timer);
        },

        selectStock: (idx, stock) => set(state => {
          if (state.rows[idx]) {
             state.rows[idx].stockId = stock.id;
             state.rows[idx].codeName = stock.medicine?.name || '';
             state.rows[idx].genericName = stock.medicine?.genericName || '';
             state.rows[idx].uom = stock.medicine?.unit || '';
             state.rows[idx].rack = '';
             state.rows[idx].totalQty = stock.quantityAvailable || 0;
             state.rows[idx].batchQty = stock.quantityAvailable || 0;
             state.rows[idx].batchNo = stock.batchNumber || '';
             state.rows[idx].expiryDate = stock.expiryDate || '';
             state.rows[idx].rate = stock.sellingRate || 0;
             state.rows[idx].gst = stock.medicine?.taxPercentage || 0;
             state.rows[idx].qty = 1;
             state.rows[idx].amount = stock.sellingRate || 0;
             state.rows[idx].searchResults = [];
          }
        }),

        updateQty: (idx, val) => set(state => {
          const qty = parseInt(val) || 0;
          const row = state.rows[idx];
          if (!row) return;

          let targetVal = val;
          let targetQty = qty;

          if (row.stockId && qty > row.totalQty) {
            toast.error(`Only ${row.totalQty} items available in stock`);
            targetVal = String(row.totalQty);
            targetQty = row.totalQty;
          }

          row.qty = targetVal;
          row.amount = row.rate * targetQty;
        }),

        updateRowDiscount: (idx, val) => set(state => {
          const row = state.rows[idx];
          if (!row) return;
          const disc = parseFloat(val) || 0;
          const baseAmt = row.rate * (parseInt(row.qty) || 0);
          const discAmt = (baseAmt * disc) / 100;
          row.discount = val;
          row.amount = baseAmt - discAmt;
        }),

      })),
      {
        name: 'pos-form',
        storage: createJSONStorage(() => sessionStorage),
        partialize: (state) => {
          const rowsNoSearch = state.rows.map(r => ({ ...r, searchResults: [] }));
          return { ...state, rows: rowsNoSearch, patientSearchResults: [] };
        }
      }
    )
  )
);
