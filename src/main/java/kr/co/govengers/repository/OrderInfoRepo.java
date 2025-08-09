package kr.co.govengers.repository;

import kr.co.govengers.entity.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderInfoRepo extends JpaRepository<OrderInfo, Long> {
    List<OrderInfo> findByUser_UidOrderByOrderDateDesc(String uid);
}