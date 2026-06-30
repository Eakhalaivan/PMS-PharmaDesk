# State Management Architecture

This document outlines the standard patterns for state management in the PMS-PharmaDesk frontend application.

## Core Philosophy

We separate state into three distinct categories, each managed by the appropriate tool:
1. **API / Server State (Lists, Queries)** -> `React Query`
2. **UI / Form / Client State** -> `Zustand`
3. **Async Controls (Timers, AbortControllers)** -> `Module-level Variables`

---

## 1. API / Server State (React Query)
Any state that is a direct reflection of server data (e.g., lists of users, lookup data, system configuration) should be managed using `@tanstack/react-query`.

- **Why:** React Query provides built-in caching, background refetching, deduping, and stale-time configuration.
- **When to use:** Reports, dashboards, data tables (where we just read from the server and display it).
- **Implementation:** 
  - Contexts (`SystemContext`, `LookupContext`, `ConfigContext`) have been refactored into custom hooks (`useSystem`, `useLookup`, `useConfig`) that wrap `useQuery`.

---

## 2. UI / Form / Client State (Zustand)
Complex client-side interactions, form wizards, Point-of-Sale (POS) grids, and global UI preferences should use Zustand.

- **Why:** Zustand provides unopinionated, fast, and simple state management without Provider wrappers.
- **When to use:** Multi-step forms (e.g., `usePOSStore`), domain-specific workflows (e.g., `usePharmacySalesStore`, `useDirectSalesStore`, `useWorklistStore`, `usePrescriptionStore`).
- **Best Practices (Required):**
  1. **Domain-Driven Stores:** Never create a "god store" (like the old `useSalesStore`). Split stores by specific business domains (e.g., `usePOSStore`, `useWorklistStore`).
  2. **Shallow Selectors:** Components MUST extract what they need using `useShallow` to prevent unnecessary re-renders.
     ```javascript
     // BAD (causes re-renders on ANY state change)
     const { patientName, rows } = usePOSStore();
     
     // GOOD (only re-renders if patientName or rows change)
     const { patientName, rows } = usePOSStore(useShallow(state => ({
       patientName: state.patientName,
       rows: state.rows
     })));
     ```
  3. **Immer & Persist:** Use the `immer` middleware for easy immutable updates (e.g., modifying deep arrays like POS rows) and `persist` for saving form drafts in `localStorage`.

---

## 3. Async Controls (Module-level Variables)
Never store `AbortController` instances or `setTimeout` references inside a Zustand store or React state. They are not reactive UI elements.

- **Why:** Storing non-serializable objects in Zustand breaks DevTools/Persist and causes memory leaks. Putting them in React State causes useless re-renders.
- **When to use:** Debouncing search inputs, cancelling ongoing fetch requests when a new one starts.
- **Implementation:** Keep them at the module level (outside the store hook/factory).
  ```javascript
  let abortController = null;
  let searchTimer = null;
  
  export const useMyStore = create((set, get) => ({
    searchTerm: '',
    results: [],
    
    setSearchTerm: (term) => {
      set({ searchTerm: term });
      
      // Cleanup previous request/timer
      if (searchTimer) clearTimeout(searchTimer);
      if (abortController) abortController.abort();
      
      searchTimer = setTimeout(async () => {
        abortController = new AbortController();
        try {
          const res = await api.get(`/search?q=${term}`, { signal: abortController.signal });
          set({ results: res.data });
        } catch (err) {
          if (err.name !== 'CanceledError') {
            console.error(err);
          }
        }
      }, 300);
    }
  }));
  ```

---

## Summary of Refactor
- Fixed `AuthContext` to properly register the `auth:expired` event listener.
- Split the monolithic `useSalesStore` into 4 separate stores: `usePharmacySalesStore`, `useDirectSalesStore`, `useWorklistStore`, and `usePrescriptionStore`.
- Refactored `usePOSStore` to use `persist` and `immer`, and properly managed network aborts using module-level controllers.
- Applied `useShallow` across the entire codebase for all Zustand store consumers.
- Migrated Context providers (`SystemContext`, `LookupContext`, `ConfigContext`) to use `React Query`.
- Added an `ErrorBoundary` at the root (`App.jsx`) to gracefully catch rendering errors.
