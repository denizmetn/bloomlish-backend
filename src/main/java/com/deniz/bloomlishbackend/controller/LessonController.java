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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor

public class LessonController {
    private final LessonService lessonService;

    //Yeni ders oluşturma(eğitmen)
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LessonDto> createLesson(
            @RequestPart("dto") LessonDto lessonDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal User instructor
    )
    {
        LessonDto savedLesson = lessonService.createLesson(lessonDto, files, instructor);
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

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/my-lessons")
    public List<LessonDto> getMyLessons(@AuthenticationPrincipal User instructor) {
        return lessonService.getLessonsByInstructor(instructor);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal User instructor
    ){
        lessonService.deleteLesson(id, instructor);

        return ResponseEntity.ok("Ders başarıyla silindi");
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<LessonDto> updateLesson(
            @PathVariable Long id,
            @RequestBody LessonDto dto,
            @AuthenticationPrincipal User instructor
    ) {
        LessonDto updated = lessonService.updateLesson(id, dto, instructor);
        return ResponseEntity.ok(updated);
    }

}
