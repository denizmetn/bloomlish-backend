package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.entity.Comment;
import com.deniz.bloomlishbackend.mapper.BlogPostMapper;
import com.deniz.bloomlishbackend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BlogPostMapper blogPostMapper;

    public Page<CommentDto> getAll(int page,int size){
        Pageable pageable = PageRequest.of(page,size);
        return commentRepository.findAll(pageable)
                .map(blogPostMapper::commentToDto);
    }

    public CommentDto getById(Long id){
       Comment comment= commentRepository.findById(id)
               .orElseThrow(()-> new RuntimeException("Yorum bulunamadı."));
       return blogPostMapper.commentToDto(comment);
    }
    public Page<CommentDto> getCommentByPost(Long postId ,int page,int size){
        Pageable pageable = PageRequest.of(page,size, Sort.by("id").descending());
        Page<Comment> comments = commentRepository.findByBlogPostId(postId,pageable);
        return comments.map(blogPostMapper::commentToDto);
    }

    public void delete(Long id,String username){
        Comment comment=commentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Yorum bulunamadı."));
        if(!comment.getUsername().equals(username)){
            throw new AccessDeniedException("Bu yorumu silme yetkiniz yoktur!");
        }
        commentRepository.delete(comment);
    }
    public CommentDto update(Long id,CommentDto commentDto,String username){
        Comment comment=commentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Yorum bulunamadı."));
        if(!comment.getUsername().equals(username)){
            throw new AccessDeniedException("Bu yorumu silme yetkiniz yoktur!");
        }
        comment.setText(commentDto.getText());
        comment.setUpdatedAt(LocalDateTime.now());
        return blogPostMapper.commentToDto(commentRepository.save(comment));
    }

}
