package CoinLogger.api;

import CoinLogger.api.upbit.AccountDto;
import java.util.Comparator;

public class NameComparator implements Comparator<AccountDto> {

    @Override
    public int compare(AccountDto o1, AccountDto o2) {
        if(o1.getCoinName().charAt(0) < o2.getCoinName().charAt(0)){
            return -1;
        }else if(o1.getCoinName().charAt(0) == o2.getCoinName().charAt(0)){
            if(o1.getCoinName().equals(o2.getCoinName())){
                return 0;
            }else {
                return 1;
            }
        }else {
            return 1;
        }
    }
}
