package com.trillion.tikitaka.statistics.dto.response;

import com.trillion.tikitaka.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Getter
@NoArgsConstructor
public class AllUser {

    private String userName;
    private Long userId;
   private String userEmail;
   private String userProfile;
   private int totalManagingCreatedTicket;

    public void updateAllUser(String userName,String userEmail,Long userId,String userProfile,int totalManagingCreatedTicket){
        this.userEmail = userEmail;
        this.userId = userId;
        this.userName = userName;
        this.userProfile = userProfile;
        this.totalManagingCreatedTicket = totalManagingCreatedTicket;
    }
}
