package CoinLogger.api.comparator;

import CoinLogger.api.dto.AccountDto;

import java.util.Comparator;

public class CoinSumBuyPriceComparator implements Comparator<AccountDto> {
    @Override
    public int compare(AccountDto o1, AccountDto o2) {
        if(o1.getCoinName().equals("KRW")){
            return 1;
        }else if(o2.getCoinName().equals("KRW")){
            return -1;
        }

        if (o1.getSumBuyPrice() > o2.getSumBuyPrice()) {
            return -1;
        } else if(o1.getSumBuyPrice() > o2.getSumBuyPrice()) {
            return 1;
        }else {
            return 0;
        }
    }
}
