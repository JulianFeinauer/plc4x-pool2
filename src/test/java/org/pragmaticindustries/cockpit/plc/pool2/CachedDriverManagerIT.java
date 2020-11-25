package org.pragmaticindustries.cockpit.plc.pool2;

import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.mock.PlcMockConnection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Multi Threading Test
 *
 * @author julian
 * Created by julian on 06.04.20
 */
class CachedDriverManagerIT {

    @Test
    void connectWithMultpleThreads() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        Supplier<PlcConnection> mock = (Supplier<PlcConnection>) Mockito.mock(Supplier.class);
        PlcMockConnection plcMockConnection = mock(PlcMockConnection.class);
        when(mock.get()).thenReturn(plcMockConnection);

        CachedDriverManager driverManager = new CachedDriverManager("", mock, 2_000);

        AtomicInteger errorCounter = new AtomicInteger(0);
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 1; i <= 100; i++) {
            executorService.submit(() -> {
                try {
                    driverManager.getConnection("").close();
                    successCounter.incrementAndGet();
                } catch (Exception e) {
                    errorCounter.incrementAndGet();
                }
            });
        }

        executorService.shutdown();

        executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(100, successCounter.get());
        assertEquals(0, errorCounter.get());
    }
}