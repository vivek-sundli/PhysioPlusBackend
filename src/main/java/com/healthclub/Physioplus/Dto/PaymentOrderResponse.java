package com.healthclub.Physioplus.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentOrderResponse {

    private boolean success;
    private String message;
    private String orderId;
    private String paymentId;  // Internal payment ID
    private double amount;
    private String currency;
    private String razorpayKeyId;

    public PaymentOrderResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PaymentOrderResponse(boolean success, String message, String orderId, String paymentId,
                                 double amount, String currency, String razorpayKeyId) {
        this.success = success;
        this.message = message;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.razorpayKeyId = razorpayKeyId;
    }

    public static PaymentOrderResponse success(String orderId, String paymentId, double amount,
                                                String currency, String razorpayKeyId) {
        return new PaymentOrderResponse(true, "Order created successfully", orderId, paymentId,
                amount, currency, razorpayKeyId);
    }

    public static PaymentOrderResponse error(String message) {
        return new PaymentOrderResponse(false, message);
    }
}
