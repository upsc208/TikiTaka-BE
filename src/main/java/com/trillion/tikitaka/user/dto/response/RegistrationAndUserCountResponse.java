package com.trillion.tikitaka.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationAndUserCountResponse {
    private Long RegistrationCount;
    private Long userCount;
}
