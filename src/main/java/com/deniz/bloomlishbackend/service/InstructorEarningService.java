package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.InstructorStatsDto;
import com.deniz.bloomlishbackend.dto.LessonDto;
import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.EnrollmentRepository;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorEarningService {

    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;

    // 1️⃣ ÖZET
    public InstructorStatsDto getStats(User instructor) {
        List<Lesson> lessons = lessonRepository.findByInstructor(instructor);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalSoldLessons = 0;
        BigDecimal thisMonthTotal = BigDecimal.ZERO;

        int currentMonth = LocalDate.now().getMonthValue();

        for (Lesson lesson : lessons) {
            List<Enrollment> enrollments = enrollmentRepository.findByLesson(lesson);

            for (Enrollment e : enrollments) {
                if (!e.isPaid()) continue;

                BigDecimal price = BigDecimal.valueOf(lesson.getPrice());

                totalRevenue = totalRevenue.add(price);
                totalSoldLessons++;

                if (e.getEnrolledAt().getMonthValue() == currentMonth) {
                    thisMonthTotal = thisMonthTotal.add(price);
                }
            }
        }

        return new InstructorStatsDto(
                totalSoldLessons,
                totalRevenue,
                thisMonthTotal   // dto'ya yeni alan
        );
    }



    // 2️⃣ Aylık Gelir Grafiği
    public Map<String, BigDecimal> getMonthly(User instructor) {

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        List<Lesson> lessons = lessonRepository.findByInstructor(instructor);

        lessons.forEach(lesson -> {
            List<Enrollment> enrollments =
                    enrollmentRepository.findByLesson(lesson);

            enrollments.stream()
                    .filter(Enrollment::isPaid)
                    .forEach(e -> {
                        String monthName = e.getEnrolledAt()
                                .getMonth()
                                .getDisplayName(TextStyle.FULL, new Locale("tr"));

                        BigDecimal value = BigDecimal.valueOf(lesson.getPrice());

                        result.put(
                                monthName,
                                result.getOrDefault(monthName, BigDecimal.ZERO).add(value)
                        );
                    });
        });

        return result;
    }


    // 3️⃣ Tablo verisi
    public List<Map<String, Object>> getTable(User instructor) {

        List<Lesson> lessons = lessonRepository.findByInstructor(instructor);

        List<Map<String, Object>> table = new ArrayList<>();

        lessons.forEach(lesson -> {
            List<Enrollment> enrollments =
                    enrollmentRepository.findByLesson(lesson);

            enrollments.forEach(e -> {

                Map<String, Object> row = new HashMap<>();
                row.put("date", e.getEnrolledAt()); // DERS TARİHİ DEĞİL ÖDEME TARİHİ
                row.put("lesson", lesson.getName());
                row.put("student", e.getStudent().getUsername());
                row.put("price", lesson.getPrice());
                row.put("status", e.isPaid() ? "Ödendi" : "Beklemede");

                table.add(row);
            });
        });

        return table;
    }

}
