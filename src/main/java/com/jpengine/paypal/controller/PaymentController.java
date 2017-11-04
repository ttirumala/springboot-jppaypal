package com.jpengine.paypal.controller;

import com.jpengine.paypal.config.PaypalPaymentIntent;
import com.jpengine.paypal.config.PaypalPaymentMethod;
import com.jpengine.paypal.service.PaypalService;
import com.jpengine.paypal.util.URLUtils;
import com.paypal.api.payments.*;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
public class PaymentController {

	public static final String PAYPAL_SUCCESS_URL = "pay/success";
	public static final String PAYPAL_CANCEL_URL = "pay/cancel";
	public static final String PAYPAL_PROCESS_URL = "pay/process";
	public static final String PAYPAL_CAPTURE_URL = "pay/capture";
	public static final String PAYPAL_CAPSUCCESS_URL = "pay/capsuccess";
	public static final String PAYPAL_REFCAPTURE_URL = "pay/refcapture";


	private static Authorization authorization;

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private PaypalService paypalService;

	@RequestMapping(method = RequestMethod.GET)
	public String index(){
		return "index";
	}

	@RequestMapping(method = RequestMethod.POST, value = "pay")
	public String pay(HttpServletRequest request){
		String cancelUrl = URLUtils.getBaseURl(request) + "/" + PAYPAL_CANCEL_URL;
		String successUrl = URLUtils.getBaseURl(request) + "/" + PAYPAL_SUCCESS_URL;
		try {
			Payment payment = paypalService.createPayment(
					4.54,
					"USD",
					PaypalPaymentMethod.paypal,
					PaypalPaymentIntent.sale,
					"payment description",
					cancelUrl,
					successUrl);
			for(Links links : payment.getLinks()){
				if(links.getRel().equals("approval_url")){
					return "redirect:" + links.getHref();
				}
			}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		return "redirect:/";
	}

	@RequestMapping(method = RequestMethod.POST, value = "auth")
	public String auth(HttpServletRequest request){
		String cancelUrl = URLUtils.getBaseURl(request) + "/" + PAYPAL_CANCEL_URL;
		String successUrl = URLUtils.getBaseURl(request) + "/" + PAYPAL_PROCESS_URL;
		try {
			Payment authPayment = paypalService.authorizePayment(
					4.54,
					"USD",
					PaypalPaymentMethod.paypal,
					PaypalPaymentIntent.authorize,
					"payment auth description",
					cancelUrl,
					successUrl);
			System.out.println(authPayment.toString());
			for(Links links : authPayment.getLinks()){
				if(links.getRel().equals("approval_url")){
					return "redirect:" + links.getHref();
				}
			}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		return "redirect:/";
	}

	@RequestMapping(method = RequestMethod.POST, value = "capture")
	public String capture(HttpServletRequest request){
		try {
			Amount amount = new Amount();
			amount.setCurrency("USD");
			amount.setTotal("4.54");
			Capture capture = paypalService.executeCapture(authorization,amount);
			System.out.println("Capture: "+capture.toJSON());
			if(capture.getState().equals("completed")){
				return "capsuccess";
			}
			//for(Links links : capture.getLinks()){
			//	if(links.getRel().equals("self")){
					//return "redirect:" + links.getHref();
			//	}
			//}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		return "redirect:/";
	}

	@RequestMapping(method = RequestMethod.GET, value = PAYPAL_CANCEL_URL)
	public String cancelPay(){
		return "cancel";
	}

	@RequestMapping(method = RequestMethod.GET, value = PAYPAL_SUCCESS_URL)
	public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId){
		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			if(payment.getState().equals("approved")){
				return "success";
			}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		return "redirect:/";
	}

	@RequestMapping(method = RequestMethod.GET, value = PAYPAL_PROCESS_URL)
	public String successAuth(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId){
		try {
			authorization = paypalService.executeAuthorization(paymentId, payerId);
			System.out.println("Authorization:"+authorization.toJSON());
			if(authorization.getState().equals("approved")){
				return "process";
			}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		return "redirect:/";
	}

	@RequestMapping(method = RequestMethod.GET, value = PAYPAL_REFCAPTURE_URL)
	public String refundCapture(@RequestParam("captureId") String captureId, @RequestParam("PayerID") String payerId){

		return "redirect:/";
	}

}
