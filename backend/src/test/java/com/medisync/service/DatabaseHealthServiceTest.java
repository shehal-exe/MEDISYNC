package com.medisync.service;

import com.medisync.dao.DatabaseHealthDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseHealthServiceTest {

    @Mock
    private DatabaseHealthDao databaseHealthDao;

    @InjectMocks
    private DatabaseHealthService databaseHealthService;

    @Test
    public void testIsDatabaseUp_WhenDatabaseIsUp() {
        when(databaseHealthDao.checkConnection()).thenReturn(true);
        assertTrue(databaseHealthService.isDatabaseUp());
    }

    @Test
    public void testIsDatabaseUp_WhenDatabaseIsDown() {
        when(databaseHealthDao.checkConnection()).thenReturn(false);
        assertFalse(databaseHealthService.isDatabaseUp());
    }
}
