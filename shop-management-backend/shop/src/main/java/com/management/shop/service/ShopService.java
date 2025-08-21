package com.management.shop.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

//OpenPDF (com.lowagie.*)
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.management.shop.dto.BillingRequest;
import com.management.shop.dto.BillingResponse;
import com.management.shop.dto.CustomerRequest;
import com.management.shop.dto.CustomerSuccessDTO;
import com.management.shop.dto.DasbboardResponseDTO;
import com.management.shop.dto.InvoiceDetails;
import com.management.shop.dto.OrderItem;
import com.management.shop.dto.PaymentDetails;
import com.management.shop.dto.ProductRequest;
import com.management.shop.dto.ProductSuccessDTO;
import com.management.shop.dto.SalesResponseDTO;
import com.management.shop.entity.BillingEntity;
import com.management.shop.entity.CustomerEntity;
import com.management.shop.entity.PaymentEntity;
import com.management.shop.entity.ProductEntity;
import com.management.shop.entity.ProductSalesEntity;
import com.management.shop.repository.BillingRepository;
import com.management.shop.repository.ProductRepository;
import com.management.shop.repository.ProductSalesRepository;
import com.management.shop.repository.SalesPaymentRepository;
import com.management.shop.repository.ShopRepository;
import com.management.shop.util.CSVUpload;
import com.management.shop.util.EmailSender;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShopService {

	@Autowired
	private ShopRepository shopRepo;

	@Autowired
	private ProductRepository prodRepo;

	@Autowired
	private BillingRepository billRepo;

	@Autowired
	private ProductSalesRepository prodSalesRepo;

	@Autowired
	private SalesPaymentRepository salesPaymentRepo;

	@Autowired
	CSVUpload util;
	
	@Autowired
	EmailSender email;

	public CustomerSuccessDTO saveCustomer(CustomerRequest request) {

		var customerEntity = CustomerEntity.builder().name(request.getName()).email(request.getEmail())
				.phone(request.getPhone()).totalSpent(0).createdDate(LocalDateTime.now()).build();

		CustomerEntity ent = shopRepo.save(customerEntity);

		if (ent.getId() != null) {

			return CustomerSuccessDTO.builder().success(true).customer(request).build();
		}

		return CustomerSuccessDTO.builder().success(false).customer(request).build();

	}

	public List<CustomerEntity> getAllCustomer() {

		return shopRepo.findAll().stream()
			    .sorted(Comparator.comparing(CustomerEntity::getCreatedDate).reversed())
			    .collect(Collectors.toList());
	}

	@Transactional
	public ProductSuccessDTO saveProduct(ProductRequest request) {

		String status = "In Stock";
		if (request.getStock() < 0)
			status = "Out of Stock";

		System.out.println("The new request" + request.getTax());

		ProductEntity productEntity = null;

		if (request.getSelectedProductId() != null && request.getSelectedProductId() != 0) {

			// prodRepo.addProductStock(request.getSelectedProductId(), request.getStock());

			productEntity = ProductEntity.builder().id(request.getSelectedProductId()).name(request.getName())
					.category(request.getCategory()).status(status).stock(request.getStock())
					.taxPercent(request.getTax()).price(request.getPrice()).build();

		} else {

			productEntity = ProductEntity.builder().name(request.getName()).category(request.getCategory())
					.status(status).stock(request.getStock()).taxPercent(request.getTax()).price(request.getPrice())
					.build();

		}

		ProductEntity ent = prodRepo.save(productEntity);
		if (ent.getId() != null) {

			return ProductSuccessDTO.builder().success(true).product(request).build();
		}

		return ProductSuccessDTO.builder().success(false).product(request).build();

	}

	public ProductSuccessDTO updateProduct(ProductRequest request) {

		String status = "In Stock";
		if (request.getStock() < 1)
			status = "Out of Stock";
		System.out.println("The updated request" + request.getTax());
		var productEntity = ProductEntity.builder().id(request.getSelectedProductId()).name(request.getName())
				.category(request.getCategory()).status(status).stock(request.getStock()).taxPercent(request.getTax())
				.price(request.getPrice()).build();

		ProductEntity ent = prodRepo.save(productEntity);

		if (ent.getId() != null) {

			return ProductSuccessDTO.builder().success(true).product(request).build();
		}

		return ProductSuccessDTO.builder().success(false).product(request).build();

	}

	public List<ProductEntity> getAllProducts() {

		return prodRepo.findAll();
	}

	@Transactional
	public BillingResponse doPayment(BillingRequest request) {

		Integer unitsSold = 0;
		for (var obj : request.getCart()) {
			unitsSold += obj.getQuantity();
		}
		var billingEntity = BillingEntity.builder().customerId(request.getSelectedCustomer().getId())
				.unitsSold(unitsSold).taxAmount(request.getTax()).totalAmount(request.getTotal())
				.subTotalAmount(request.getTotal() - request.getTax()).createdDate(LocalDateTime.now()).build();

		BillingEntity billResponse = billRepo.save(billingEntity);

		if (billResponse.getId() != null) {
			request.getCart().stream().forEach(obj -> {

				ProductEntity prodRes = prodRepo.findById(obj.getId()).get();
				System.out.println("Product details " + prodRes);
				Integer tax = (prodRes.getTaxPercent() * obj.getQuantity() * obj.getPrice()) / 100;
				Integer subTotal = obj.getQuantity() * obj.getPrice();
				Integer total = tax + subTotal;

				var productSalesEntity = ProductSalesEntity.builder().billingId(billResponse.getId())
						.productId(obj.getId()).quantity(obj.getQuantity()).tax(tax).subTotal(subTotal).total(total)
						.build();

				ProductSalesEntity prodSalesResponse = prodSalesRepo.save(productSalesEntity);

				if (prodSalesResponse.getId() != null) {
					prodRepo.updateProductStock(obj.getId(), obj.getQuantity());

				}

			});
			String paymentMethod = "CASH";
			if (request.getPaymentMethod() != null) {
				paymentMethod = request.getPaymentMethod();
			}

			var paymentEntity = PaymentEntity.builder().billingId(billResponse.getId()).createdDate(LocalDateTime.now())
					.paymentMethod(paymentMethod).status("Paid").tax(request.getTax())
					.subtotal(request.getTotal() - request.getTax()).total(request.getTotal()).build();

			salesPaymentRepo.save(paymentEntity);

			try {
				shopRepo.updateCustomerSpentAmount(request.getSelectedCustomer().getId(), request.getTotal());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			InvoiceDetails order = getOrderDetails(billResponse.getInvoiceNumber());
			
			try {
				CompletableFuture<String> futureResult = email.sendEmail(order.getCustomerEmail(), billResponse.getInvoiceNumber(), order.getCustomerName(), generateOrderInvoice(billResponse.getInvoiceNumber()));
				System.out.println(futureResult);
			} catch (MailjetException | MailjetSocketTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

			return BillingResponse.builder().paymentReferenceNumber(paymentEntity.getPaymentReferenceNumber())
					.invoiceNumber(billResponse.getInvoiceNumber()).status("SUCCESS").build();
		}

		return BillingResponse.builder().status("FAILURE").build();
	}

	public List<SalesResponseDTO> getAllSales() {

		List<BillingEntity> listOfBills = billRepo.findAll().stream()
			    .sorted(Comparator.comparing(BillingEntity::getCreatedDate).reversed())
			    .collect(Collectors.toList());
		List<SalesResponseDTO> response = new ArrayList();
		listOfBills.stream().forEach(obj -> {
			// SalesResponseDTO salesResponse
			var salesResponse = SalesResponseDTO.builder()
					.customer(shopRepo.findById(obj.getCustomerId()).get().getName())
					.date(String.valueOf(obj.getCreatedDate())).id(obj.getInvoiceNumber()).total(obj.getTotalAmount())
					.status(salesPaymentRepo.findPaymentDetails(obj.getId()).getStatus()).build();

			response.add(salesResponse);

		});

		return response;
	}

	public DasbboardResponseDTO getDashBoardDetails() {
		List<BillingEntity> billList = billRepo.findAll();
		List<ProductEntity> prodList = prodRepo.findAll();

		Integer monthlyRevenue = 0;
		Integer taxCollected = 0;
		Integer totalUnitsSold = 0;
		Integer outOfStockCount = 0;

		for (BillingEntity obj : billList) {
			monthlyRevenue = monthlyRevenue + obj.getTotalAmount();
			taxCollected = taxCollected + obj.getTaxAmount();
			totalUnitsSold = totalUnitsSold + obj.getUnitsSold();
		}
		;
		for (ProductEntity obj : prodList) {
			if (obj.getStock() < 1)
				outOfStockCount = outOfStockCount + 1;
		}
		;

		return DasbboardResponseDTO.builder().monthlyRevenue(monthlyRevenue).outOfStockCount(outOfStockCount)
				.taxCollected(taxCollected).totalUnitsSold(totalUnitsSold).build();
	}

	public List<PaymentDetails> getPaymentList() {
		List<BillingEntity> billList = billRepo.findAll();
		List<PaymentDetails> response = new ArrayList<>();
		billList.stream().forEach(obj -> {

			response.add(PaymentDetails.builder()
					.id(salesPaymentRepo.findPaymentDetails(obj.getId()).getPaymentReferenceNumber())
					.amount(obj.getTotalAmount()).date(String.valueOf(obj.getCreatedDate()))
					.saleId(obj.getInvoiceNumber())
					.method(salesPaymentRepo.findPaymentDetails(obj.getId()).getPaymentMethod()).build());
		});

		return response;
	}

	public ProductSuccessDTO uploadProduct(File request) {

		return null;
	}

	public List<ProductRequest> uploadBulkProduct(MultipartFile file) {

		try {
			List<ProductRequest> prodList = util.parseCsv(file);
			System.out.println(prodList);
			prodList.stream().forEach(obj -> {
				ProductSuccessDTO prodsaveResponse = saveProduct(obj);
				System.out.println(prodsaveResponse);
			});
			return prodList;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public ByteArrayOutputStream generateOrderInvoice(String orderId)

	{
		
		InvoiceDetails order = getOrderDetails(orderId);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    Document document = new Document(PageSize.A4, 40, 40, 50, 50);
	    PdfWriter.getInstance(document, baos);
	    document.open();

	    // Colors
	    Color indigo = new Color(63, 81, 181);
	    Color headerText = Color.WHITE;
	    Color altRow = new Color(240, 240, 240);
	    Color accentBlue = new Color(33, 150, 243);
	    Color muted = Color.GRAY;

	    // Fonts
	    Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
	    Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD, headerText);
	    Font bodyFont = new Font(Font.HELVETICA, 12);
	    Font totalFont = new Font(Font.HELVETICA, 14, Font.BOLD, accentBlue);
	    Font footerFont = new Font(Font.HELVETICA, 10, Font.ITALIC, muted);
	    Font badgePaid = new Font(Font.HELVETICA, 14, Font.BOLD, Color.GREEN);
	    Font badgeUnpaid = new Font(Font.HELVETICA, 14, Font.BOLD, Color.RED);

	    // Logo
	    try {
	        Image logo = Image.getInstance(this.getClass().getResource("/static/logo.png"));
	        logo.scaleAbsolute(60, 60);
	        logo.setAlignment(Image.ALIGN_LEFT);
	        document.add(logo);
	    } catch (Exception ignored) {}

	    // Title + Payment Status Badge
	    Paragraph title = new Paragraph(new Phrase("INVOICE", titleFont));
	    title.setAlignment(Element.ALIGN_CENTER);
	    document.add(title);

	    String statusText = order.isPaid() ? "PAID" : "UNPAID";
	    Font statusFont = order.isPaid() ? badgePaid : badgeUnpaid;
	    Paragraph status = new Paragraph(new Phrase(statusText, statusFont));
	    status.setAlignment(Element.ALIGN_CENTER);
	    document.add(status);

	    document.add(new Paragraph(" ")); // spacer

	    NumberFormat inr = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
	    document.add(new Paragraph(new Phrase("Order ID: " + order.getInvoiceId(), bodyFont)));
	    document.add(new Paragraph(new Phrase("Date: " + LocalDate.now(), bodyFont)));
	    document.add(new Paragraph(new Phrase("Customer: " + order.getCustomerName(), bodyFont)));
		/*
		 * if (order.getCustomerAddress() != null) { document.add(new Paragraph(new
		 * Phrase("Address: " + order.getCustomerAddress(), bodyFont))); }
		 */
	    document.add(new Paragraph(" "));

	    // Items table
	    PdfPTable table = new PdfPTable(4);
	    table.setWidthPercentage(100);
	    table.setSpacingBefore(10f);
	    table.setWidths(new float[] { 5f, 1.5f, 2f, 2f });

	    Stream.of("Item", "Qty", "Price", "Total").forEach(col -> {
	        PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
	        cell.setBackgroundColor(indigo);
	        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        cell.setPadding(8);
	        cell.setBorderColor(indigo.darker());
	        table.addCell(cell);
	    });

	    int rowIndex = 0;
	    for (OrderItem item : order.getItems()) {
	        Color bg = (rowIndex % 2 == 0) ? Color.WHITE : altRow;
	        PdfPCell c1 = new PdfPCell(new Phrase(item.getProductName(), bodyFont));
	        PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), bodyFont));
	        PdfPCell c3 = new PdfPCell(new Phrase(inr.format(item.getUnitPrice()), bodyFont));
	        PdfPCell c4 = new PdfPCell(new Phrase(inr.format(item.getQuantity() * item.getUnitPrice()), bodyFont));

	        for (PdfPCell c : Arrays.asList(c1, c2, c3, c4)) {
	            c.setBackgroundColor(bg);
	            c.setHorizontalAlignment(Element.ALIGN_CENTER);
	            c.setPadding(6);
	            c.setBorderColor(indigo.darker());
	        }
	        table.addCell(c1);
	        table.addCell(c2);
	        table.addCell(c3);
	        table.addCell(c4);
	        rowIndex++;
	    }

	    document.add(table);

	    // GST & Discount rows
	    double gstRate = 0.18; // 18%
	    double discountRate = 0.10; // 10%
	    double gstAmount = order.getTotalAmount() * gstRate;
	    double discountAmount = order.getTotalAmount() * discountRate;
	    double finalTotal = order.getTotalAmount() + gstAmount - discountAmount;

	    document.add(new Paragraph(new Phrase("GST (18%): " + inr.format(gstAmount), bodyFont)));
	    document.add(new Paragraph(new Phrase("Discount (10%): -" + inr.format(discountAmount), bodyFont)));

	    // Final total highlighted
	    Paragraph total = new Paragraph(new Phrase("Grand Total: " + inr.format(finalTotal), totalFont));
	    total.setAlignment(Element.ALIGN_RIGHT);
	    document.add(total);

	    document.add(new Paragraph(" "));

	    // Footer
	    Paragraph footer = new Paragraph(new Phrase("Thank you for your business!", footerFont));
	    footer.setAlignment(Element.ALIGN_CENTER);
	    document.add(footer);

	    document.close();

	return baos;
}
	
public InvoiceDetails getOrderDetails(String orderReferenceNumber) {

	BillingEntity billDetails = billRepo.findOrderByReference(orderReferenceNumber);

	PaymentEntity paymentEntity = salesPaymentRepo.findPaymentDetails(billDetails.getId());
	
	boolean paid=false;
	if(paymentEntity.getStatus().equalsIgnoreCase("Paid")) {
		paid=true;
	}
	
	CustomerEntity customerEntity=shopRepo.findById(billDetails.getCustomerId()).get();

	List<ProductSalesEntity> prodSales = prodSalesRepo.findByOrderId(billDetails.getId());
	Double gst = 0d;
	for (ProductSalesEntity orders : prodSales) {
		gst = gst + orders.getTax();
	}

	List<OrderItem> items = prodSales.stream().map(obj -> {

		ProductEntity prodRes = prodRepo.findById(obj.getProductId()).get();

		var orderItems = OrderItem.builder().productName(prodRes.getName()).unitPrice(prodRes.getPrice())
				.quantity(obj.getQuantity()).build();
		return orderItems;
	}).collect(Collectors.toList());
	var response = InvoiceDetails.builder().discountRate(0).invoiceId(orderReferenceNumber)
			.paymentReferenceNumber(paymentEntity.getPaymentReferenceNumber()).items(items).gstRate(gst)
			.totalAmount(billDetails.getTotalAmount())
			.customerName(customerEntity.getName())
			.customerEmail(customerEntity.getEmail())
			.customerPhone(customerEntity.getPhone())
			.paid(paid)
			.build();
	return response;
}

}
