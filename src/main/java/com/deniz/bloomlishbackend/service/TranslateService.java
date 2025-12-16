package com.deniz.bloomlishbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    private static final String TRANSLATE_API =
            "https://api.mymemory.translated.net/get?q={q}&langpair=en|tr";

    private final RestTemplate restTemplate;

    public String translate(String word) {
        try {
            Map<String, Object> response =
                    restTemplate.getForObject(TRANSLATE_API, Map.class, word);

            if (response == null) return null;

            Map<String, Object> data =
                    (Map<String, Object>) response.get("responseData");

            return data != null ? (String) data.get("translatedText") : null;

        } catch (Exception e) {
            log.warn("Translate error for {} : {}", word, e.getMessage());
            return null;
        }
    }
}
