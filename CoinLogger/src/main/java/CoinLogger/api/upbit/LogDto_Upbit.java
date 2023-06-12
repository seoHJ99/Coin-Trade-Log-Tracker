package CoinLogger.api.upbit;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogDto_Upbit {
    private String orderTime;
    private String orderSort;
    private String coinName;
    private String thatTimePrice;
    private String orderAmount;
    private String signedAmount;
    private String remainAmount;
    private String state;
}
