import React from 'react';

export default function SupplierReturns({ onBack }) {
  return (
    <div className="p-6">
      <button onClick={onBack} className="mb-4 text-blue-600 hover:underline">
        &larr; Back to Suppliers
      </button>
      <h2 className="text-2xl font-bold">Returns to Supplier</h2>
      <p className="text-slate-500 mt-2">Supplier returns module coming soon.</p>
    </div>
  );
}
