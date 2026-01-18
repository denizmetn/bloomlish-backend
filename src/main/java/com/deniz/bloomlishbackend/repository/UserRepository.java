package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findTop10ByOrderByWeeklyXpDesc();
    @Query("""
        select u from User u
        where (:q is null or :q = '' 
               or lower(u.email) like lower(concat('%', :q, '%'))
               or lower(u.username) like lower(concat('%', :q, '%')))
        """)
    Page<User> search(@Param("q") String q, Pageable pageable);

}
