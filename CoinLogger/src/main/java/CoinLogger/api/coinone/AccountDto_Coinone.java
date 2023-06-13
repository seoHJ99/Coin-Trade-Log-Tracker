package CoinLogger.api.coinone;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDto_Coinone {
    private String coinName;
    private double ownAmount;
    private double nowPrice;
    private double buyPrice;
    private int sumNowPrice;
    private int earning;
    private double rateOfReturn;
}
