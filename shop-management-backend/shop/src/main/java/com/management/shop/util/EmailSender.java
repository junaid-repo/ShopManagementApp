package com.management.shop.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;

@Component
public class EmailSender {
	@Async("mailAsync")
	public  CompletableFuture<String> sendEmail2(String emailId, String message, String name, ByteArrayOutputStream byteArrayOutputStream) throws MailjetException, MailjetSocketTimeoutException {
		MailjetClient client;
		MailjetRequest request;
		MailjetResponse response;
		//System.out.println("From emailId is-->"+fromEmail);
		System.out.println("To emailId is-->"+emailId);

		client = new MailjetClient("3e292e1e3e850abe850793dbb22554b9",
				"2fa15000afb8c7ad2cd676c9828bcd5e", new ClientOptions("v3.1"));
		request = new MailjetRequest(Emailv31.resource).property(Emailv31.MESSAGES,
				new JSONArray().put(new JSONObject()
						.put(Emailv31.Message.FROM, new JSONObject().put("Email", "tahanasim3001@gmail.com")
								.put("Name", "Friends Mobile"))
						.put(Emailv31.Message.TO,
								new JSONArray().put(
										new JSONObject().put("Email", emailId).put("JPC Waqf Board", "Hello")))
						.put(Emailv31.Message.SUBJECT, "Order has been confirmed")
						.put(Emailv31.Message.TEXTPART, "Dear Mr. "+name+"  Your order has been confirmed")
						.put(Emailv31.Message.HTMLPART,
								"<h3>"+"Dear Mr. "+name+"  Your order has been confirmed")
						.put(Emailv31.Message.CUSTOMID, "AppGettingStartedTest")));
		response = client.post(request);
		System.out.println(response.getStatus());
		System.out.println(response.getData());
		return CompletableFuture.completedFuture(response.getData().toString());
	}
	 public CompletableFuture<String> sendEmail(String emailId, String orderId, String name, ByteArrayOutputStream pdfStream) throws MailjetException, MailjetSocketTimeoutException {
	        // Assume you have a ByteArrayOutputStream named 'pdfStream'
	        // This stream would contain the PDF data, for example, from a PDF generator library.
	       // ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

	        // **NOTE:** In a real-world scenario, you would write the PDF data to 'pdfStream' here.
	        // For this example, we'll simulate a small PDF's content.
	        try {
	            pdfStream.write("This is a simulated PDF file content.".getBytes());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        String base64Content = "";
	        try {
	            // 1. Get the byte array from the ByteArrayOutputStream
	            byte[] fileContent = pdfStream.toByteArray();

	            // 2. Encode the byte array to a Base64 string
	            base64Content = Base64.getEncoder().encodeToString(fileContent);
	        } finally {
	            try {
	                pdfStream.close(); // Always close the stream
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	        MailjetClient client;
	        MailjetRequest request;
	        MailjetResponse response;
	        client = new MailjetClient("3e292e1e3e850abe850793dbb22554b9",
					"2fa15000afb8c7ad2cd676c9828bcd5e", new ClientOptions("v3.1"));
	        request = new MailjetRequest(Emailv31.resource)
	                .property(Emailv31.MESSAGES, new JSONArray()
	                        .put(new JSONObject()
	            					.put(Emailv31.Message.FROM, new JSONObject().put("Email", "help@friendsmobile.store")
	        								.put("Name", "Friends Mobile"))
	        						.put(Emailv31.Message.TO,
	        								new JSONArray().put(
	        										new JSONObject().put("Email", emailId).put("Friends Mobile", "Hello")))
	        						.put(Emailv31.Message.SUBJECT, "Order has been confirmed with Order Number "+orderId)
	                                .put(Emailv31.Message.TEXTPART, "Dear Mr."+name+" Welcome to Friends Mobile")
	                                .put(Emailv31.Message.HTMLPART, "<h3>We are happy to inform to inform you that we received your payment and your order is confirmed. Please find below the order details attached as pdf document.\n"
	                                		+ "Really happy to serve you.\n"
	                                		+ "\n"
	                                		+ "Thanks a lot.")
	                                .put(Emailv31.Message.ATTACHMENTS, new JSONArray()
	                                        .put(new JSONObject()
	                                                .put("ContentType", "application/pdf")
	                                                .put("Filename", orderId+".pdf")
	                                                .put("Base64Content", base64Content)))));

	        response = client.post(request);
	        System.out.println(response.getStatus());
	        System.out.println(response.getData());
			return CompletableFuture.completedFuture(response.getData().toString());

	    }
}
