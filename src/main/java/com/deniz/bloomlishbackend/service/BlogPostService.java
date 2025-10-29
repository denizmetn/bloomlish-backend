package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.entity.BlogPost;
import com.deniz.bloomlishbackend.entity.Comment;
import com.deniz.bloomlishbackend.mapper.BlogPostMapper;
import com.deniz.bloomlishbackend.repository.BlogPostRepository;
import com.deniz.bloomlishbackend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final BlogPostRepository blogPostRepository;
    private final CommentRepository commentRepository;
    private  final BlogPostMapper blogPostMapper;


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

    public Page<BlogPostDto> getAll(int page, int size){
        Pageable pageable= PageRequest.of(page,size, Sort.by("id").descending());
        return blogPostRepository.findAll(pageable)
                .map(blogPostMapper::toDto);
    }
    public BlogPostDto getById(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Post bulunmadı."));
        return blogPostMapper.toDto(blogPost);
    }
    public void delete(Long id,String username) {
        BlogPost blogPost=blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));
        if(!blogPost.getUsername().equals(username)){
            throw new AccessDeniedException("Bu postu silme yetkiniz yoktur!");
        }
        blogPostRepository.delete(blogPost);
    }
    public BlogPostDto like(Long id,String email) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));

        if(blogPost.getUsername().equals(email)){
            throw new  RuntimeException("Kendi postunu beğenemezsin!");
        }
        if (blogPost.getLikedUsers().contains(email)) {
            // zaten beğenmiş → geri çek
            blogPost.getLikedUsers().remove(email);
            blogPost.setLikes(blogPost.getLikes() - 1);
        } else {
            // ilk kez beğeniyor
            blogPost.getLikedUsers().add(email);
            blogPost.setLikes(blogPost.getLikes() + 1);
        }

        return blogPostMapper.toDto(  blogPostRepository.save(blogPost));

    }
    public CommentDto createComment(Long postId,CommentDto commentDto,String username) {
        BlogPost post=blogPostRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));
        Comment comment=Comment.builder()
                .text(commentDto.getText())
                .username(username)
                .blogPost(post)
                .build();

        Comment saved=commentRepository.save(comment);
        return blogPostMapper.commentToDto(saved);

    }

}
