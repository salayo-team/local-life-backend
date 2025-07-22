package com.salayo.locallifebackend.global.config;

import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.siot.IamportRestClient.IamportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamportConfig {

	@Value("${IAMPORT_API_KEY}")
	private String apiKey;

	@Value("${IAMPORT_API_SECRET_KEY}")
	private String apiSecretKey;

	@Bean
	public IamportClient iamportClient(){
		if(apiKey == null || apiSecretKey == null){
			throw new CustomException(ErrorCode.MISSING_API_KEY);
		}

		return new IamportClient(apiKey, apiSecretKey);
	}

}
