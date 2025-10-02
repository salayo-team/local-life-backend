package com.salayo.locallifebackend.domain.payment.enums;

import java.util.Arrays;
import java.util.Optional;

public enum PaymentProvider {
	KG_INICIS;

	public static Optional<PaymentProvider> from(String value) {

		if (value == null || value.trim().isEmpty()) {
			return Optional.empty();
		}

		String normalizedValue = value.trim();
		return Arrays.stream(values())
			.filter(provider -> provider.name().equalsIgnoreCase(normalizedValue))
			.findFirst();
	}
}
