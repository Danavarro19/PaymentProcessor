package com.org.paymentprocessor.controller;

import com.org.paymentprocessor.dto.api.PaymentRequest;
import com.org.paymentprocessor.dto.api.PaymentResponse;
import com.org.paymentprocessor.exception.InvalidDateRangeException;
import com.org.paymentprocessor.model.PaymentStatus;
import com.org.paymentprocessor.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to
    ) {
        try {
            List<PaymentResponse> payments = paymentService.getPayments(customerId, from, to);
            return ResponseEntity.ok(payments);
        } catch (InvalidDateRangeException e) {
            return ResponseEntity.badRequest().build();
        }
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
