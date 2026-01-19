package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.entity.BlogPost;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.BlogPostMapper;
import com.deniz.bloomlishbackend.repository.BlogPostRepository;
import com.deniz.bloomlishbackend.repository.CommentRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

    @Mock BlogPostRepository blogPostRepository;
    @Mock CommentRepository commentRepository;
    @Mock UserRepository userRepository;
    @Mock BlogPostMapper blogPostMapper;

    @InjectMocks BlogPostService blogPostService;

    @Test
    void delete_notOwner_shouldThrowAccessDenied() {
        Long postId = 1L;
        String attackerEmail = "attacker@mail.com";

        User owner = User.builder().userID(10L).email("owner@mail.com").build();
        BlogPost post = BlogPost.builder().id(postId).user(owner).build();

        when(blogPostRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(AccessDeniedException.class, () -> blogPostService.delete(postId, attackerEmail));

        verify(blogPostRepository, never()).delete(any());
    }

    @Test
    void like_selfLike_shouldThrow409() {
        // given
        Long postId = 1L;
        String email = "same@mail.com";

        User u = User.builder().userID(10L).email(email).build();
        BlogPost post = BlogPost.builder().id(postId).user(u).build();

        when(blogPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));

        // when
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> blogPostService.like(postId, email));

        // then
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(blogPostRepository, never()).save(any());
    }

    @Test
    void like_toggleLike_shouldAddThenRemove() {
        // given
        Long postId = 1L;

        User owner = User.builder().userID(1L).email("owner@mail.com").build();
        BlogPost post = BlogPost.builder().id(postId).user(owner).likes(0).build();

        User liker = User.builder().userID(2L).email("liker@mail.com").build();

        when(blogPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail(liker.getEmail())).thenReturn(Optional.of(liker));

        // save geri dönüşü önemli değil, mapper da mock
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(inv -> inv.getArgument(0));
        when(blogPostMapper.toDto(any(BlogPost.class))).thenReturn(mock(BlogPostDto.class));

        // when - 1: like eklenir
        blogPostService.like(postId, liker.getEmail());

        // then - 1
        assertEquals(1, post.getLikedUsers().size());
        assertEquals(1, post.getLikes());

        // when - 2: like kaldırılır (toggle)
        blogPostService.like(postId, liker.getEmail());

        // then - 2
        assertEquals(0, post.getLikedUsers().size());
        assertEquals(0, post.getLikes());
    }
}
