package com.deniz.bloomlishbackend.dto;

import lombok.Data;

@Data
public class IceCandidate {
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
    private String usernameFragment;
}

