package com.jpengine.paypal.service;

import java.util.ArrayList;
import java.util.List;

import com.paypal.api.payments.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jpengine.paypal.config.PaypalPaymentIntent;
import com.jpengine.paypal.config.PaypalPaymentMethod;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

@Service
public class PaypalService {

	@Autowired
	private APIContext apiContext;

	public Payment createPayment(
			Double total,
			String currency,
			PaypalPaymentMethod method,
			PaypalPaymentIntent intent,
			String description,
			String cancelUrl,
			String successUrl) throws PayPalRESTException{

		// Set payment details
		Details details = new Details();
		details.setShipping("0.50");
		details.setSubtotal("4");
		details.setTax("0.04");

		Amount amount = new Amount();
		amount.setCurrency(currency);
		amount.setTotal(String.format("%.2f", total));
		amount.setDetails(details);

		Transaction transaction = new Transaction();
		transaction.setDescription(description);
		transaction.setAmount(amount);

		List<Transaction> transactions = new ArrayList<>();
		transactions.add(transaction);

		Payer payer = new Payer();
		payer.setPaymentMethod(method.toString());

		Payment payment = new Payment();
		payment.setIntent(intent.toString());
		payment.setPayer(payer);
		payment.setTransactions(transactions);
		RedirectUrls redirectUrls = new RedirectUrls();
		redirectUrls.setCancelUrl(cancelUrl);
		redirectUrls.setReturnUrl(successUrl);
		payment.setRedirectUrls(redirectUrls);

		return payment.create(apiContext);
	}

	public Payment authorizePayment(
			Double total,
			String currency,
			PaypalPaymentMethod method,
			PaypalPaymentIntent intent,
			String description,
			String cancelUrl,
			String successUrl) throws PayPalRESTException{

		// Set payment details
		Details details = new Details();
		details.setShipping("0.50");
		details.setSubtotal("4");
		details.setTax("0.04");

		Amount amount = new Amount();
		amount.setCurrency(currency);
		amount.setTotal(String.format("%.2f", total));
		amount.setDetails(details);

		Transaction transaction = new Transaction();
		transaction.setDescription(description);
		transaction.setAmount(amount);

		List<Transaction> transactions = new ArrayList<>();
		transactions.add(transaction);

		Payer payer = new Payer();
		payer.setPaymentMethod(method.toString());

		Payment payment = new Payment();
		payment.setIntent(intent.toString());
		payment.setPayer(payer);
		payment.setTransactions(transactions);
		RedirectUrls redirectUrls = new RedirectUrls();
		redirectUrls.setCancelUrl(cancelUrl);
		redirectUrls.setReturnUrl(successUrl);
		payment.setRedirectUrls(redirectUrls);
		return payment.create(apiContext);
		//System.out.println("JSON->:"+payment.getTransactions().get(0).toJSON());
		//System.out.println("JSON->:"+payment.getTransactions().get(0).getRelatedResources());
		//System.out.println("JSON->:"+payment.getTransactions().get(0).getRelatedResources().get(0).toJSON());
		//return payment.getTransactions().get(0).getRelatedResources().get(0).getAuthorization();

	}

	public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException{
		Payment payment = new Payment();
		payment.setId(paymentId);
		PaymentExecution paymentExecute = new PaymentExecution();
		paymentExecute.setPayerId(payerId);
		return payment.execute(apiContext, paymentExecute);
	}

	public Authorization executeAuthorization(String paymentId, String payerId) throws PayPalRESTException{
		Payment payment = new Payment();
		payment.setId(paymentId);
		PaymentExecution paymentExecute = new PaymentExecution();
		paymentExecute.setPayerId(payerId);
		Payment createdAuthPayment = payment.execute(apiContext, paymentExecute);
		return createdAuthPayment.getTransactions().get(0).getRelatedResources().get(0).getAuthorization();
	}

	public Capture executeCapture(Authorization auth, Amount amount) throws PayPalRESTException{
		Capture capture = new Capture();
		capture.setAmount(amount);
		// Set as final capture amount
		capture.setIsFinalCapture(true);
		// Capture payment
		return auth.capture(apiContext, capture);
	}
}
