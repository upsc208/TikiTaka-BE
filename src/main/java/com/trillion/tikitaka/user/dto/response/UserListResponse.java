package com.trillion.tikitaka.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    List<UserResponse> users;
    Long adminCount;
    Long managerCount;
    Long userCount;
}
