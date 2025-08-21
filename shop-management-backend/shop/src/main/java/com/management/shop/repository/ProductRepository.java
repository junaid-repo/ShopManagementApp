package com.management.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.management.shop.entity.ProductEntity;

import jakarta.transaction.Transactional;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer>{

	
	@Modifying
	@Transactional 
	@Query(value="UPDATE shop_product  SET stock = stock - ?2 WHERE id = ?1", nativeQuery=true)
	void updateProductStock(Integer id, Integer quantity);
	
	@Modifying
	@Transactional 
	@Query(value="UPDATE shop_product  SET stock = stock + ?2 WHERE id = ?1", nativeQuery=true)
	void addProductStock(Integer id, Integer quantity);
	
	

}
