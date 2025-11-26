package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.LessonDto;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.service.LessonService;
import com.deniz.bloomlishbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor

public class LessonController {
    private final LessonService lessonService;

    //Yeni ders oluşturma(eğitmen)
    @PostMapping("/create")
    public ResponseEntity<LessonDto> createLesson(
            @RequestBody LessonDto lessonDto,
            @AuthenticationPrincipal User instructor
    ) {
        LessonDto savedLesson = lessonService.createLesson(lessonDto, instructor);
        return ResponseEntity.ok(savedLesson);
    }

    //tüm dersler listeleme
    @GetMapping
    public ResponseEntity<List<LessonDto>> getAllLessons(){
        return ResponseEntity.ok(lessonService.getAllLessons());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<LessonDto>> filterLessons(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String instructor,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice
    ){

        List<LessonDto> filtered = lessonService.filterLessons(name,instructor,level,category,startDate,endDate,minPrice,maxPrice);
        return ResponseEntity.ok(filtered);
    }

    //tek dersin detayları gibi
    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> getLesson (@PathVariable Long id) {
        return lessonService.getLessonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
