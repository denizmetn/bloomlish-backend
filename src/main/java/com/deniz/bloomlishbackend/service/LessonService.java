package com.deniz.bloomlishbackend.service;


import com.deniz.bloomlishbackend.dto.LessonDto;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.LessonMapper;
import com.deniz.bloomlishbackend.repository.EnrollmentRepository;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final EnrollmentRepository enrollmentRepository;

    //yeni ders oluşturma
    public LessonDto createLesson(LessonDto lessonDto, List<MultipartFile> files, User instructor) {
        Lesson lesson = lessonMapper.toEntity(lessonDto);
        lesson.setInstructor(instructor);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {

                    // Kayıt yolu: uploads/resources/
                    java.nio.file.Path folderPath = java.nio.file.Paths.get("uploads", "resources");

                    String originalFilename = file.getOriginalFilename();
                    String extension = "";
                    int i = originalFilename.lastIndexOf('.');
                    if (i > 0) {
                        extension = originalFilename.substring(i);
                    }
                    String safeFileName = UUID.randomUUID().toString() + extension;
                    java.nio.file.Path filePath = folderPath.resolve(safeFileName);

                    try {
                        java.nio.file.Files.createDirectories(folderPath);

                        java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                        lesson.getResourcePaths().add(safeFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Dosya kaydedilemedi: " + e.getMessage());
                    }
                }
            }
        }
        Lesson saved = lessonRepository.save(lesson);
        return lessonMapper.toDto(saved);
    }

    public List<LessonDto> getAllLessons() {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Lesson> lessons = lessonRepository.findAll();

        List<Lesson> filtered = lessons.stream()
                .filter(lesson -> {


                    if (enrollmentRepository.existsByLesson(lesson)) {
                        return false;
                    }

                    LocalDate lessonDate = lesson.getDate();
                    LocalTime startTime = lesson.getStartTime();

                    if (lessonDate.isBefore(today)) {
                        return false;
                    }

                    if (lessonDate.isEqual(today) && startTime.isBefore(now)) {
                        return false;
                    }

                    return true;
                })
                .toList();

        return lessonMapper.toDtoList(filtered);
    }


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

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        return lessonRepository.findAll().stream()

                .filter(l -> !enrollmentRepository.existsByLesson(l))

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

                .filter(l -> {
                    LocalDate lessonDate = l.getDate();
                    LocalTime startTime = l.getStartTime();

                    if (lessonDate.isBefore(today)) return false;
                    if (lessonDate.isEqual(today) && startTime.isBefore(now)) return false;

                    return true;
                })

                .map(lessonMapper::toDto)
                .collect(Collectors.toList());
    }
    //tek ders detay
    public Optional<LessonDto> getLessonById(Long id){
        Optional<Lesson> opt = lessonRepository.findById(id);
        if (opt.isEmpty()) return Optional.empty();

        Lesson lesson = opt.get();

        if (enrollmentRepository.existsByLesson(lesson)) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (lesson.getDate().isBefore(today)) {
            return Optional.empty();
        }

        if (lesson.getDate().isEqual(today) && lesson.getStartTime().isBefore(now)) {
            return Optional.empty();
        }

        return Optional.of(lessonMapper.toDto(lesson));
    }


    //eğitmenin kendi derslerini getir
    public List<LessonDto> getLessonsByInstructor(User instructor) {

        List<Lesson> lessons = lessonRepository.findByInstructor(instructor);

        return lessonMapper.toDtoList(
                lessonRepository.findByInstructorOrderByCreatedAtDesc(instructor));
    }

    // Eğitmenin kendi dersini silmesi için
    public void deleteLesson(Long lessonId, User instructor) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı"));

        if (!lesson.getInstructor().getUserID().equals(instructor.getUserID())) {
            throw new RuntimeException("Bu dersi silme yetkiniz yok!");
        }

        lessonRepository.delete(lesson);
    }

    public LessonDto updateLesson(Long id, LessonDto dto, User instructor) {

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı"));

        if (!lesson.getInstructor().getUserID().equals(instructor.getUserID())) {
            throw new RuntimeException("Bu dersi güncelleme yetkiniz yok!");
        }

        lesson.setName(dto.getName());
        lesson.setDescription(dto.getDescription());
        lesson.setDate(dto.getDate());
        lesson.setStartTime(dto.getStartTime());
        lesson.setEndTime(dto.getEndTime());
        lesson.setPrice(dto.getPrice());
        lesson.setCategory(dto.getCategory());
        lesson.setLevel(dto.getLevel());
        lesson.setResourcePaths(dto.getResourcePaths());

        Lesson saved = lessonRepository.save(lesson);
        return lessonMapper.toDto(saved);
    }

    public String uploadResource(Long id, MultipartFile file, User instructor) {

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı"));

        if (!lesson.getInstructor().getUserID().equals(instructor.getUserID())) {
            throw new RuntimeException("Bu dersi güncelleme yetkiniz yok!");
        }

        // Klasör yolu
        java.nio.file.Path folderPath = java.nio.file.Paths.get("uploads", "resources");

        // Güvenli dosya adı oluşturma
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }

        String safeFileName = UUID.randomUUID().toString() + extension;
        java.nio.file.Path filePath = folderPath.resolve(safeFileName); // Tam dosya yolu

        try {
            java.nio.file.Files.createDirectories(folderPath);

            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {

            e.printStackTrace();
            throw new RuntimeException("Dosya yüklenirken hata oluştu: " + e.getMessage());
        }

        lesson.getResourcePaths().add(safeFileName);
        lessonRepository.save(lesson);

        return safeFileName;
    }
    public void deleteResource(Long id, String fileName, User instructor) {

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı"));

        if (!lesson.getInstructor().getUserID().equals(instructor.getUserID())) {
            throw new RuntimeException("Yetkiniz yok!");
        }

        lesson.getResourcePaths().remove(fileName);
        lessonRepository.save(lesson);

        File file = new File("uploads/resources/" + fileName);
        if (file.exists()) file.delete();
    }

    public String canJoin(Long lessonId, User student) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı"));

        // 1) Öğrenci derse kayıtlı mı?
        boolean enrolled = enrollmentRepository.existsByLessonAndStudent(lesson, student);
        if (!enrolled) return "NOT_ENROLLED";

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 2) Ders bugün değilse
        if (!lesson.getDate().isEqual(today))
            return "WRONG_DAY";

        LocalTime start = lesson.getStartTime();
        LocalTime end = lesson.getEndTime();

        // 3) Ders zaten bitti
        if (now.isAfter(end))
            return "FINISHED";

        // 4) Ders başladı → derse girebil∫ir
        if (now.isAfter(start))
            return "OK";

        // 5) Ders henüz başlamadı
        return "NOT_STARTED";
    }
    public String canInstructorJoin(Long lessonId, User instructor) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı"));

        // ders bu hocanın mı?
        if (!lesson.getInstructor().getUserID().equals(instructor.getUserID()))
            return "NOT_OWNER";

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (!lesson.getDate().isEqual(today))
            return "WRONG_DAY";

        if (!now.isBefore(lesson.getEndTime()))  // now >= end
            return "FINISHED";

        if (!now.isBefore(lesson.getStartTime())) // now >= start
            return "OK";

        return "NOT_STARTED";
    }



}
