package CoinLogger.api.binance;

import CoinLogger.api.upbit.LogDto;

import java.util.Comparator;

public class LogTimeComparator implements Comparator<LogDto> {

    @Override
    public int compare(LogDto o1, LogDto o2) {
        if (o1.getOrderTime().isAfter( o2.getOrderTime())) {
            return -1;
        } else if(o1.getOrderTime().isBefore( o2.getOrderTime())) {
            return 1;
        }else {
            return 0;
        }
    }
}
