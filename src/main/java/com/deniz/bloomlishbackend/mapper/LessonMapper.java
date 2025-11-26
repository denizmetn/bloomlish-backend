package com.deniz.bloomlishbackend.mapper;


import com.deniz.bloomlishbackend.dto.LessonDto;
import com.deniz.bloomlishbackend.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(source = "instructor.username", target="instructorName")
    LessonDto toDto(Lesson lesson);

    Lesson toEntity(LessonDto dto);

    List<LessonDto> toDtoList(List<Lesson> lessons);

    List<Lesson> toEntityList(List<LessonDto> dtos);
}
