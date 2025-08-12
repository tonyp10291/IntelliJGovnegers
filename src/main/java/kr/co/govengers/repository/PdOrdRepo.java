package kr.co.govengers.repository;

import kr.co.govengers.entity.OrderInfo;
import kr.co.govengers.entity.OrderItem;
import kr.co.govengers.entity.enums.AdminStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PdOrdRepo extends JpaRepository<OrderInfo, Long> {

    // 복합 조건으로 주문 검색 (페이징)
    @Query("SELECT DISTINCT o FROM OrderInfo o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE (:keyword IS NULL OR " +
            "       CAST(o.orderId AS string) LIKE %:keyword% OR " +
            "       u.unm LIKE %:keyword% OR " + // uname → unm으로 변경
            "       o.receiverName LIKE %:keyword%) " +
            "AND (:orderStatus IS NULL OR o.adminStatus = :orderStatus) " +
            "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
            "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<OrderInfo> findOrdersWithFilters(
            Pageable pageable,
            @Param("keyword") String keyword,
            @Param("orderStatus") AdminStatus orderStatus,
            @Param("paymentMethod") String paymentMethod,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 주문 ID로 주문 정보 조회 (주문 아이템 포함)
    @Query("SELECT o FROM OrderInfo o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.orderId = :orderId")
    Optional<OrderInfo> findByIdWithItems(@Param("orderId") Long orderId);

    // 주문 ID로 주문 정보 조회 (사용자 정보와 주문 아이템 포함)
    @Query("SELECT o FROM OrderInfo o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.orderId = :orderId")
    Optional<OrderInfo> findByIdWithUserAndItems(@Param("orderId") Long orderId);

    // 주문 상태별 개수 조회
    Long countByAdminStatus(AdminStatus adminStatus);

    // 날짜 범위 주문 개수 조회
    Long countByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 날짜 범위 주문 금액 합계 조회
    @Query("SELECT SUM(o.finalPayment) FROM OrderInfo o " +
            "WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate " +
            "AND o.adminStatus != '주문취소완료'")
    Integer sumFinalPaymentByOrderDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 주문 상태별 조회
    List<OrderInfo> findByAdminStatusOrderByOrderDateDesc(AdminStatus adminStatus);

    // 월별 주문 통계
    @Query("SELECT EXTRACT(YEAR FROM o.orderDate) as year, " +
            "EXTRACT(MONTH FROM o.orderDate) as month, " +
            "COUNT(o) as orderCount, " +
            "SUM(o.finalPayment) as totalAmount " +
            "FROM OrderInfo o " +
            "WHERE o.adminStatus != '주문취소완료' " +
            "GROUP BY EXTRACT(YEAR FROM o.orderDate), EXTRACT(MONTH FROM o.orderDate) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> findMonthlyOrderStatistics();

    // 결제 수단별 통계
    @Query("SELECT o.paymentMethod, COUNT(o) as orderCount, SUM(o.finalPayment) as totalAmount " +
            "FROM OrderInfo o " +
            "WHERE o.adminStatus != '주문취소완료' " +
            "GROUP BY o.paymentMethod " +
            "ORDER BY orderCount DESC")
    List<Object[]> findPaymentMethodStatistics();

    // 일별 주문 통계 (최근 30일)
    @Query("SELECT DATE(o.orderDate) as orderDate, " +
            "COUNT(o) as orderCount, " +
            "SUM(o.finalPayment) as totalAmount " +
            "FROM OrderInfo o " +
            "WHERE o.orderDate >= :startDate " +
            "AND o.adminStatus != '주문취소완료' " +
            "GROUP BY DATE(o.orderDate) " +
            "ORDER BY orderDate DESC")
    List<Object[]> findDailyOrderStatistics(@Param("startDate") LocalDateTime startDate);

    // 사용자별 주문 통계
    @Query("SELECT u.unm, COUNT(o) as orderCount, SUM(o.finalPayment) as totalAmount " +
            "FROM OrderInfo o " +
            "LEFT JOIN o.user u " +
            "WHERE o.adminStatus != '주문취소완료' " +
            "GROUP BY u.unm " +
            "ORDER BY orderCount DESC")
    List<Object[]> findUserOrderStatistics();

    // 상품별 주문 통계
    @Query("SELECT oi.pnm, SUM(oi.quantity) as totalQuantity, " +
            "SUM(oi.price * oi.quantity) as totalAmount " +
            "FROM OrderItem oi " +
            "WHERE oi.status != '환불완료' " +
            "GROUP BY oi.pnm " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findProductOrderStatistics();

    // 높은 금액 주문 조회 (상위 N개)
    @Query("SELECT o FROM OrderInfo o " +
            "WHERE o.adminStatus != '주문취소완료' " +
            "ORDER BY o.finalPayment DESC")
    List<OrderInfo> findTopOrdersByAmount(Pageable pageable);

    // 특정 금액 이상 주문 조회
    @Query("SELECT DISTINCT o FROM OrderInfo o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.finalPayment >= :minAmount " +
            "AND o.adminStatus != '주문취소완료' " +
            "ORDER BY o.orderDate DESC")
    List<OrderInfo> findOrdersByMinAmount(@Param("minAmount") Integer minAmount);

    // 주문 ID로 주문 아이템들 조회
    @Query("SELECT oi FROM OrderItem oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE oi.orderInfo.orderId = :orderId")
    List<OrderItem> findOrderItemsByOrderId(@Param("orderId") Long orderId);

    // 주문 아이템 ID로 주문 아이템 조회
    @Query("SELECT oi FROM OrderItem oi " +
            "LEFT JOIN FETCH oi.orderInfo " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE oi.id = :itemId")
    Optional<OrderItem> findOrderItemById(@Param("itemId") Long itemId);

    // 특정 기간 매출 조회
    @Query("SELECT SUM(o.finalPayment) FROM OrderInfo o " +
            "WHERE o.orderDate >= :startDate " +
            "AND o.orderDate <= :endDate " +
            "AND o.adminStatus != '주문취소완료'")
    Long sumSalesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 주문 상태별 금액 통계
    @Query("SELECT o.adminStatus, COUNT(o), SUM(o.finalPayment) " +
            "FROM OrderInfo o " +
            "GROUP BY o.adminStatus")
    List<Object[]> findOrderStatisticsByStatus();

    // 시간대별 주문 통계
    @Query("SELECT EXTRACT(HOUR FROM o.orderDate) as hour, COUNT(o) as orderCount " +
            "FROM OrderInfo o " +
            "WHERE o.orderDate >= :startDate " +
            "GROUP BY EXTRACT(HOUR FROM o.orderDate) " +
            "ORDER BY hour")
    List<Object[]> findHourlyOrderStatistics(@Param("startDate") LocalDateTime startDate);

    // 배송 지역별 통계
    @Query("SELECT SUBSTRING(o.receiverAddress, 1, LOCATE(' ', o.receiverAddress)) as region, " +
            "COUNT(o) as orderCount, SUM(o.finalPayment) as totalAmount " +
            "FROM OrderInfo o " +
            "WHERE o.adminStatus != '주문취소완료' " +
            "AND o.receiverAddress IS NOT NULL " +
            "GROUP BY SUBSTRING(o.receiverAddress, 1, LOCATE(' ', o.receiverAddress)) " +
            "ORDER BY orderCount DESC")
    List<Object[]> findRegionOrderStatistics();

    // 반복 주문 고객 조회
    @Query("SELECT u.unm, COUNT(o) as orderCount, SUM(o.finalPayment) as totalAmount " +
            "FROM OrderInfo o " +
            "LEFT JOIN o.user u " +
            "WHERE o.adminStatus != '주문취소완료' " +
            "GROUP BY u.unm " +
            "HAVING COUNT(o) >= :minOrderCount " +
            "ORDER BY orderCount DESC")
    List<Object[]> findRepeatCustomers(@Param("minOrderCount") Integer minOrderCount);

    // 취소율 높은 상품 조회
    @Query("SELECT oi.pnm, " +
            "COUNT(oi) as totalCount, " +
            "SUM(CASE WHEN oi.status = '환불완료' THEN 1 ELSE 0 END) as cancelCount, " +
            "ROUND(SUM(CASE WHEN oi.status = '환불완료' THEN 1 ELSE 0 END) * 100.0 / COUNT(oi), 2) as cancelRate " +
            "FROM OrderItem oi " +
            "GROUP BY oi.pnm " +
            "HAVING COUNT(oi) >= 5 " +
            "ORDER BY cancelRate DESC")
    List<Object[]> findHighCancelRateProducts();

    // 특정 사용자의 주문 이력
    @Query("SELECT o FROM OrderInfo o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.user.uid = :uid " +
            "ORDER BY o.orderDate DESC")
    List<OrderInfo> findOrdersByUserId(@Param("uid") String uid);

    // 주문 금액 범위별 통계
    @Query("SELECT " +
            "CASE " +
            "  WHEN o.finalPayment < 50000 THEN '5만원 미만' " +
            "  WHEN o.finalPayment < 100000 THEN '5-10만원' " +
            "  WHEN o.finalPayment < 200000 THEN '10-20만원' " +
            "  WHEN o.finalPayment < 500000 THEN '20-50만원' " +
            "  ELSE '50만원 이상' " +
            "END as priceRange, " +
            "COUNT(o) as orderCount, " +
            "SUM(o.finalPayment) as totalAmount " +
            "FROM OrderInfo o " +
            "WHERE o.adminStatus != '주문취소완료' " +
            "GROUP BY " +
            "CASE " +
            "  WHEN o.finalPayment < 50000 THEN '5만원 미만' " +
            "  WHEN o.finalPayment < 100000 THEN '5-10만원' " +
            "  WHEN o.finalPayment < 200000 THEN '10-20만원' " +
            "  WHEN o.finalPayment < 500000 THEN '20-50만원' " +
            "  ELSE '50만원 이상' " +
            "END " +
            "ORDER BY MIN(o.finalPayment)")
    List<Object[]> findOrderStatisticsByPriceRange();
}