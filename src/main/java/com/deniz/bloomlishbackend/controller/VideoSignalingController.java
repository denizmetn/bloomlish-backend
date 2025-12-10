package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.SignalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class VideoSignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/video/{roomId}")
    public void handleSignal(@DestinationVariable String roomId, SignalMessage message) {

        // 1-1 oda için en basit model:
        // Odaya gelen HER sinyali /topic/video/{roomId} üzerinden herkese gönder.
        // Frontend zaten senderId ile kendi mesajını ayıklıyor.
        messagingTemplate.convertAndSend("/topic/video/" + roomId, message);
    }

}
