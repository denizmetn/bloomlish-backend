package com.deniz.bloomlishbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlacementQuestion {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 500)
    private String question;


    @ElementCollection
    @CollectionTable(
            name = "placement_question_options",
            joinColumns = @JoinColumn(name = "placement_question_id")
    )
    @Column(name = "option_text", nullable = false, length = 300)
    private List<String> options;

    @Column(nullable = false, length = 300)
    private String correctAnswer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Level level;
}
