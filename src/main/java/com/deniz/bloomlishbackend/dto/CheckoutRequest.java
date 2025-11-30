package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {

    private String planType;         // MONTHLY / YEARLY
    private String cardHolderName;
    private String cardNumber;
    private int expiryMonth;
    private int expiryYear;
    private String cvc;
    private String billingAddress;
}
/*Billing sayfasındaki formdan gelen her şey burada toplanacak.

Biz fake ödeme yaptığımız için kart bilgilerini hiçbir yere kaydetmeyeceğiz,
 sadece “sözde kontrol edip” bir Subscription & Payment oluşturacağız.*/