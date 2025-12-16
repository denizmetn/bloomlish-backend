package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.DictionaryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryService {

    private static final String API =
            "https://api.dictionaryapi.dev/api/v2/entries/en/%s";

    private final RestTemplate restTemplate;
    private final TranslateService translateService;

    public DictionaryResult fetch(String word) {
        try {
            String url = String.format(API, word);
            List<Map<String, Object>> response =
                    restTemplate.getForObject(url, List.class);

            if (response == null || response.isEmpty()) return null;

            Map<String, Object> entry = response.get(0);
            List<Map<String, Object>> meanings =
                    (List<Map<String, Object>>) entry.get("meanings");

            if (meanings == null) return null;

            for (Map<String, Object> m : meanings) {
                List<Map<String, Object>> defs =
                        (List<Map<String, Object>>) m.get("definitions");

                if (defs == null) continue;

                for (Map<String, Object> d : defs) {
                    String example = (String) d.get("example");
                    if (example != null && !example.isBlank()) {

                        String tr = translateService.translate(word);
                        if (tr == null || tr.isBlank()) return null;

                        DictionaryResult r = new DictionaryResult();
                        r.setWord(word);
                        r.setMeaning(tr);
                        r.setExample(example);
                        return r;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Dictionary API error for {} : {}", word, e.getMessage());
        }
        return null;
    }
}
