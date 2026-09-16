package com.org.paymentprocessor.controller;

import com.org.paymentprocessor.dto.api.PaymentRequest;
import com.org.paymentprocessor.dto.api.PaymentResponse;
import com.org.paymentprocessor.model.PaymentStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @GetMapping
    ResponseEntity<List<PaymentResponse>> getPayments() {
        List<PaymentResponse> payments = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            PaymentResponse payment = new PaymentResponse();
            payment.setId("payment-" + "i");
            payment.setStatus(PaymentStatus.PROCESSED);
            payment.setMessage("Payment processed successfully.");
            payment.setProcessedAt(OffsetDateTime.now());

            payments.add(payment);
        }

        return ResponseEntity.ok(payments);
    }

    @PostMapping
    ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setId(paymentRequest.getId());
        paymentResponse.setStatus(PaymentStatus.PROCESSED);
        paymentResponse.setMessage("Payment processed successfully for: " + paymentRequest.getAmount() + " " + paymentRequest.getCurrency()+ ".");
        paymentResponse.setProcessedAt(OffsetDateTime.now());

        return ResponseEntity.ok(paymentResponse);

    }



}
