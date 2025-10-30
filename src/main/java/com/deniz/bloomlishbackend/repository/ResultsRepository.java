package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Results;
import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultsRepository extends JpaRepository<Results,Long> {
    List<Results> findByUser(User user);
}
