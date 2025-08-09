package kr.co.govengers.repository;

import kr.co.govengers.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderInfo_OrderId(Long orderId);
}
