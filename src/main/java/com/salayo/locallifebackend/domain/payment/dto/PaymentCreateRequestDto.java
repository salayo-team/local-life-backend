package com.salayo.locallifebackend.domain.payment.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PaymentCreateRequestDto {

	private Long reservationId;

	private String pgTid;

	private String impUid;

	private BigDecimal paymentCost;

	private String paymentCard;

	private String paymentMethodType;

	private String paymentProvider;

}
