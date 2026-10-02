package com.medisync.service;

import com.medisync.dao.*;
import com.medisync.dto.*;
import com.medisync.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SaleService {

    private final SaleDao saleDao;
    private final SaleItemDao saleItemDao;
    private final MedicineDao medicineDao;
    private final PharmacistProfileDao pharmacistProfileDao;
    private final UserDao userDao;

    public SaleService(SaleDao saleDao, SaleItemDao saleItemDao, 
                       MedicineDao medicineDao, PharmacistProfileDao pharmacistProfileDao, 
                       UserDao userDao) {
        this.saleDao = saleDao;
        this.saleItemDao = saleItemDao;
        this.medicineDao = medicineDao;
        this.pharmacistProfileDao = pharmacistProfileDao;
        this.userDao = userDao;
    }

    private Long getPharmacistIdByEmail(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        PharmacistProfileResponse profile = pharmacistProfileDao.getProfileByUserId(user.getUserId());
        if (profile == null) throw new IllegalArgumentException("Pharmacist profile not found");
        return profile.getPharmacistId();
    }

    @Transactional
    public SaleResponse createSale(String email, CreateSaleRequest request) {
        Long pharmacistId = getPharmacistIdByEmail(email);
        
        BigDecimal grandTotal = BigDecimal.ZERO;
        
        // Validation and Subtotal calculation
        for (SaleItemRequest itemReq : request.getItems()) {
            MedicineResponse med = medicineDao.findById(pharmacistId, itemReq.getMedicineId());
            if (med == null) {
                throw new IllegalArgumentException("Medicine ID " + itemReq.getMedicineId() + " not found");
            }
            if (med.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for medicine: " + med.getName());
            }
            BigDecimal itemTotal = med.getPrice().multiply(new BigDecimal(itemReq.getQuantity()));
            grandTotal = grandTotal.add(itemTotal);
        }

        // Create Sale
        Long saleId = saleDao.create(request.getPatientId(), pharmacistId, grandTotal);

        // Process items
        for (SaleItemRequest itemReq : request.getItems()) {
            MedicineResponse med = medicineDao.findById(pharmacistId, itemReq.getMedicineId());
            BigDecimal itemTotal = med.getPrice().multiply(new BigDecimal(itemReq.getQuantity()));

            // Record SaleItem
            saleItemDao.create(pharmacistId, saleId, med.getMedicineId(), itemReq.getQuantity(), med.getPrice(), itemTotal);
        }

        return getSaleById(email, saleId);
    }

    public List<SaleResponse> getAllSales(String email) {
        return saleDao.findAllByPharmacistId(getPharmacistIdByEmail(email));
    }

    public SaleResponse getSaleById(String email, Long saleId) {
        SaleResponse sale = saleDao.findByIdAndPharmacistId(saleId, getPharmacistIdByEmail(email));
        if (sale == null) {
            throw new IllegalArgumentException("Sale not found");
        }
        sale.setItems(saleItemDao.findBySaleId(saleId));
        return sale;
    }
}
