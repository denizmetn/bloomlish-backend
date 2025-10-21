package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.entity.BlogPost;
import com.deniz.bloomlishbackend.entity.Comment;
import com.deniz.bloomlishbackend.mapper.BlogPostMapper;
import com.deniz.bloomlishbackend.repository.BlogPostRepository;
import com.deniz.bloomlishbackend.repository.CommentRepository;
import com.deniz.bloomlishbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final BlogPostRepository blogPostRepository;
    private final CommentRepository commentRepository;
    private  final BlogPostMapper blogPostMapper;
    private final JwtService jwtService;

    public BlogPostDto create(String content,String username) {
        BlogPost blogPost= BlogPost.builder()
                .username(username)
                .content(content)
                .likes(0)
                .createdAt(LocalDateTime.now())
                .comments(new ArrayList<>())
                .build();
        BlogPost saved=blogPostRepository.save(blogPost);
        return blogPostMapper.toDto(saved);
    }

    public List<BlogPostDto> getAll(){
        return blogPostMapper.toDtoList(blogPostRepository.findAll());
    }
    public BlogPostDto getById(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Post bulunmadı."));
        return blogPostMapper.toDto(blogPost);
    }
    public int like(Long id,String username) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));
        if(blogPost.getLikedUsers().contains(username)){
            throw new RuntimeException("Bu kullanıcı zaten bu postu beğendi!");
        }
        blogPost.getLikedUsers().add(username);
        blogPost.setLikes(blogPost.getLikes() + 1);
        blogPostRepository.save(blogPost);
        return blogPost.getLikes();
    }
    public CommentDto createComment(Long postId,CommentDto commentDto,String token) {
        BlogPost post=blogPostRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));
        String userEmail= jwtService.extractUsername(token);
        Comment comment=blogPostMapper.dtoToComment(commentDto);
        comment.setUsername(userEmail);
        comment.setBlogPost(post);
        Comment saved=commentRepository.save(comment);
        return blogPostMapper.commentToDto(saved);

    }

}
