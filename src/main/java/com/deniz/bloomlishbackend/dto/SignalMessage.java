package com.deniz.bloomlishbackend.dto;

import lombok.Data;
@Data
public class SignalMessage {
    private String type;            // "offer", "answer", "candidate"
    private String sdp;             // OFFER/ANSWER ise sadece sdp text
    private IceCandidate candidate; // candidate object
    private String senderId;
    private String targetUserId;
}