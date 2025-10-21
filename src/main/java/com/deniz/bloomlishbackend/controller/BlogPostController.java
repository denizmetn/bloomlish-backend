package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.service.BlogPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class BlogPostController {
    private final BlogPostService blogPostService;
    @PostMapping("create")
    public ResponseEntity<BlogPostDto>  create(
            @RequestBody BlogPostDto blogPostDto,
            Principal principal) {
        String username=principal.getName();
        System.out.println("Login olan kullanıcı: " + username);
        BlogPostDto savedPost = blogPostService.create(blogPostDto.getContent(), username);
        System.out.println("DTO username: " + savedPost.getUsername());
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<BlogPostDto>> getAll() {
        return ResponseEntity.ok(blogPostService.getAll());
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<BlogPostDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(blogPostService.getById(id));
    }
    @PatchMapping("/{id}/like")
    public ResponseEntity<Integer> like(
            @PathVariable Long id,
           Principal principal)
     {
         String username=principal.getName();
        return ResponseEntity.ok( blogPostService.like(id,username));
    }

    @PostMapping("{postID}/comment")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postID,
            @RequestBody CommentDto commentDto,
      @RequestHeader("Authorization")String authHeader) {
        if(authHeader== null|| !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token=authHeader.substring(7);
        return ResponseEntity.ok(blogPostService.createComment(postID,commentDto,token));

    }

}
