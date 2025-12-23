package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByLessonAndStudent(Lesson lesson, User student);

    List<Enrollment> findByStudent(User student);
    List<Enrollment> findByLesson(Lesson lesson);

    @Query("""
        select e from Enrollment e
        join fetch e.student s
        join fetch e.lesson l
        """)
    Page<Enrollment> findAllWithStudentAndLesson(Pageable pageable);

    //  ADMIN: paid filtreli, sayfalı liste
    @Query("""
        select e from Enrollment e
        join fetch e.student s
        join fetch e.lesson l
        where e.paid = :paid
        """)
    Page<Enrollment> findByPaidWithStudentAndLesson(@Param("paid") boolean paid, Pageable pageable);

    //  ADMIN: arama (email/username/lesson)
    @Query("""
        select e from Enrollment e
        join fetch e.student s
        join fetch e.lesson l
        where (:q is null or :q = '' 
               or lower(s.email) like lower(concat('%', :q, '%'))
               or lower(s.username) like lower(concat('%', :q, '%'))
               or lower(l.name) like lower(concat('%', :q, '%')))
        """)
    Page<Enrollment> searchWithStudentAndLesson(@Param("q") String q, Pageable pageable);

    // paid + arama birlikte
    @Query("""
        select e from Enrollment e
        join fetch e.student s
        join fetch e.lesson l
        where e.paid = :paid
          and (:q is null or :q = '' 
               or lower(s.email) like lower(concat('%', :q, '%'))
               or lower(s.username) like lower(concat('%', :q, '%'))
               or lower(l.name) like lower(concat('%', :q, '%')))
        """)
    Page<Enrollment> searchByPaidWithStudentAndLesson(@Param("paid") boolean paid,
                                                      @Param("q") String q,
                                                      Pageable pageable);
}
