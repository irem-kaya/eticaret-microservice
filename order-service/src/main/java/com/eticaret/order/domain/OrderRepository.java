package com.eticaret.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(String userId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    @Query("SELECT oi.productId, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi " +
            "GROUP BY oi.productId " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProductIds(Pageable pageable);
}