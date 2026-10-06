package com.medisync.service;

import com.medisync.dao.DatabaseHealthDao;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHealthService {

    private final DatabaseHealthDao databaseHealthDao;

    public DatabaseHealthService(DatabaseHealthDao databaseHealthDao) {
        this.databaseHealthDao = databaseHealthDao;
    }

    public boolean isDatabaseUp() {
        return databaseHealthDao.checkConnection();
    }
}
