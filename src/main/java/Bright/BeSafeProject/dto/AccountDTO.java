package Bright.BeSafeProject.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AccountDTO(
        Long id,
        String platform,
        String name,
        String email,
        String password,
        String authority,
        LocalDateTime createdAt
) { }