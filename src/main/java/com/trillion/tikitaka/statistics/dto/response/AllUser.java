package com.trillion.tikitaka.statistics.dto.response;

import com.trillion.tikitaka.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Getter
@NoArgsConstructor
public class AllUser {
    /*담당자별(그냥 list not page,사용자 유저이름,아이디,이메일,프로필사진 주소)
일별-담당자별 담당중인 처리중인 티켓의 갯수(진행중,대기중),완료한 갯수

월별-이번달 생성된 티켓중 담당자 본인인 티켓 전부
      ->year,month,userId기반으로 통계조회
*/
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
