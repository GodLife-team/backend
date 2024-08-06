package com.god.life.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> template;
    public static final String NO_VALUE = "NO VALUE FOR KEY";

    public void setValue(String key, String data) {
        template.opsForValue().set(key, data);
    }

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

    public void addValueToRightOnList(String key, String data) {
        ListOperations<String, Object> list = template.opsForList();
        if (list.size(key).intValue() == 10)   {
            list.leftPop(key);
        }
        list.rightPush(key, data);
    }

    public List<String> getList(String key) {
        ListOperations<String, Object> list = template.opsForList();
        List<Object> recommendList = list.range(key, 0, -1);
        if(recommendList == null) return new ArrayList<>();

        return recommendList.stream().map(r -> (String)r).toList();
    }

    public void deleteValueInList(String recommendBoardKey, String removeId) {
        ListOperations<String, Object> list = template.opsForList();
        list.remove(recommendBoardKey, 0, removeId); //0은 모두 삭제, count > 0 head -> tail까지 돌면서 count개 삭제, cout<0은 반대
    }


}
