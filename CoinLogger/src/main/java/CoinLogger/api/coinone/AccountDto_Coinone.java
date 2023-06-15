package CoinLogger.api.coinone;

import CoinLogger.api.AccountDto;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDto_Coinone implements AccountDto {
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
}
