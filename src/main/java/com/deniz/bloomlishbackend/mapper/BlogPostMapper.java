package com.deniz.bloomlishbackend.mapper;

import com.deniz.bloomlishbackend.dto.BlogPostDto;
import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.dto.DailyNoteDto;
import com.deniz.bloomlishbackend.entity.BlogPost;
import com.deniz.bloomlishbackend.entity.Comment;
import com.deniz.bloomlishbackend.entity.DailyNote;
import com.deniz.bloomlishbackend.entity.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface  BlogPostMapper {

    CommentDto commentToDto(Comment comment);
    @Mapping(target = "createdAt", ignore = true)
    Comment dtoToComment(CommentDto dto);

    @Mapping(target = "likedUsers", source = "likedUsers")
    BlogPostDto toDto(BlogPost blogPost);
    @Mapping(target = "likedUsers", source = "likedUsers")
    BlogPost toEntity(BlogPostDto dto);

    DailyNoteDto dailyToDto(DailyNote dto);

    DailyNote dailyToEntity(DailyNoteDto dto);

    List<BlogPostDto> toDtoList(List<BlogPost> blogPosts);

    List<BlogPost> toEntityList(List<BlogPostDto> dtos);

    List<CommentDto> commentToDtoList(List<Comment> comments);
    List<Comment> dtoToCommentList(List<CommentDto> dtos);

}
