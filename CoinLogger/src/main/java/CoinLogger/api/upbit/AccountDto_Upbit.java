package CoinLogger.api.upbit;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AccountDto_Upbit {
    private String coinName;
    private double ownAmount;
    private double nowPrice;
    private int buyPrice;
    private int sumNowPrice;
    private int earning;
    private double rateOfReturn;
}
