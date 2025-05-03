package com.real.backend.domain.chatbot;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.FastAPIService;
import com.real.backend.domain.chatbot.dto.ChatbotRequestDTO;
import com.real.backend.domain.chatbot.dto.ChatbotResponseDTO;
import com.real.backend.response.DataResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatbotController {

    private final FastAPIService fastAPIService;

    @PostMapping("/v1/chatbots")
    public DataResponse<?> makeQuestion(@RequestBody ChatbotRequestDTO chatbotRequestDTO) throws
        JsonProcessingException {
        ChatbotResponseDTO chatbotResponseDTO = fastAPIService.makeQuestion(chatbotRequestDTO);

        return DataResponse.of(chatbotResponseDTO);
    }
}
