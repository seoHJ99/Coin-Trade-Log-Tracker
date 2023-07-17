package CoinLogger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiDto {
    private final String accessKey;
    private final String secretKey;
}
