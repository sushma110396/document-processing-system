package io.documentprocessing.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class TokenService {
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    public void storeToken(String token, Long userId) {
        tokenStore.put(token, userId);
    }

    public Long getUserIdFromToken(String token) {
        return tokenStore.get(token);
    }
}
