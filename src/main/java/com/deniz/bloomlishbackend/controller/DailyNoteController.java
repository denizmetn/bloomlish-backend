package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.CommentDto;
import com.deniz.bloomlishbackend.dto.DailyNoteDto;
import com.deniz.bloomlishbackend.entity.DailyNote;
import com.deniz.bloomlishbackend.service.DailyNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/notes")
@RequiredArgsConstructor
public class DailyNoteController {
    private final DailyNoteService dailyNoteService;

    @PostMapping("/create")
    public ResponseEntity<DailyNoteDto> create(
            @RequestBody DailyNoteDto dailyNoteDto,
            @AuthenticationPrincipal UserDetails userdetails) {
        String username = userdetails.getUsername();
        return ResponseEntity.ok(dailyNoteService.create(dailyNoteDto,username));
    }
    @GetMapping("/get-all")
    public ResponseEntity<Page<DailyNoteDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userdetails) {
        String username = userdetails.getUsername();
        return ResponseEntity.ok(dailyNoteService.getAllMyNotes(page,size,username));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userdetails){
        String username = userdetails.getUsername();
        dailyNoteService.delete(id,username);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<DailyNoteDto> update(
            @PathVariable Long id,
            @RequestBody DailyNoteDto dailyNoteDto,
            @AuthenticationPrincipal UserDetails userdetails) {
        String username = userdetails.getUsername();
       return ResponseEntity.ok(dailyNoteService.update(id,dailyNoteDto,username));

    }

}
