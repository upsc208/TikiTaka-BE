package com.trillion.tikitaka.statistics.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AllCategory {
    private String firstCategoryName;
    private String secondCategoryName;
    private int totalCreated;

    public void updateAllCategory(String firstCategoryName, String secondCategoryName,int totalCreated){
        this.firstCategoryName = firstCategoryName;
        this.secondCategoryName = secondCategoryName;
        this.totalCreated = totalCreated;

    }
}
