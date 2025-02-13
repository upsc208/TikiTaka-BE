package com.trillion.tikitaka.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DailyCategoryStatisticsResponse {
    private Long firstCategoryId;
    private String firstCategoryName;
    private List<SecondCategoryInfo> secondCategories;
    private int totalTicketCount;

    @Getter
    @AllArgsConstructor
    public static class SecondCategoryInfo {
        private Long secondCategoryId;
        private String secondCategoryName;
        private int ticketCount;
    }
    @Override
    public String toString() {
        return "DailyCategoryStatisticsResponse{" +
                "firstCategoryId=" + firstCategoryId +
                ", firstCategoryName='" + firstCategoryName + '\'' +
                ", secondCategories=" + secondCategories +
                ", totalTicketCount=" + totalTicketCount +
                '}';
    }

}
