package Bright.BeSafeProject.dto;

import java.time.LocalDateTime;

public record ErrorStatusDTO(
        LocalDateTime timestamp,
        int status,
        String errorMessage
) { }
