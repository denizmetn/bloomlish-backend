package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.MessageDto;
import com.deniz.bloomlishbackend.entity.Message;
import com.deniz.bloomlishbackend.repository.MessageRepository;
import com.deniz.bloomlishbackend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageHistoryController {
    private final MessageService messageService;

    @GetMapping("/get/{user1id}/{user2id}")
    public ResponseEntity<List<MessageDto>>getConversation(
            @PathVariable Long user1id,
            @PathVariable Long user2id){
        List<MessageDto> messages = messageService.getConversationHistory(user1id,user2id);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/create/{user1id}/{user2id}")
    public  ResponseEntity<List<MessageDto>>createConversation(
            @PathVariable Long user1id,
            @PathVariable Long user2id){
        List<MessageDto> messages = messageService.createConversation(user1id,user2id);
        return ResponseEntity.ok(messages);
    }
}
