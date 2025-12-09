package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.entity.BlogPost;
import com.deniz.bloomlishbackend.entity.Comment;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.BlogPostMapper;
import com.deniz.bloomlishbackend.repository.BlogPostRepository;
import com.deniz.bloomlishbackend.repository.CommentRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final BlogPostRepository blogPostRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private  final BlogPostMapper blogPostMapper;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + email));
    }

    public BlogPostDto create(String content,String email) {
        User user=getUserByEmail(email);
        BlogPost blogPost= BlogPost.builder()
                .user(user)
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
    public void delete(Long id,String email) {
        BlogPost blogPost=blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));
        if(!blogPost.getUser().getEmail().equals(email)){
            throw new AccessDeniedException("Bu postu silme yetkiniz yoktur!");
        }
        blogPostRepository.delete(blogPost);
    }
    public BlogPostDto like(Long id,String email) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));
        User currentUser = getUserByEmail(email);
        if(blogPost.getUser().getUserID().equals(currentUser.getUserID())){
            throw new  RuntimeException("Kendi postunu beğenemezsin!");
        }
        Set<User> likedUsers = blogPost.getLikedUsers();
        if (likedUsers.contains(currentUser)) {
            // zaten beğenmiş → geri çek
           likedUsers.remove(currentUser);
            blogPost.setLikes(blogPost.getLikes() - 1);
        } else {
            // ilk kez beğeniyor
           likedUsers.add(currentUser);
            blogPost.setLikes(blogPost.getLikes() + 1);
        }

        return blogPostMapper.toDto(  blogPostRepository.save(blogPost));

    }
    public CommentDto createComment(Long postId,CommentDto commentDto,String email) {
        BlogPost post=blogPostRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));
        User user = getUserByEmail(email);
        Comment comment=Comment.builder()
                .text(commentDto.getText())
                .user(user)
                .blogPost(post)
                .build();

        Comment saved=commentRepository.save(comment);
        return blogPostMapper.commentToDto(saved);

    }
    public BlogPostDto update(Long id,BlogPostDto blogPostDto,String email) {
        BlogPost post= blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Post bulunamadı."));
        if(!post.getUser().getEmail().equals(email)){
            throw new AccessDeniedException("Bu postu değiştirme yetkiniz yoktur!");
        }
      post.setContent(blogPostDto.getContent());
        post.setUpdatedAt(LocalDateTime.now());
      return  blogPostMapper.toDto(blogPostRepository.save(post));
    }

}
