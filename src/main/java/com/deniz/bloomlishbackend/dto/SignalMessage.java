package com.deniz.bloomlishbackend.dto;

import lombok.Data;
@Data
public class SignalMessage {
    private String type;
    private String sdp;
    private IceCandidate candidate;
    private String senderId;
    private String targetUserId;
}