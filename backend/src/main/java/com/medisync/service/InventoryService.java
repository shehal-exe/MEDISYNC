package com.medisync.service;

import com.medisync.dao.MedicineDao;
import com.medisync.dto.MedicineRequest;
import com.medisync.dto.MedicineResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final MedicineDao medicineDao;

    public InventoryService(MedicineDao medicineDao) {
        this.medicineDao = medicineDao;
    }

    public List<MedicineResponse> getAllMedicines() {
        return medicineDao.findAll();
    }

    public MedicineResponse getMedicine(Long id) {
        MedicineResponse res = medicineDao.findById(id);
        if (res == null) throw new IllegalArgumentException("Medicine not found");
        return res;
    }

    public MedicineResponse addMedicine(MedicineRequest req) {
        Long id = medicineDao.create(req);
        return medicineDao.findById(id);
    }

    public MedicineResponse updateMedicine(Long id, MedicineRequest req) {
        boolean updated = medicineDao.update(id, req);
        if (!updated) throw new IllegalArgumentException("Medicine not found");
        return medicineDao.findById(id);
    }

    public void deleteMedicine(Long id) {
        boolean deleted = medicineDao.delete(id);
        if (!deleted) throw new IllegalArgumentException("Medicine not found");
    }
}
