package com.management.shop.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.management.shop.dto.BillingRequest;
import com.management.shop.dto.BillingResponse;
import com.management.shop.dto.CustomerRequest;
import com.management.shop.dto.CustomerSuccessDTO;
import com.management.shop.dto.DasbboardResponseDTO;
import com.management.shop.dto.PaymentDetails;
import com.management.shop.dto.ProductRequest;
import com.management.shop.dto.ProductSuccessDTO;
import com.management.shop.dto.SalesResponseDTO;
import com.management.shop.entity.CustomerEntity;
import com.management.shop.entity.ProductEntity;
import com.management.shop.service.ShopService;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

	@Autowired
	ShopService serv;


	@PostMapping("/create/customer")
	ResponseEntity<CustomerSuccessDTO> createCustomer(@RequestBody CustomerRequest request) {

		CustomerSuccessDTO response = serv.saveCustomer(request);

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@GetMapping("/get/customersList")
	ResponseEntity<List<CustomerEntity>> getCustomersList() {

		List<CustomerEntity> response = serv.getAllCustomer();

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PostMapping("/create/product")
	ResponseEntity<ProductSuccessDTO> createCustomer(@RequestBody ProductRequest request) {

		ProductSuccessDTO response = serv.saveProduct(request);

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}
	@PostMapping("/upload/productList")
	ResponseEntity<ProductSuccessDTO> createCustomer(@RequestBody File request) {

		ProductSuccessDTO response = serv.uploadProduct(request);

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PutMapping("/update/product")
	ResponseEntity<ProductSuccessDTO> updateCustomer(@RequestBody ProductRequest request) {

		ProductSuccessDTO response = serv.updateProduct(request);

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@GetMapping("/get/productsList")
	ResponseEntity<List<ProductEntity>> getProductsList() {

		List<ProductEntity> response = serv.getAllProducts();

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PostMapping("/do/billing")
	ResponseEntity<BillingResponse> doBilling(@RequestBody BillingRequest request) {
		
			System.out.println("The request payload for billing app is-->"+request);
		
			BillingResponse response=serv.doPayment(request);
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/get/sales")
	ResponseEntity<List<SalesResponseDTO>> getSalesList(){
		
		List<SalesResponseDTO> response=serv.getAllSales();
		
		return ResponseEntity.status(HttpStatus.OK).body(response);
		
		
	}
	@GetMapping("/get/dashboardDetails")
	ResponseEntity<DasbboardResponseDTO> getDashBoardDetails(){
		
		DasbboardResponseDTO response=serv.getDashBoardDetails();
		
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}
	@GetMapping("/get/paymentLists")
	ResponseEntity<List<PaymentDetails>> getPaymentList(){
		List<PaymentDetails> response=serv.getPaymentList();
		
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	 @PostMapping(path = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	    public ResponseEntity<?> bulkUpload(@RequestPart("file") MultipartFile file) {
	        try {
	            List<ProductRequest> products = serv.uploadBulkProduct(file);

	            // TODO: persist products (e.g., productService.saveAll(products));

	            Map<String, Object> body = new HashMap<>();
	            body.put("count", products.size());
	            body.put("items", products);
	            return ResponseEntity.ok(body);
	        } catch (IllegalArgumentException ex) {
	            return ResponseEntity.badRequest().body(error("Bad CSV: " + ex.getMessage()));
	        } catch (Exception ex) {
	            return ResponseEntity.status(500).body(error("Upload failed: " + ex.getMessage()));
	        }
	    }

	    private Map<String, String> error(String message) {
	        Map<String, String> map = new HashMap<>();
	        map.put("message", message);
	        return map;
	    }
	    
	    @GetMapping("/get/invoice/{orderReferenceNumber}")
	    public ResponseEntity<byte[]> downloadStyledInvoice(@PathVariable String orderReferenceNumber){
	    	
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	baos=serv.generateOrderInvoice(orderReferenceNumber);
	    	
	    	  return ResponseEntity.ok()
	    		        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + orderReferenceNumber + ".pdf")
	    		        .contentType(MediaType.APPLICATION_PDF)
	    		        .body(baos.toByteArray());
	    }

	

}
