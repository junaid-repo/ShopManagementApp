package com.management.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.management.shop.entity.CustomerEntity;

import jakarta.transaction.Transactional;

@Repository
public interface ShopRepository extends JpaRepository<CustomerEntity, Integer>{

	@Modifying
	@Transactional 
	@Query(value="UPDATE shop_customer  SET total_spent = total_spent + ?2 WHERE id = ?1", nativeQuery=true)
	void updateCustomerSpentAmount(Integer id, Integer spent_value );
	

}
