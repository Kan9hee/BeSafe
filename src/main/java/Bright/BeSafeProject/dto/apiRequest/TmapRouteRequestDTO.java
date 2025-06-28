package Bright.BeSafeProject.dto.apiRequest;

public record TmapRouteRequestDTO(
        float startX,
        float startY,
        float endX,
        float endY,
        String startName,
        String endName
) { }
