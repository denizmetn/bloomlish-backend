package com.deniz.bloomlishbackend.mapper;

import com.deniz.bloomlishbackend.dto.QuestionDto;
import com.deniz.bloomlishbackend.dto.QuizDto;
import com.deniz.bloomlishbackend.dto.QuizResultsDto;
import com.deniz.bloomlishbackend.entity.Question;
import com.deniz.bloomlishbackend.entity.QuestionOption;
import com.deniz.bloomlishbackend.entity.Quiz;
import com.deniz.bloomlishbackend.entity.Results;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel= "spring")
public interface QuestionMapper {
    // Entity → DTO
    @Mapping(source = "quiz.id", target = "quizId")
    @Mapping(target = "limit", ignore = true)// quiz nesnesindeki id → dto’daki quizId
    QuestionDto toDto(Question question);

    // DTO → Entity
    @Mapping(source = "quizId", target = "quiz.id")
    @Mapping(target = "questionCount", ignore = true)
    Question toEntity(QuestionDto dto);

    Quiz toEntityQuiz(QuizDto dto);
    QuizDto toDtoQuiz(Quiz quiz);

    Results toEntityResults(QuizResultsDto quizResultsDto);
    QuizResultsDto toDtoResults(Results results);
    List<QuizResultsDto> toDtoListResults(List<Results> results);


    List<QuestionDto> toDtoList(List<Question> questions);
    List<Question> toEntityList(List<QuestionDto> dtos);

    // List<QuestionOption>  -> List<String> (Question.options -> QuestionDto.options)
    default String map(QuestionOption option) {
        if (option == null) return null;
        return option.getOptionText();   // >>> STRING alanımız bu
    }

    // List<String> -> List<QuestionOption> (QuestionDto.options -> Question.options)
    default QuestionOption map(String optionText) {
        if (optionText == null) {
            return null;
        }
        return QuestionOption.builder()
                .optionText(optionText)
                .correct(false)
                .build();
    }
}
