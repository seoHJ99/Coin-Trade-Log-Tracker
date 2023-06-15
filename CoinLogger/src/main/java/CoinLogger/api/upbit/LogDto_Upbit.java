package CoinLogger.api.upbit;

import CoinLogger.api.LogDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogDto_Upbit implements LogDto {
    private LocalDateTime orderTime;
    private String orderSort;
    private String coinName;
    private String thatTimePrice;
    private String orderAmount;
    private String signedAmount;
    private String remainAmount;
    private String state;
    private String timeStr;

}
