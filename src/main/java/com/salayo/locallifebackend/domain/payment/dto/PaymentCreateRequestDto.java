package com.salayo.locallifebackend.domain.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PaymentCreateRequestDto {

	@NotBlank(message = "PG사 거래 ID는 필수값입니다.")
	private String pgTid;

	private String impUid;

	@NotNull(message = "결제 금액은 필수값입니다.")
	@DecimalMin(value = "0.0", inclusive = false, message = "결제 금액은 0보다 커야 합니다.")
	private BigDecimal paymentCost;

	@NotBlank(message = "결제 카드 정보는 필수값입니다.")
	private String paymentCard;

	private String paymentMethodType;

}
