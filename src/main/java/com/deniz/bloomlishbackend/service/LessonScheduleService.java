package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.LessonMessage;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonScheduleService {

    private final LessonRepository lessonRepository;
    private final SimpMessagingTemplate messaging;

    @Scheduled(fixedRate = 30000) // 30 saniyede bir kontrol
    public void checkLessons() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Lesson> todayLessons = lessonRepository.findAll()
                .stream()
                .filter(l -> l.getDate().isEqual(today))
                .toList();

        for (Lesson lesson : todayLessons) {

            LocalTime start = lesson.getStartTime();
            LocalTime end = lesson.getEndTime();

            // 1️⃣ DERSE 1 DAKİKA KALA UYARI
            if (now.isAfter(end.minusMinutes(1)) && now.isBefore(end)) {
                messaging.convertAndSend(
                        "/topic/video/room-" + lesson.getId(),
                        new LessonMessage("warning", "Dersin bitmesine 1 dakika kaldı!")
                );
            }

            // 2️⃣ DERS BİTER BİTMEZ OTOMATİK AT
            if (now.isAfter(end)) {
                messaging.convertAndSend(
                        "/topic/video/room-" + lesson.getId(),
                        new LessonMessage("force-leave", "Ders süresi bitti, görüşme sona erdi.")
                );
            }
        }
    }
}
