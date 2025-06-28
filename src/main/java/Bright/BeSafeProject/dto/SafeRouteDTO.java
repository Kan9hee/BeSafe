package Bright.BeSafeProject.dto;

import java.util.List;

public record SafeRouteDTO(
        List<LocationDTO> routeNodes,
        List<LocationDTO> lightNodes
) { }
