package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.dto.AdminEnrollmentRowDto;
import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByLessonAndStudent(Lesson lesson, User student);
    Optional<Enrollment> findByLessonAndStudent(Lesson lesson, User student);

    boolean existsByLesson(Lesson lesson);

    List<Enrollment> findByStudent(User student);
    List<Enrollment> findByLesson(Lesson lesson);

    Optional<Enrollment> findByLessonIdAndStudentUserID(Long lessonId, Long studentId);


    // ✅ ADMIN: hepsi (DTO)
    @Query("""
        select new com.deniz.bloomlishbackend.dto.AdminEnrollmentRowDto(
            e.id,
            s.userID,
            s.username,
            s.email,
            l.id,
            l.name,
            l.price,
            case when e.paid = true then 'PAID' else 'UNPAID' end,
            e.enrolledAt
        )
        from Enrollment e
        join e.student s
        join e.lesson l
    """)
    Page<AdminEnrollmentRowDto> findAdminRows(Pageable pageable);

    // ✅ ADMIN: paid filtre (DTO)
    @Query("""
        select new com.deniz.bloomlishbackend.dto.AdminEnrollmentRowDto(
            e.id,
            s.userID,
            s.username,
            s.email,
            l.id,
            l.name,
            l.price,
            case when e.paid = true then 'PAID' else 'UNPAID' end,
            e.enrolledAt
        )
        from Enrollment e
        join e.student s
        join e.lesson l
        where e.paid = :paid
    """)
    Page<AdminEnrollmentRowDto> findAdminRowsByPaid(@Param("paid") boolean paid, Pageable pageable);

    // ✅ ADMIN: search (DTO)
    @Query("""
        select new com.deniz.bloomlishbackend.dto.AdminEnrollmentRowDto(
            e.id,
            s.userID,
            s.username,
            s.email,
            l.id,
            l.name,
            l.price,
            case when e.paid = true then 'PAID' else 'UNPAID' end,
            e.enrolledAt
        )
        from Enrollment e
        join e.student s
        join e.lesson l
        where (:q is null or :q = '' 
               or lower(s.email) like lower(concat('%', :q, '%'))
               or lower(s.username) like lower(concat('%', :q, '%'))
               or lower(l.name) like lower(concat('%', :q, '%')))
    """)
    Page<AdminEnrollmentRowDto> searchAdminRows(@Param("q") String q, Pageable pageable);

    // ✅ ADMIN: paid + search (DTO)
    @Query("""
        select new com.deniz.bloomlishbackend.dto.AdminEnrollmentRowDto(
            e.id,
            s.userID,
            s.username,
            s.email,
            l.id,
            l.name,
            l.price,
            case when e.paid = true then 'PAID' else 'UNPAID' end,
            e.enrolledAt
        )
        from Enrollment e
        join e.student s
        join e.lesson l
        where e.paid = :paid
          and (:q is null or :q = '' 
               or lower(s.email) like lower(concat('%', :q, '%'))
               or lower(s.username) like lower(concat('%', :q, '%'))
               or lower(l.name) like lower(concat('%', :q, '%')))
    """)
    Page<AdminEnrollmentRowDto> searchAdminRowsByPaid(
            @Param("paid") boolean paid,
            @Param("q") String q,
            Pageable pageable
    );
}
