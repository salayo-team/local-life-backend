package com.salayo.locallifebackend.domain.email.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailSendRequestDto {

    private String email;

}
