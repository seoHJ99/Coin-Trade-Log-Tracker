package CoinLogger.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AccountDto{
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
