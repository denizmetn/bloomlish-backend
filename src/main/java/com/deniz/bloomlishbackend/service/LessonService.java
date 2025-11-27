package com.deniz.bloomlishbackend.service;


import com.deniz.bloomlishbackend.dto.LessonDto;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.LessonMapper;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

    //yeni ders oluşturma
    public LessonDto createLesson(LessonDto lessonDto, List<MultipartFile> files, User instructor)
    {
        Lesson lesson = lessonMapper.toEntity(lessonDto);

        lesson.setInstructor(instructor);
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        String filePath = "uploads/" + fileName;

                        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
                        java.nio.file.Files.createDirectories(uploadPath);

                        java.nio.file.Path destination = uploadPath.resolve(fileName);
                        java.nio.file.Files.write(destination, file.getBytes());

                        lesson.getResourcePaths().add(filePath);

                    } catch (IOException e) {
                        throw new RuntimeException("Dosya kaydedilemedi: " + e.getMessage());
                    }
                }
            }
        }
        Lesson saved = lessonRepository.save(lesson);
        return lessonMapper.toDto(saved);
    }

    //tüm dersleri listelem
    public List<LessonDto> getAllLessons(){
        List<Lesson> lessons = lessonRepository.findAll();
        return lessonMapper.toDtoList(lessons);
    }

    //dersleri filtreleme
    public List<LessonDto> filterLessons(
            String name,
            String instructor,
            String level,
            String category,
            String startDate,
            String endDate,
            String minPrice,
            String maxPrice
    ){
        LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
        LocalDate endD = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;

        Double minP = (minPrice != null && !minPrice.isBlank()) ? Double.parseDouble(minPrice) : null;
        Double maxP = (maxPrice != null && !maxPrice.isBlank()) ? Double.parseDouble(maxPrice) : null;

        return lessonRepository.findAll().stream()

                .filter(l -> name == null || name.isBlank() ||
                        l.getName().toLowerCase().contains(name.toLowerCase()))

                .filter(l -> instructor == null || instructor.isBlank() ||
                        l.getInstructor().getUsername().equalsIgnoreCase(instructor))


                .filter(l -> level == null || level.isBlank() ||
                        l.getLevel().equalsIgnoreCase(level))

                .filter(l -> category == null || category.isBlank() ||
                        l.getCategory().equalsIgnoreCase(category))

                .filter(l -> start == null || !l.getDate().isBefore(start))
                .filter(l -> endD == null || !l.getDate().isAfter(endD))

                .filter(l -> minP == null || l.getPrice() >= minP)
                .filter(l -> maxP == null || l.getPrice() <= maxP)

                .map(lessonMapper::toDto)
                .collect(Collectors.toList());
    }
    //tek ders detay
    public Optional <LessonDto> getLessonById(Long id){
        return lessonRepository.findById(id)
                .map(lessonMapper:: toDto);
    }
}
