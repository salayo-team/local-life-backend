package com.salayo.locallifebackend.domain.payment.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/payments")
@Tag(name = "Payment", description = "결제 관련 API")
@RestController
public class PaymentController {


}
