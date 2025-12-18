package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Results;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ResultsRepository extends JpaRepository<Results, Long> {
    List<Results> findByUserUserIDOrderByTakenAtAsc(Long userId);
    List<Results> findByUserEmail(String email);
    List<Results> findTop30ByUserUserIDOrderByTakenAtDesc(Long userId);
    @Query("""
    select coalesce(sum(r.correct), 0)
    from Results r
    join r.quiz q
    where r.user.userID = :userId
      and q.quizType = :quizType
""")
    int sumCorrectByUserAndQuizType(@Param("userId") Long userId,
                                    @Param("quizType") String quizType);

    @Query("""
        select count(r)
        from Results r
        where r.user.userID = :userId
    """)
    long countByUserId(@Param("userId") Long userId);

    @Query("""
        select coalesce(sum(r.score), 0)
        from Results r
        where r.user.userID = :userId
    """)
    int sumScoreByUserId(@Param("userId") Long userId);

    @Query("""
      select r from Results r
      join fetch r.quiz q
      where r.user.userID = :userId
        and r.takenAt >= :from
      order by r.takenAt desc
    """)
    List<Results> findRecentResultsWithQuiz(@Param("userId") Long userId,
                                            @Param("from") LocalDateTime from);

    @Query("""
      select count(r) from Results r
      where r.user.userID = :userId
        and r.takenAt >= :from
    """)
    long countSince(@Param("userId") Long userId,
                    @Param("from") LocalDateTime from);

    @Query("""
      select max(r.takenAt) from Results r
      where r.user.userID = :userId
    """)
    LocalDateTime findLastTakenAt(@Param("userId") Long userId);
}
