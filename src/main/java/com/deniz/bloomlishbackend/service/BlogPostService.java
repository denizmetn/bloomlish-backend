package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.entity.BlogPost;
import com.deniz.bloomlishbackend.mapper.BlogPostMapper;
import com.deniz.bloomlishbackend.repository.BlogPostRepository;
import com.deniz.bloomlishbackend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final BlogPostRepository blogPostRepository;
    private final CommentRepository commentRepository;
    private  final BlogPostMapper blogPostMapper;

    public BlogPostDto create(BlogPostDto blogPostDto) {
        BlogPost blogPost= blogPostMapper.toEntity(blogPostDto);
        BlogPost saved=blogPostRepository.save(blogPost);
        return blogPostMapper.toDto(saved);
    }
    public List<BlogPostDto> getAll(){
        return blogPostMapper.toDtoList(blogPostRepository.findAll());
    }
    public BlogPostDto getById(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id).orElse(null);
        return blogPostMapper.toDto(blogPost);
    }
    public int like(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id).orElse(null);
        blogPost.setLikes(blogPost.getLikes() + 1);
        blogPostRepository.save(blogPost);
        return blogPost.getLikes();
    }

}
