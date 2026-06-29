package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.*;
import com.pharmadesk.backend.sales.model.*;
import com.pharmadesk.backend.pharmacy.repository.*;
import com.pharmadesk.backend.sales.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierPerformanceRepository performanceRepository;

    public SupplierService(SupplierRepository supplierRepository,
                           SupplierPerformanceRepository performanceRepository) {
        this.supplierRepository = supplierRepository;
        this.performanceRepository = performanceRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAllByDeletedFalse();
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        supplier.setDeleted(false);
        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().isBlank()) {
            String prefix = supplier.getSupplierType() != null
                    ? supplier.getSupplierType().substring(0, Math.min(3, supplier.getSupplierType().length())).toUpperCase()
                    : "SUP";
            supplier.setSupplierCode(prefix + "-" + System.currentTimeMillis() % 100000);
        }
        if (supplier.getStatus() == null || supplier.getStatus().isBlank()) {
            supplier.setStatus("ACTIVE");
        }
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));

        supplier.setName(supplierDetails.getName());
        supplier.setContact(supplierDetails.getContact());
        supplier.setGstin(supplierDetails.getGstin());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setSupplierType(supplierDetails.getSupplierType());
        supplier.setContactPersonName(supplierDetails.getContactPersonName());
        supplier.setDesignation(supplierDetails.getDesignation());
        supplier.setMobileNumber(supplierDetails.getMobileNumber());
        supplier.setAlternatePhone(supplierDetails.getAlternatePhone());
        supplier.setEmailAddress(supplierDetails.getEmailAddress());
        supplier.setPincode(supplierDetails.getPincode());
        supplier.setCity(supplierDetails.getCity());
        supplier.setState(supplierDetails.getState());
        supplier.setCountry(supplierDetails.getCountry());
        supplier.setDrugLicenseNumber(supplierDetails.getDrugLicenseNumber());
        supplier.setDrugLicenseExpiry(supplierDetails.getDrugLicenseExpiry());
        supplier.setPanNumber(supplierDetails.getPanNumber());
        supplier.setAccountNumber(supplierDetails.getAccountNumber());
        supplier.setBankName(supplierDetails.getBankName());
        supplier.setBranch(supplierDetails.getBranch());
        supplier.setIfscCode(supplierDetails.getIfscCode());
        supplier.setPaymentTerms(supplierDetails.getPaymentTerms());
        supplier.setCreditLimit(supplierDetails.getCreditLimit());
        supplier.setPreferredDeliveryDays(supplierDetails.getPreferredDeliveryDays());
        supplier.setStatus(supplierDetails.getStatus());
        supplier.setNotes(supplierDetails.getNotes());

        return supplierRepository.save(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
    }

    public List<SupplierPerformance> getPerformanceHistory(Long supplierId) {
        return performanceRepository.findBySupplierIdOrderByPeriodStartDesc(supplierId);
    }

    @Transactional
    public SupplierPerformance savePerformanceScore(Long supplierId, SupplierPerformance performance) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
        performance.setSupplier(supplier);

        // Calculate weighted overall score
        double score = 0;
        if (performance.getOnTimeDeliveryRate() != null)
            score += performance.getOnTimeDeliveryRate() * 0.25;
        if (performance.getOrderFillRate() != null)
            score += performance.getOrderFillRate() * 0.20;
        if (performance.getQualityRejectionRate() != null)
            score += (100 - performance.getQualityRejectionRate()) * 0.20;
        if (performance.getPriceVarianceRate() != null)
            score += (100 - performance.getPriceVarianceRate()) * 0.15;
        if (performance.getInvoiceAccuracyRate() != null)
            score += performance.getInvoiceAccuracyRate() * 0.10;
        if (performance.getReturnRate() != null)
            score += (100 - performance.getReturnRate()) * 0.05;
        if (performance.getCreditNoteResponseDays() != null) {
            double responseScore = Math.max(0, 100 - performance.getCreditNoteResponseDays() * 5);
            score += responseScore * 0.05;
        }

        performance.setOverallScore(Math.min(100, Math.max(0, score)));
        return performanceRepository.save(performance);
    }
}
