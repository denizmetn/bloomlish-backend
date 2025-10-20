package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.MessageDto;
import com.deniz.bloomlishbackend.entity.Message;
import com.deniz.bloomlishbackend.mapper.MessageMapper;
import com.deniz.bloomlishbackend.repository.MessageRepository;
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
    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(Long user1Id, Long user2Id) {
        List<Message> entity = messageRepository.findConversation(user1Id, user2Id);
        return messageMapper.messageToDtoList(entity);
    }

    @Transactional()
    public MessageDto saveMessage(MessageDto messageDto) {
        if (messageDto.getSentAt() == null) {
            messageDto.setSentAt(LocalDateTime.now());
        }
        Message message =messageMapper.DtoToMessage(messageDto);
        Message saved = messageRepository.save(message);
        return messageMapper.messageToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getConversationHistory(Long user1Id, Long user2Id) {
        if (user1Id.equals(user2Id)) {
            // Aynı kullanıcıyla konuşma yok, boş liste dön
            return List.of();
        }
       List<Message> conversation= messageRepository.findConversation(user1Id, user2Id);
        return messageMapper.messageToDtoList(conversation);

    }

    @Transactional()
    public List<MessageDto> createConversation(){

    }
}

