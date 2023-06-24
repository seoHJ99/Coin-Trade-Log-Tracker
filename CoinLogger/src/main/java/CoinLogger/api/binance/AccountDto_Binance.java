package CoinLogger.api.binance;

import CoinLogger.api.AccountDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDto_Binance implements AccountDto {
    String coinName ;
    double ownAmount= 0;
    String bigAmount;
    double nowPrice= 0;
    String bigNow;
    double buyPrice= 0;
    String bigBuy;
    int sumNowPrice= 0;
    double sumBuyPrice= 0;
    int earning= 0;
    double rateOfReturn= 0;
    String trader;
}
