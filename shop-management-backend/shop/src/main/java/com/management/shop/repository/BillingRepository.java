package com.management.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.management.shop.entity.BillingEntity;

public interface BillingRepository extends JpaRepository<BillingEntity, Integer>{

	
	@Query(value="select * from billing_details where invoice_number=?1", nativeQuery=true)
	BillingEntity findOrderByReference(String orderReferenceNumber);

	@Query(value="select * from billing_Details order by created_date desc", nativeQuery=true)
	List<BillingEntity> findAllBillingDetails();

}
