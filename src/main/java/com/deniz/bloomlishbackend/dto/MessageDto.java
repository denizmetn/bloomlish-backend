package com.deniz.bloomlishbackend.dto;

import com.deniz.bloomlishbackend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long id;
    private User sender;
    private User receiver;
    private String content;
    private LocalDateTime sentAt;
}
