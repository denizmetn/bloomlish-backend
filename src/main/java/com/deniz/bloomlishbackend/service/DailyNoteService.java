package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.DailyNoteDto;
import com.deniz.bloomlishbackend.entity.DailyNote;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.BlogPostMapper;
import com.deniz.bloomlishbackend.repository.DailyNoteRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DailyNoteService {
    private final DailyNoteRepository dailyNoteRepository;
    private final BlogPostMapper blogPostMapper;
    private final UserRepository userRepository;

    public DailyNoteDto create(DailyNoteDto dailyNoteDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        DailyNote dailyNote = DailyNote.builder()
                .content(dailyNoteDto.getContent())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
        DailyNote saved = dailyNoteRepository.save(dailyNote);
        DailyNoteDto response = blogPostMapper.dailyToDto(saved);
        response.setUserId(user.getUserID());
        return response;
    }

    public Page<DailyNoteDto> getAllMyNotes(int page, int size, String email) {
        User user= userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return dailyNoteRepository.findByUserUserID(user.getUserID(),pageable)
                .map(blogPostMapper::dailyToDto);
    }

    public void delete(Long id, String username) {
        DailyNote dailyNote = dailyNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı"));
        if (!dailyNote.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Bu postu silme yetkiniz yoktur!");
        }
        dailyNoteRepository.delete(dailyNote);
    }
    public DailyNoteDto update(Long id,DailyNoteDto dailyNoteDto, String username) {
        DailyNote dailyNote=dailyNoteRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Post bulunamadı."));
        if (!dailyNote.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Bu postu değiştirme yetkiniz yoktur!");
        }
        dailyNote.setContent(dailyNoteDto.getContent());
       return blogPostMapper.dailyToDto(dailyNoteRepository.save(dailyNote));
    }
}
