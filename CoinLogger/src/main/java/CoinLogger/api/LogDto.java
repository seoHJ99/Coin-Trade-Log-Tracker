package CoinLogger.api;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Date;

public interface LogDto {
       LocalDateTime orderTime = null;
       String orderSort= null;
       String coinName= null;
       String thatTimePrice= null;
       String orderAmount= null;
       String signedAmount= null;
       String remainAmount= null;
       String state= null;
       String timeStr = null;

       String getTimeStr();

       LocalDateTime getOrderTime();
       String getOrderSort();
       String getCoinName();
       String getThatTimePrice();
       String getOrderAmount();
       String getSignedAmount();
       String getRemainAmount();
       String getState();

}
