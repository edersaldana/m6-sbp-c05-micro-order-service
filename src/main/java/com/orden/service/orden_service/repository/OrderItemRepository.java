package com.orden.service.orden_service.repository;

import com.orden.service.orden_service.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    @Query(value = "SELECT * FROM order_items WHERE order_id = :id", nativeQuery = true)
    List<OrderItemEntity> findByOrderId(@Param("id") Long orderId);
}
