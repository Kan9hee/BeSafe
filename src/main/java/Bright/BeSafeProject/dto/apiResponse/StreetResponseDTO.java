package Bright.BeSafeProject.dto.apiResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record StreetResponseDTO(int currentCount,
                                List<StreetResponseDataDTO> data,
                                int matchCount,
                                int page,
                                int perPage,
                                int totalCount) {

        public record StreetResponseDataDTO(
                @JsonProperty("위도")
                double latitude,
                @JsonProperty("경도")
                double longitude
        ){ }
}
