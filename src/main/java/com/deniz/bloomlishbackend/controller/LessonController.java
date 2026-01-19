package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.LessonDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{id}/upload-resource")
    public ResponseEntity<String> uploadResource(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User instructor
    ) {
        String savedPath = lessonService.uploadResource(id, file, instructor);
        return ResponseEntity.ok(savedPath);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{id}/resource/{fileName}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long id,
            @PathVariable String fileName,
            @AuthenticationPrincipal User instructor
    ) {
        lessonService.deleteResource(id, fileName, instructor);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/join-check/{lessonId}")
    public ResponseEntity<String> canJoinLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User student
    ) {
        return ResponseEntity.ok(lessonService.canJoin(lessonId, student));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor-join-check/{lessonId}")
    public ResponseEntity<String> canInstructorJoin(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User instructor
    ) {
        return ResponseEntity.ok(lessonService.canInstructorJoin(lessonId, instructor));
    }

}
