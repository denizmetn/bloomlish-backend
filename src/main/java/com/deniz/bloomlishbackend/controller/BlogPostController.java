package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.service.BlogPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class BlogPostController {
    private final BlogPostService blogPostService;
    @PostMapping("/create")
    public ResponseEntity<BlogPostDto>  create(
            @RequestBody BlogPostDto blogPostDto,
            @AuthenticationPrincipal UserDetails userDetails ) {
        String username=userDetails.getUsername();
        System.out.println("Login olan kullanıcı: " + username);
        BlogPostDto savedPost = blogPostService.create(blogPostDto.getContent(), username);
        System.out.println("DTO username: " + savedPost.getUsername());
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping("/get-all")
    public ResponseEntity<Page<BlogPostDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.ok(blogPostService.getAll(page,size));
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<BlogPostDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(blogPostService.getById(id));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void>  delete(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails ) {
        String username=userDetails.getUsername();
        blogPostService.delete(id,username);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/like")
    public ResponseEntity<BlogPostDto> like(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails)
     {
         String email=userDetails.getUsername();
        return ResponseEntity.ok( blogPostService.like(id,email));
    }

    @PostMapping("/{postID}/comment")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postID,
            @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username=userDetails.getUsername();
        return ResponseEntity.ok(blogPostService.createComment(postID,commentDto,username));

    }
    @PutMapping("/update/{id}")
    public ResponseEntity<BlogPostDto> update(
            @PathVariable Long id,
            @RequestBody BlogPostDto blogPostDto,
            @AuthenticationPrincipal UserDetails userDetails ){
        String username=userDetails.getUsername();
        return ResponseEntity.ok(blogPostService.update(id,blogPostDto,username));

    }


}
