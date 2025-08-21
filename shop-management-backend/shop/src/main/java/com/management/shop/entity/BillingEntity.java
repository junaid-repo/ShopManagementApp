package com.management.shop.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="BillingDetails")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BillingEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private String invoiceNumber;
	private Integer taxAmount;
	private Integer subTotalAmount;
	private Integer totalAmount;
	private Integer customerId;
	private Integer unitsSold;
	private LocalDateTime createdDate;
	
	 @PostPersist
	    public void generateOrderNumberOnPersist() {
	        // Ensure the ID is available and orderNumber hasn't been set yet
	        if (this.id != null && this.invoiceNumber == null) {
	            // Format the current date into YYYYMMDD (e.g., 20230818)
	            String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

	      
	            String sequentialPart = String.format("%04d", this.id);


	            this.invoiceNumber = "INV-" + datePart + "-" + sequentialPart;

	        }
	    }

}
