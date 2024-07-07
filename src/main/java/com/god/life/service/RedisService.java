package com.god.life.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> template;
    public static final String NO_VALUE = "NO VALUE FOR KEY";

    public void setValue(String key, String data, long duration) {
        template.opsForValue().set(key, data, duration, TimeUnit.MILLISECONDS);
    }

    public String getValues(String key){
        String value = (String) template.opsForValue().get(key);
        if (value == null) {
            return NO_VALUE;
        }
        return value;
    }

    public void deleteValue(String key) {
        template.delete(key);
    }

}
