package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.LessonMapper;
import com.deniz.bloomlishbackend.repository.EnrollmentRepository;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceJoinCheckTest {

    @Mock LessonRepository lessonRepository;
    @Mock LessonMapper lessonMapper; // bu testte kullanılmıyor ama ctor istiyor
    @Mock EnrollmentRepository enrollmentRepository;

    @InjectMocks LessonService lessonService;

    private void setFixedClock(LocalDate date, LocalTime time) {
        // Türkiye için örnek: Europe/Istanbul
        ZoneId zone = ZoneId.of("Europe/Istanbul");
        Instant instant = ZonedDateTime.of(date, time, zone).toInstant();
        lessonService.setClock(Clock.fixed(instant, zone));
    }

    @Test
    void canJoin_notEnrolled_returnsNOT_ENROLLED() {
        // given
        Long lessonId = 10L;
        User student = new User(1L);

        Lesson lesson = mock(Lesson.class);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.existsByLessonAndStudent(lesson, student)).thenReturn(false);

        // when
        String res = lessonService.canJoin(lessonId, student);

        // then
        assertEquals("NOT_ENROLLED", res);
    }

    @Test
    void canJoin_wrongDay_returnsWRONG_DAY() {
        // given
        Long lessonId = 10L;
        User student = new User(1L);
        Lesson lesson = mock(Lesson.class);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.existsByLessonAndStudent(lesson, student)).thenReturn(true);

        // today = 2026-01-20
        setFixedClock(LocalDate.of(2026, 1, 20), LocalTime.of(10, 0));
        when(lesson.getDate()).thenReturn(LocalDate.of(2026, 1, 21)); // farklı gün

        // when
        String res = lessonService.canJoin(lessonId, student);

        // then
        assertEquals("WRONG_DAY", res);
    }

    @Test
    void canJoin_finished_returnsFINISHED() {
        // given
        Long lessonId = 10L;
        User student = new User(1L);
        Lesson lesson = mock(Lesson.class);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.existsByLessonAndStudent(lesson, student)).thenReturn(true);

        // today aynı gün
        LocalDate d = LocalDate.of(2026, 1, 20);
        when(lesson.getDate()).thenReturn(d);

        when(lesson.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(lesson.getEndTime()).thenReturn(LocalTime.of(11, 0));

        // now = 11:01 (bitti)
        setFixedClock(d, LocalTime.of(11, 1));

        // when
        String res = lessonService.canJoin(lessonId, student);

        // then
        assertEquals("FINISHED", res);
    }

    @Test
    void canJoin_ok_returnsOK() {
        Long lessonId = 10L;
        User student = new User(1L);
        Lesson lesson = mock(Lesson.class);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.existsByLessonAndStudent(lesson, student)).thenReturn(true);

        LocalDate d = LocalDate.of(2026, 1, 20);
        when(lesson.getDate()).thenReturn(d);
        when(lesson.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(lesson.getEndTime()).thenReturn(LocalTime.of(11, 0));

        setFixedClock(d, LocalTime.of(10, 30));
        String res = lessonService.canJoin(lessonId, student);
        assertEquals("OK", res);
    }

    @Test
    void canJoin_notStarted_returnsNOT_STARTED() {
        // given
        Long lessonId = 10L;
        User student = new User(1L);
        Lesson lesson = mock(Lesson.class);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.existsByLessonAndStudent(lesson, student)).thenReturn(true);

        LocalDate d = LocalDate.of(2026, 1, 20);
        when(lesson.getDate()).thenReturn(d);
        when(lesson.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(lesson.getEndTime()).thenReturn(LocalTime.of(11, 0));

        // now = 09:59 (başlamadı)
        setFixedClock(d, LocalTime.of(9, 59));

        // when
        String res = lessonService.canJoin(lessonId, student);

        // then
        assertEquals("NOT_STARTED", res);
    }
}
