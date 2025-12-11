package com.salayo.locallifebackend.domain.payment.enums;

import java.util.Arrays;
import java.util.Optional;

public enum PaymentProvider {
	KG_INICIS("html5_inicis");

	private final String iamportCode;

	PaymentProvider(String iamportCode) {
		this.iamportCode = iamportCode;
	}

	public static Optional<PaymentProvider> from(String value) {

		if (value == null || value.trim().isEmpty()) {
			return Optional.empty();
		}

		String normalizedValue = value.trim();
		return Arrays.stream(values())
			.filter(provider -> provider.name().equalsIgnoreCase(normalizedValue)
					|| provider.iamportCode.equalsIgnoreCase(normalizedValue))
			.findFirst();
	}
}
