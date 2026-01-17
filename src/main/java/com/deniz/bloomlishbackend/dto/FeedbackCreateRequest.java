package com.deniz.bloomlishbackend.dto;

public record FeedbackCreateRequest(Long lessonId, Integer rating, String comment) {}
