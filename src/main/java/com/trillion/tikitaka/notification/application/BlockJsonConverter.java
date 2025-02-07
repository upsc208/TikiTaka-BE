package com.trillion.tikitaka.notification.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.notification.dto.response.Block;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlockJsonConverter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String convertBlocksToJson(List<Block> blocks) {
        try {
            return objectMapper.writeValueAsString(blocks);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Block> convertJsonToBlocks(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Block>>() {});
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
