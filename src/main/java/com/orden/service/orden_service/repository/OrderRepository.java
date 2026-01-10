package com.orden.service.orden_service.repository;

import com.orden.service.orden_service.entity.OrderEntity;
import com.orden.service.orden_service.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") Long id);

    Optional<OrderEntity> findTopByOrderByIdDesc();

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items")
    List<OrderEntity> findAllWithItems();

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.userId = :userId")
    List<OrderEntity> findByUserIdWithItems(@Param("userId") Long userId);
}
