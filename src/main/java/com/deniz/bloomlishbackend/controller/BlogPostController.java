package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.repository.BlogPostRepository;
import com.deniz.bloomlishbackend.repository.CommentRepository;
import com.deniz.bloomlishbackend.service.BlogPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/blog")
@RequiredArgsConstructor
public class BlogPostController {
    private final BlogPostService blogPostService;


    @PostMapping("create")
    public ResponseEntity<BlogPostDto>  create(@RequestBody BlogPostDto blogPostDto) {
        return ResponseEntity.ok(blogPostService.create(blogPostDto));
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
    public ResponseEntity<Integer> like(@PathVariable Long id) {
        return ResponseEntity.ok(blogPostService.like(id));
    }

}
