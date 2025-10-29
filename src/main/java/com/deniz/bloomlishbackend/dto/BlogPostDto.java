package com.deniz.bloomlishbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogPostDto {
    private Long id;
    private String username;
    private String content;
    private int likes;
    private List<CommentDto> comments;
    private LocalDateTime createdAt;
    private Set<String> likedUsers;
}
