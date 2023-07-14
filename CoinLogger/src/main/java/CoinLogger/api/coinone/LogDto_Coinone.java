package CoinLogger.api.coinone;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogDto_Coinone  {

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
