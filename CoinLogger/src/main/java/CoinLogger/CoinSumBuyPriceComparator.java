package CoinLogger;

import CoinLogger.api.coinone.AccountDto_Coinone;

import java.util.Comparator;

public class CoinSumBuyPriceComparator implements Comparator<AccountDto_Coinone> {
    @Override
    public int compare(AccountDto_Coinone o1, AccountDto_Coinone o2) {
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
