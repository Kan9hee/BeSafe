package Bright.BeSafeProject.dto;

import java.time.LocalDateTime;

public record HistoryDTO(
        float startX,
        float startY,
        float endX,
        float endY,
        LocalDateTime usedAt
) { }
