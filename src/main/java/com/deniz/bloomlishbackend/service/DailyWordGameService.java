package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.*;
import com.deniz.bloomlishbackend.entity.*;
import com.deniz.bloomlishbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DailyWordGameService {

    private static final int QUESTION_COUNT = 5;
    private static final int OPTION_COUNT = 3;

    private final DailyWordSessionRepository sessionRepo;
    private final DailyWordRoundRepository roundRepo;
    private final DailyWordItemRepository itemRepo;
    private final WordRepository wordRepo;

    /* ================= START (YENİ TUR) ================= */

    public DailyWordGameResponse start(Long userId, boolean newRound) {

        LocalDate today = LocalDate.now();

        DailyWordSession session =
                sessionRepo.findByUserIdAndDate(userId, today)
                        .orElseGet(() -> {
                            DailyWordSession s = new DailyWordSession();
                            s.setUserId(userId);
                            s.setDate(today);
                            return sessionRepo.save(s);
                        });


        if (!newRound) {

            boolean hasCompletedRound =
                    roundRepo.findBySessionIdOrderByRoundNumberAsc(session.getId())
                            .stream()
                            .anyMatch(DailyWordRound::isCompleted);

            if (!hasCompletedRound) {

                newRound = true;
            } else {
                return buildSummaryResponse(session);
            }
        }


        // 2) newRound=true ise: önce aktif (bitmemiş) round var mı?
        Optional<DailyWordRound> active =
                roundRepo.findTopBySessionIdOrderByRoundNumberDesc(session.getId())
                        .filter(r -> !r.isCompleted());

        DailyWordRound round;

        if (active.isPresent()) {
            round = active.get();
        } else {
            // 3) aktif yoksa yeni round oluştur
            int nextRoundNumber =
                    roundRepo.findTopBySessionIdOrderByRoundNumberDesc(session.getId())
                            .map(r -> r.getRoundNumber() + 1)
                            .orElse(1);

            round = new DailyWordRound();
            round.setSession(session);
            round.setRoundNumber(nextRoundNumber);
            round.setCompleted(false);
            round = roundRepo.save(round);

            createWordsForRound(round);
        }

        // 4) sıradaki soruyu dön (cevaplanmamış ilk item)
        List<DailyWordItem> items =
                itemRepo.findByRoundIdOrderByOrderIndex(round.getId());

        DailyWordItem nextItem =
                items.stream()
                        .filter(i -> i.getAnsweredCorrect() == null)
                        .findFirst()
                        .orElse(null);

        if (nextItem == null) {
            // round zaten tamamlanmış demektir
            round.setCompleted(true);
            roundRepo.save(round);
            return buildSummaryResponse(session);
        }

        return buildQuestionResponse(round, nextItem);
    }




    /* ================= ANSWER ================= */
    public DailyWordGameResponse answer(Long userId, DailyWordAnswerRequest req) {

        DailyWordItem item = itemRepo.findById(req.getWordId()).orElseThrow();
        DailyWordRound round = item.getRound();

        if (!round.getSession().getUserId().equals(userId)) {
            throw new RuntimeException("Forbidden");
        }

        if (round.isCompleted()) {
            return buildSummaryResponse(round.getSession());
        }

        // 🔹 aynı item daha önce cevaplandıysa: tekrar işlem yapma
        if (item.getAnsweredCorrect() != null) {
            return buildResult(item);
        }

        String correctForm = extractWordForm(item.getWord());
        boolean correct = correctForm.equalsIgnoreCase(req.getSelected());

        item.setAnsweredCorrect(correct);
        itemRepo.save(item);

        boolean finished =
                itemRepo.findByRoundIdOrderByOrderIndex(round.getId())
                        .stream()
                        .allMatch(i -> i.getAnsweredCorrect() != null);

        if (finished) {
            round.setCompleted(true);
            roundRepo.save(round);
            return buildSummaryResponse(round.getSession());
        }

        return buildResult(item);
    }






    /* ================= CREATE WORDS ================= */

    private void createWordsForRound(DailyWordRound round) {

        Set<Long> selectedIds = new HashSet<>();
        List<Word> selected = new ArrayList<>();

        while (selected.size() < QUESTION_COUNT) {
            Word w = wordRepo.getRandomWord();
            if (w != null && selectedIds.add(w.getId())) {
                selected.add(w);
            }
        }

        int index = 1;
        for (Word w : selected) {
            DailyWordItem item = new DailyWordItem();
            item.setRound(round);
            item.setWord(w);
            item.setOrderIndex(index++);
            item.setAnsweredCorrect(null);
            itemRepo.save(item);
        }
    }

    /* ================= QUESTION ================= */

    private DailyWordGameResponse buildQuestionResponse(
            DailyWordRound round,
            DailyWordItem item
    ) {

        Word word = item.getWord();
        String correctForm = extractWordForm(word);
        String blankSentence = blankOut(word.getSentence(), correctForm);

        Set<String> options = new HashSet<>();
        options.add(correctForm);

        while (options.size() < OPTION_COUNT) {
            Word w = wordRepo.getRandomWord();
            if (w != null && !w.getWord().equalsIgnoreCase(correctForm)) {
                options.add(w.getWord());
            }
        }

        List<String> shuffled = new ArrayList<>(options);
        Collections.shuffle(shuffled);

        DailyWordQuestionDto q = new DailyWordQuestionDto();
        q.setSessionId(round.getSession().getId());
        q.setWordId(item.getId());
        q.setOrder(item.getOrderIndex());
        q.setSentence(blankSentence);
        q.setOptions(shuffled);

        DailyWordGameResponse res = new DailyWordGameResponse();
        res.setType("QUESTION");
        res.setQuestion(q);

        return res;
    }

    /* ================= RESULT ================= */

    private DailyWordGameResponse buildResult(DailyWordItem item) {

        DailyWordResultDto r = new DailyWordResultDto();
        r.setCorrect(item.getAnsweredCorrect());
        r.setWord(extractWordForm(item.getWord()));
        r.setMeaning(item.getWord().getMeaning());
        r.setCompleted(false);

        DailyWordGameResponse res = new DailyWordGameResponse();
        res.setType("RESULT");
        res.setResult(r);
        return res;
    }

    /* ================= SUMMARY ================= */

    private DailyWordGameResponse buildSummaryResponse(DailyWordSession session) {

        List<RoundSummaryDto> rounds =
                roundRepo.findBySessionIdOrderByRoundNumberAsc(session.getId())
                        .stream()
                        .filter(DailyWordRound::isCompleted)
                        .map(r -> new RoundSummaryDto(
                                r.getRoundNumber(),
                                itemRepo.findByRoundIdOrderByOrderIndex(r.getId())
                                        .stream()
                                        .map(i -> new WordSummaryDto(
                                                i.getWord().getWord(),
                                                i.getWord().getMeaning()
                                        ))
                                        .toList()
                        ))
                        .toList();

        DailyWordGameResponse res = new DailyWordGameResponse();
        res.setType("SUMMARY");
        res.setRounds(rounds);
        return res;
    }



    /* ================= HELPERS ================= */

    private String extractWordForm(Word word) {
        return extractWordFormFromSentence(
                word.getWord(),
                word.getSentence()
        );
    }

    private String extractWordFormFromSentence(String baseWord, String sentence) {

        if (sentence == null) return baseWord;

        for (String raw : sentence.split("\\s+")) {
            String cleaned = raw.replaceAll("[^a-zA-Z-']", "");
            if (cleaned.equalsIgnoreCase(baseWord)
                    || cleaned.toLowerCase().startsWith(baseWord.toLowerCase())) {
                return cleaned;
            }
        }
        return baseWord;
    }

    private String blankOut(String sentence, String word) {
        return sentence.replaceAll(
                "(?i)" + Pattern.quote(word),
                "_____"
        );
    }
}
