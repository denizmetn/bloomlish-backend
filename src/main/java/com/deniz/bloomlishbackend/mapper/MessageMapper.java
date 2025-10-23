package com.deniz.bloomlishbackend.mapper;

import com.deniz.bloomlishbackend.dto.MessageDto;
import com.deniz.bloomlishbackend.entity.Message;
import com.deniz.bloomlishbackend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel= "spring")
public interface MessageMapper {
    @Mapping(source = "sender.userID", target = "senderId")
    @Mapping(source = "receiver.userID", target = "receiverId")
    MessageDto messageToDto(Message message);

    @Mapping(target = "sender", expression = "java(new User(messageDto.getSenderId()))")
    @Mapping(target = "receiver", expression = "java(new User(messageDto.getReceiverId()))")
    Message dtoToMessage(MessageDto messageDto);

    List<MessageDto> messageToDtoList(List<Message> message);
    List<Message> dtoToMessageList(List<MessageDto> messageDto);

}
