package CoinLogger.api.upbit;

import CoinLogger.api.AccountDto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AccountDto_Upbit implements AccountDto {
    private String coinName;
    private double ownAmount;
    private String bigAmount;
    private double nowPrice;
    private String bigNow;
    private double buyPrice;
    private String bigBuy;
    private int sumNowPrice;
    private double sumBuyPrice;
    private int earning;
    private double rateOfReturn;
    private String trader;
}
