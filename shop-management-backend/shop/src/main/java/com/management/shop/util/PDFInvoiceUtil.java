package com.management.shop.util;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.management.shop.dto.OrderItem;

@Component
public class PDFInvoiceUtil {
	

		@Autowired
		private final TemplateEngine templateEngine;

		public PDFInvoiceUtil(TemplateEngine templateEngine) {
			this.templateEngine = templateEngine;
		}
	
		public byte[] generateInvoice(String customerName, String customerEmail, String customerPhone,  String invoiceId, List<OrderItem> products)
				throws Exception {
			Context context = new Context();
			double grandTotal = products.stream().mapToDouble(p -> p.getUnitPrice() * p.getQuantity()).sum();
			
			//String barcodeBase64 = BarcodeGenerator.generateBarcodeBase64(invoiceId);

			context.setVariable("shopName", "Friends Mobile Store");
			context.setVariable("shopAddress", "Bariatu Ranchi");
			context.setVariable("invoiceId", invoiceId);
			context.setVariable("products", products);
			context.setVariable("grandTotal", grandTotal);
			context.setVariable("customerName", customerName);
			context.setVariable("customerEmail", customerEmail);
			context.setVariable("customerPhone", customerPhone);
			//context.setVariable("barcodeBase64", barcodeBase64);

			// Render Thymeleaf template into HTML string
			String htmlContent = templateEngine.process("invoice", context);

			// Convert HTML to PDF
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(htmlContent);
			renderer.layout();
			renderer.createPDF(baos);

			return baos.toByteArray();
		}

		// Sample product model
		public static class Product {
	        private String name;
	        private int quantity;
	        private double price;
	        public Product(String name, int qty, double price) {
	            this.name = name; this.quantity = qty; this.price = price;
	        }
	        public String getName() { return name; }
	        public int getQuantity() { return quantity; }
	        public double getPrice() { return price; }
	    }
}


