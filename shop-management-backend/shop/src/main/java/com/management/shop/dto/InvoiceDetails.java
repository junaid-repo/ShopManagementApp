package com.management.shop.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceDetails {
	  private String invoiceId;
	    private String paymentReferenceNumber;
	    private List<OrderItem> items;
	    private double totalAmount; // subtotal before GST/discount
	    private boolean paid;
	    private String customerName;
	    private double gstRate;      // e.g., 0.18 for 18%
	    private double discountRate;
	    private String customerEmail;
	    private String customerPhone;
}
