package com.fastorder.order.service;

import com.fastorder.order.domain.dto.PedidoRequest;
import com.fastorder.order.domain.entity.ProductoStock;
import com.fastorder.order.repository.PedidoRepository;
import com.fastorder.order.repository.ProductoStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductoStockRepository productoStockRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        productoStockRepository.deleteAll();

        ProductoStock p = new ProductoStock();
        p.setId(999L);
        p.setNombre("Producto Test");
        p.setPrecio(new BigDecimal("10.00"));
        p.setStock(10);
        p.setDisponible(true);

        p = productoStockRepository.save(p);
        testProductId = p.getId();
    }

    @Test
    void testConcurrentOrders_PreventOverselling() throws InterruptedException {
        int numberOfThreads = 15;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);

        PedidoRequest request = new PedidoRequest(List.of(
                new PedidoRequest.ItemRequest(testProductId, 1)
        ));

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    orderService.createPedido(1L, request);
                    successfulOrders.incrementAndGet();
                } catch (Exception e) {
                    failedOrders.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        ProductoStock finalStock = productoStockRepository.findById(testProductId).orElseThrow();
        
        assertEquals(0, finalStock.getStock(), "Stock should be 0");
        assertEquals(10, successfulOrders.get(), "Only 10 orders should succeed");
        assertEquals(5, failedOrders.get(), "5 orders should fail");
        assertTrue(finalStock.getStock() >= 0, "Stock should never be negative");
    }
}
