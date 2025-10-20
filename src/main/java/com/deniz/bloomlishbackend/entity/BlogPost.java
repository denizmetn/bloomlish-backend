package com.deniz.bloomlishbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String content;

    private int likes;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "blog_post_id")
    private List<Comment> comments = new ArrayList<>();

    private LocalDateTime createdAt= LocalDateTime.now();
}
