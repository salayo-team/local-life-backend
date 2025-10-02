package com.salayo.locallifebackend.domain.payment.enums;

import java.util.Arrays;
import java.util.Optional;

public enum PaymentMethodType {
	CARD;

	public static Optional<PaymentMethodType> from(String value) {

		if (value == null || value.trim().isEmpty()) {
			return Optional.empty();
		}

		String normalizedValue = value.trim();
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(normalizedValue))
			.findFirst();
	}
}
