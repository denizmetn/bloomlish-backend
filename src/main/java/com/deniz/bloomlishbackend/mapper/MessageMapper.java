package com.deniz.bloomlishbackend.mapper;

import com.deniz.bloomlishbackend.dto.MessageDto;
import com.deniz.bloomlishbackend.entity.Message;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel= "spring")
public interface MessageMapper {

    MessageDto messageToDto(Message message);
    Message DtoToMessage(MessageDto messageDto);

    List<MessageDto> messageToDtoList(List<Message> message);
    List<Message> DtoToMessageList(List<MessageDto> messageDto);

}
