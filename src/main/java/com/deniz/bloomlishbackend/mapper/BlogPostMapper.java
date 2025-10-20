package com.deniz.bloomlishbackend.mapper;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.entity.BlogPost;
import com.deniz.bloomlishbackend.entity.Comment;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BlogPostMapper {

    CommentDto commentToDto(Comment comment);
    Comment dtoToComment(CommentDto dto);

    BlogPostDto toDto(BlogPost blogPost);
    BlogPost toEntity(BlogPostDto dto);

    List<BlogPostDto> toDtoList(List<BlogPost> blogPosts);
    List<BlogPost> toEntityList(List<BlogPostDto> dtos);

    List<CommentDto> commentToDtoList(List<Comment> comments);
    List<Comment> dtoToCommentList(List<CommentDto> dtos);
}
