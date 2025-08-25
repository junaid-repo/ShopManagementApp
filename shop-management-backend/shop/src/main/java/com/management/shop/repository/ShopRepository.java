package com.management.shop.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.management.shop.entity.CustomerEntity;

import jakarta.transaction.Transactional;

@Repository
public interface ShopRepository extends JpaRepository<CustomerEntity, Integer> {

	@Modifying
	@Transactional
	@Query(value = "UPDATE shop_customer  SET total_spent = total_spent + ?2 WHERE id = ?1", nativeQuery = true)
	void updateCustomerSpentAmount(Integer id, Integer spent_value);

	@Query(value = "SELECT   * FROM    shop_customer WHERE    created_date BETWEEN ?1 AND ?2", nativeQuery = true)
	List<CustomerEntity> findCustomerByDateRange(LocalDateTime fromDate, LocalDateTime toDate);

	@Modifying
	@Transactional
	@Query(value = "UPDATE shop_customer  SET status = ?2 WHERE id = ?1", nativeQuery = true)
	void updateStatus(Integer id, String status);

	@Query(value = "SELECT   * FROM    shop_customer WHERE    status=?1", nativeQuery = true)
	List<CustomerEntity> findAllActiveCustomer(String status);
	
    @Query(value = "SELECT TO_CHAR(sc.created_date, 'Mon') AS month, " +
            "COUNT(sc.id) AS customerCount " +
            "FROM shop_customer sc " +
            "WHERE sc.created_date BETWEEN :fromDate AND :toDate " +
            "GROUP BY TO_CHAR(sc.created_date, 'Mon'), DATE_PART('month', sc.created_date) " +
            "ORDER BY DATE_PART('month', sc.created_date)",
    nativeQuery = true)
List<Object[]> getMonthlyCustomerCount(
     @Param("fromDate") LocalDateTime fromDate,
     @Param("toDate") LocalDateTime toDate
);

}
