package com.trillion.tikitaka.statistics.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AllType {
    private Long typeId;
    private String typeName;
    private int totalCreated;

    public void updateAllType(Long typeId,String typename,int totalCreated){
        this.typeId = typeId;
        this.typeName = typename;
        this.totalCreated = totalCreated;
    }


}
