package CoinLogger.api.dto;

import lombok.*;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogDto {
    private LocalDateTime orderTime;
    private String orderSort;
    private String coinName;
    private String thatTimePrice;
    private String orderAmount;
    private String signedAmount;
    private String remainAmount;
    private String state;
    private String timeStr;
    private String trader;

    public  LogDto (JSONObject jsonObject, double dollar ) {
        Instant instant = Instant.ofEpochMilli((Long) jsonObject.get("time")); // 밀리초를 Instant으로 변환
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDateTime = localDateTime.format(formatter);
        LocalDateTime orderTime = LocalDateTime.parse(formattedDateTime, formatter);
        if(jsonObject.get("side").equals("SELL")){
            this.orderSort = "매도";
        }else {
            this.orderSort = "매수";
        }
        this.coinName = ((String) jsonObject.get("symbol")).replaceAll("USDT","");
        this.thatTimePrice = jsonObject.get("price").toString()  + "";
        this.orderAmount = (String) jsonObject.get("executedQty");
        this.signedAmount = (String) jsonObject.get("executedQty");
        this.remainAmount = (String) jsonObject.get("executedQty");
        this.state = "체결";
        this.orderTime = orderTime;
        this.timeStr = formattedDateTime;
        this.trader = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHcAAAB3CAMAAAAO5y+4AAAAYFBMVEX////zui/zuSvzuCL76cP53qP757/+/PXytxz30oDzuCb1xln53qH++e3///32yWX30Hj0vz7ytQz88dn41o342JP868n99uT87tH31Yj0wUX64Kn0wk30xFP65bj2zXAy1PzEAAAExklEQVRogcWZ22LaMBBEbQliY2IIDoEmhfb//7LGV0k7I1/l7mOj+mRXs9qREkWz47Cb/38XxCFN/wP4dEhjpTcHv7BxrNTW4Aq7PfijxsYbl7rDluANxWVgyyg2Ap8OFjaOt8lYYGOVbQEW2G1U/SGxW4AhNnw7EWwJDqpqig3aTlLJm5Tajw0nrgFsKLBvb1twgANkBDbEkBiFjddW9ZCkjFgz4wnYVfd4AnZNVU/CrgceK6kevMrJxUdBRn+ygrg4Nr7/LljKS9uJKzm7fEbRnm7BslIPYEuwZtVY1E4Uq2qsD7xA1Z69/WzX0FLPz9hjau79qj0T19x24lh1N9etrGqKzS6f9kqu6ul9PKjkUeDppf7DigewUfTOVJ1dkongL2LQY4ClGSv1ayKWgFV2x6uhqlUKf8khsPyUSgkWqloVc7Ag43IU8NUCrNQ8rABDSfXh7LGKp+9tB9bjsc5ZvQRrgdUQ1gIvwxqlJg3kgPvnpWXYTtVIyeBIaNppqpL/vhMw6tv8cpCrK1Wjvj0d+ZH5kRYInMYZaKD7I0u/IRjtbXJOixumVqMg3SMwkNT9mZWrr3J1uccIq9mQaCaQRmCErbQLwTKvF5ZZn3bwIbCI/NG0DAKLOJ11c4JJcD/mU7DHTiQ/Wbca7LGLPbZ9LT2X6S6QuBi2BANVE6y8STgvrH5wbmIHwcnRMgWm55IvrD5w2UDOal+pk7PjRfqMgZdC7dRin8LUeMQlsEY7IQtHVQ2wHrDE9qrGzlHjUudukb3g0xEZvhrMDCsstSMpYzXaY4xtSk2vGUUuvwQ8V7saTCBqhvUlim6pgh/CXuqbGNYMHvrkCqOfrxG6ywCYmportLcxmTXQV+tjXck3JcAeL3UFCmVYeD9usSXYzdjrpUTGSlEsAOtnr5s314J6bYoLTj1YUersx7RHO1MAnltBHd+WXIoBC2ddYdyL2q3/oXOtRnE1VqeDztFQtX64ZnCnmz3OxhjWrp28e9tGV2pzb9toVI2UnJzEPzV7jJQMVjfgXskCjLD5EZzVVTtB7Bl5Lk2xJVgrpOT8qdF0KjNGp1TppdIvAC7N8ANjX6oG9rwyNcgIXJEjrkYBdJnFD39yeAN7W08gZH2+CRZPp98sWxjdvB00e9Uv2Q6+ES7Tj+3dxRh727uLUb6aY83B7fFcArsMnNteauAm4Xip+eDk4VpQX6mFqZkLTqSF84CBl5onLmjhqKqhhRu8wiAsNqwEjHzyrIyrazX8FBIXwb7Ackh446ahy4yhqim2cY5TQnguDj55sJMOxyp2/BXd3mN2K3C91Ggw/buBBfZgJz9613ErWKkNVScUK73U6IypuLqMPZIi7mJMgJtEC94HxPrAlapDYSvPxb787pMU9VJjw6dqmu1cJZtxo2BNjtJYz+pbkTG+mPOYfjgSMD0yMXahpPrgqg6K9Q0JUOT1sOJizmPeKODB28nGrtBAdvB2Mos8exTw4EOix666t20MqnpNJU8Ah8L6hkQZWZAi18E91+oNZAdVdVgs9VwhGsgO2E7hJNUHUPUWWAAOc1wAsG1vlnupsbHzvbCGDKOd1vFSY6PzXGt5qbHROJBtlGyBX6reHltlvFUD2bHzvbCGjNsS7D+dtDwqLo7Z0QAAAABJRU5ErkJggg==";
    }
}
