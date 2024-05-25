package com.god.life.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
public class PathVariableCheckHandler implements HandlerInterceptor {

    // 게시판 조회, 수정, 삭제 시 ID가 Long형인지 판단한다
    // --> String 형이면 조회 XXX

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//    }


}
