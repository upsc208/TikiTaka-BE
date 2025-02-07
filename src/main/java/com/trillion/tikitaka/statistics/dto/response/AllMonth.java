package com.trillion.tikitaka.statistics.dto.response;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AllMonth {
    private int create;
    private int urgent;
    private int complete;

    public void updateAllMonth(int create, int urgent, int complete){
        this.create = create;
        this.complete = complete;
        this.urgent = urgent;
    }

}
