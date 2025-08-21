package com.management.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.management.shop.entity.PaymentEntity;

public interface SalesPaymentRepository extends JpaRepository<PaymentEntity, Integer>{

	
	@Query(value="select * from billing_payments bp where bp.billing_id=?1", nativeQuery=true)
	PaymentEntity findPaymentDetails(Integer id);

	

}
