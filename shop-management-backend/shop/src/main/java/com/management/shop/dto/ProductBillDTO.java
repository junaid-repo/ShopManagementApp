package com.management.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductBillDTO {

	private Integer id;
	private String name;
	private String category;
	private String status;
	private Integer price;
	private Integer stock;
	private Integer quantity;
}
