package com.deniz.bloomlishbackend.mapper;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.dto.DailyNoteDto;
import com.deniz.bloomlishbackend.entity.BlogPost;
import com.deniz.bloomlishbackend.entity.Comment;
import com.deniz.bloomlishbackend.entity.DailyNote;
import com.deniz.bloomlishbackend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BlogPostMapper {

    @Mapping(target = "username",
            expression = "java(comment.getUser().getEmail())")
    CommentDto commentToDto(Comment comment);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "blogPost", ignore = true)
    Comment dtoToComment(CommentDto dto);

    @Mapping(target = "username",
            expression = "java(blogPost.getUser().getEmail())")
    @Mapping(target = "likedUsers",
            expression = "java(mapLikedUsers(blogPost.getLikedUsers()))")
    BlogPostDto toDto(BlogPost blogPost);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "likedUsers", ignore = true)
    @Mapping(target = "comments", ignore = true)
    BlogPost toEntity(BlogPostDto dto);

    DailyNoteDto dailyToDto(DailyNote dto);

    List<CommentDto> commentToDtoList(List<Comment> comments);

    List<Comment> dtoToCommentList(List<CommentDto> dtos);
    default Set<String> mapLikedUsers(Set<User> likedUsers) {
        if (likedUsers == null || likedUsers.isEmpty()) {
            return Set.of();
        }
        return likedUsers.stream()
                .map(User::getEmail)   // istersen User::getUsername yap
                .collect(Collectors.toSet());
    }
}
