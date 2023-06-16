package CoinLogger.api.coinone;

import CoinLogger.api.LogDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogDto_Coinone implements LogDto {

    private LocalDateTime orderTime;
    private String orderSort;
    private String coinName;
    private String thatTimePrice;
    private String orderAmount;
    private String signedAmount;
    private String remainAmount;
    private String state;

    private String timeStr;
    private String trader;

    @Override
    public String getTimeStr() {
        return null;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public String getOrderSort() {
        return orderSort;
    }

    public String getCoinName() {
        return coinName;
    }

    public String getThatTimePrice() {
        return thatTimePrice;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public String getSignedAmount() {
        return signedAmount;
    }

    public String getRemainAmount() {
        return remainAmount;
    }

    public String getState() {
        return state;
    }
}
