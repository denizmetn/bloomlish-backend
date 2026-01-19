package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.MessageDto;
import com.deniz.bloomlishbackend.entity.Message;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.MessageMapper;
import com.deniz.bloomlishbackend.repository.MessageRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    private final UserRepository userRepository;

    //mesaj gönder
    @Transactional
    public MessageDto saveMessage(MessageDto messageDto) {
        if (messageDto.getSentAt() == null) {
            messageDto.setSentAt(LocalDateTime.now());
        }
        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Gönderen kullanıcı bulunamadı"));
        User receiver= userRepository.findById(messageDto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Alıcı kullanıcı bulunamadı"));

        if (!sender.getRole().contains("STUDENT") || !receiver.getRole().contains("STUDENT")) {
            throw new RuntimeException("Mesajlaşma sadece öğrenciler arasında yapılabilir.");
        }


        Message messageNote=Message.builder()
                .content(messageDto.getContent())
                .sender(sender)
                .receiver(receiver)
                .build();

        Message saved = messageRepository.save(messageNote);
        return messageMapper.messageToDto(saved);
    }
    //sohbet geçmişini getir
    @Transactional(readOnly = true)
    public List<MessageDto> getConversationHistory(Long user1Id, Long user2Id) {
        if (user1Id.equals(user2Id)) {
            return List.of();
        }
       List<Message> conversation= messageRepository.findConversation(user1Id, user2Id);
        return messageMapper.messageToDtoList(conversation);
    }
}

