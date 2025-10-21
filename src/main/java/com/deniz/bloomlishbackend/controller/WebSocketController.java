package com.deniz.bloomlishbackend.controller;


import com.deniz.bloomlishbackend.dto.MessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;


    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/send")
    public void sendMessage(@Payload MessageDto messageDto){
        messagingTemplate.convertAndSendToUser(
                String.valueOf(messageDto.getReceiverId()),
                "/queue/messages",
                messageDto

        );
}


}
