package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.MessageDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public WebSocketController(
            SimpMessagingTemplate messagingTemplate,
            UserRepository userRepository
    ) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/send")
    public void sendMessage(@Payload MessageDto messageDto){
        User sender = userRepository.findById(messageDto.getSenderId())
                .orElse(null);

        if (sender == null) return;

        if (!"STUDENT".equals(sender.getRole())) {
            System.out.println("WS BLOKLANDI: ÖĞRENCİ DIŞI KULLANICI MESAJ GÖNDERMEYE ÇALIŞTI.");
            return;
        }

        messagingTemplate.convertAndSendToUser(
                String.valueOf(messageDto.getReceiverId()),
                "/queue/messages",
                messageDto
        );
    }
}

