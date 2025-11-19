package com.salayo.locallifebackend.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PaymentRequestDto {

	@NotNull(message = "예약 고유 식별자는 필수값입니다.")
	private Long reservationId;

	private String pgTid;

	private String impUid;

	private BigDecimal paymentCost;

	private String paymentCard;

	@NotBlank(message = "결제 수단 타입은 필수값입니다.")
	private String paymentMethodType;

	@NotBlank(message = "결제 대행사는 필수값입니다.")
	private String paymentProvider;

}
