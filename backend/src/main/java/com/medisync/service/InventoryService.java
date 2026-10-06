package com.medisync.service;

import com.medisync.dao.MedicineDao;
import com.medisync.dao.PharmacistProfileDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.AddStockRequest;
import com.medisync.dto.MedicineRequest;
import com.medisync.dto.MedicineResponse;
import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final MedicineDao medicineDao;
    private final PharmacistProfileDao pharmacistProfileDao;
    private final UserDao userDao;

    public InventoryService(MedicineDao medicineDao, PharmacistProfileDao pharmacistProfileDao, UserDao userDao) {
        this.medicineDao = medicineDao;
        this.pharmacistProfileDao = pharmacistProfileDao;
        this.userDao = userDao;
    }

    public List<MedicineResponse> getAllMedicines(String email) {
        return medicineDao.findAll(getPharmacistId(email));
    }

    public MedicineResponse getMedicine(String email, Long id) {
        MedicineResponse res = medicineDao.findById(getPharmacistId(email), id);
        if (res == null) throw new IllegalArgumentException("Medicine not found");
        return res;
    }

    public MedicineResponse addMedicine(String email, MedicineRequest req) {
        Long pharmacistId = getPharmacistId(email);
        Long id = medicineDao.create(pharmacistId, req);
        return medicineDao.findById(pharmacistId, id);
    }

    public MedicineResponse addStock(String email, AddStockRequest req) {
        Long pharmacistId = getPharmacistId(email);
        Long medicineId = req.getMedicineId();

        if (medicineId == null) {
            if (req.getName() == null || req.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Medicine name is required when adding a new medicine");
            }

            MedicineRequest medicineRequest = new MedicineRequest();
            medicineRequest.setName(req.getName().trim());
            medicineRequest.setDescription(req.getDescription());
            medicineRequest.setManufacturer(req.getManufacturer());
            medicineRequest.setPrice(req.getUnitPrice());
            medicineRequest.setStockQuantity(req.getQuantityInStock());
            medicineRequest.setRequiresPrescription(false);
            medicineId = medicineDao.create(pharmacistId, medicineRequest);
        } else if (!medicineDao.existsById(pharmacistId, medicineId)) {
            throw new IllegalArgumentException("Medicine not found");
        }

        medicineDao.createInventoryBatch(
                medicineId,
                req.getBatchNumber().trim(),
                req.getExpiryDate(),
                req.getQuantityInStock(),
                req.getUnitPrice()
        );

        return medicineDao.findById(pharmacistId, medicineId);
    }

    @Transactional
    public MedicineResponse updateMedicine(String email, Long id, MedicineRequest req) {
        Long pharmacistId = getPharmacistId(email);
        boolean updated = medicineDao.update(pharmacistId, id, req);
        if (!updated) throw new IllegalArgumentException("Medicine not found");
        medicineDao.updateInventorySnapshot(pharmacistId, id, req.getStockQuantity(), req.getPrice());
        return medicineDao.findById(pharmacistId, id);
    }

    public void deleteMedicine(String email, Long id) {
        boolean deleted = medicineDao.delete(getPharmacistId(email), id);
        if (!deleted) throw new IllegalArgumentException("Medicine not found");
    }

    private Long getPharmacistId(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        PharmacistProfileResponse profile = pharmacistProfileDao.getProfileByUserId(user.getUserId());
        if (profile == null) throw new IllegalArgumentException("Pharmacist profile not found");
        return profile.getPharmacistId();
    }
}
