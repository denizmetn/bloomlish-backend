package com.deniz.bloomlishbackend.mapper;

import com.deniz.bloomlishbackend.dto.QuestionDto;
import com.deniz.bloomlishbackend.dto.QuizDto;
import com.deniz.bloomlishbackend.dto.ResultsDto;
import com.deniz.bloomlishbackend.entity.Question;
import com.deniz.bloomlishbackend.entity.Quiz;
import com.deniz.bloomlishbackend.entity.Results;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel= "spring")
public interface QuestionMapper {
    // Entity → DTO
    @Mapping(source = "quiz.id", target = "quizId") // quiz nesnesindeki id → dto’daki quizId
    QuestionDto toDto(Question question);

    // DTO → Entity
    @Mapping(source = "quizId", target = "quiz.id")
    Question toEntity(QuestionDto dto);

    Quiz toEntityQuiz(QuizDto dto);
    QuizDto toDtoQuiz(Quiz quiz);

    Results toEntityResults(ResultsDto resultsDto);
    ResultsDto toDtoResults(Results results);
    List<ResultsDto> toDtoListResults(List<Results> results);


    List<QuestionDto> toDtoList(List<Question> questions);
    List<Question> toEntityList(List<QuestionDto> dtos);

}
