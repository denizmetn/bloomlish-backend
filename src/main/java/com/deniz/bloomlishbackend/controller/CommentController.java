package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/get-all")
    public ResponseEntity<Page<CommentDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
     return ResponseEntity.ok(commentService.getAll(size,page));
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<CommentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getById(id));
    }
    @GetMapping("/get/post/{postId}")
    public ResponseEntity<Page<CommentDto>> getCommentByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.getCommentByPost(postId,page,size));

    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userdetails){
        String username= userdetails.getUsername();
        commentService.delete(id,username);
        return ResponseEntity.noContent().build();

    }
    @PutMapping("/update/{id}")
    public ResponseEntity<CommentDto> update(
            @PathVariable Long id,
            @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal UserDetails userdetails){
        String username= userdetails.getUsername();
        return ResponseEntity.ok(commentService.update(id,commentDto,username));
    }
}
