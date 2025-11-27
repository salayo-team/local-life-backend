package com.salayo.locallifebackend.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PaymentRefundRequestDto {

	private String refundReason;

	@Builder
	public PaymentRefundRequestDto(String refundReason) {
		this.refundReason = refundReason;
	}

}
