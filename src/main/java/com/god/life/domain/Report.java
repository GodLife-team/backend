package com.god.life.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reporterNickname;

    private Long reporterId;

    private String receivedNickname;

    private Long receivedId;

    private String reason;

    private String reportContent;

    private Long reportId;

    private LocalDateTime reportTime;

    private String reportType;

    private boolean complete;

    @PrePersist
    protected void prePersist(){
        reportTime = LocalDateTime.now().withSecond(0).withNano(0);
    }

}
