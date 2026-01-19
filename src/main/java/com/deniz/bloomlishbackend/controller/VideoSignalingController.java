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

        messagingTemplate.convertAndSend("/topic/video/" + roomId, message);
    }

}
