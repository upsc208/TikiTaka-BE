package com.trillion.tikitaka.statistics.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AllUser {
    private String userName;
    private Long userId;
    private String userEmail;
    private String userProfile;
    private int doneTickets;  // ✅ 완료된 티켓 수 추가
    private int inProgressTickets; // ✅ 진행 중인 티켓 수 추가

    public void updateAllUser(String userName, String userEmail, Long userId, String userProfile, int doneTickets, int inProgressTickets) {
        this.userEmail = userEmail;
        this.userId = userId;
        this.userName = userName;
        this.userProfile = userProfile;
        this.doneTickets = doneTickets;
        this.inProgressTickets = inProgressTickets;
    }
}
