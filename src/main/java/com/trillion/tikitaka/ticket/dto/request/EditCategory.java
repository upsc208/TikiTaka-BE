package com.trillion.tikitaka.ticket.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditCategory {
    private Long firstCategoryId;

    private Long secondCategoryId;
}
