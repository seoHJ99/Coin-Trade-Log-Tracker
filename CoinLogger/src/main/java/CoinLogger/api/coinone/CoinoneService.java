package CoinLogger.api.coinone;

import CoinLogger.CoinSumBuyPriceComparator;
import CoinLogger.PublicMethod;
import CoinLogger.api.LogDto;
import CoinLogger.api.OrderTimeComparator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CoinoneService {

    private final PublicMethod publicMethod;
    private final JSONParser jsonParser;
    private final HttpClient httpClient = HttpClientBuilder.create().build();
    private final String ACCESS_TOKEN = "1da9e4b2-11f5-4558-9522-b4f50ca2bb6b";
    private final String SECRET_KEY = "9ef621f6-9bd4-4c1e-96ec-0ffed6f6d1bc";
    private final ObjectMapper om = new ObjectMapper();

    @Getter
    static class Payload {
        private final String access_token;
        private final String nonce;
        String to_trade_id;
        int size;
        long from_ts;
        long to_ts;

        public Payload(String access_token, String nonce) {
            this.access_token = access_token;
            this.nonce = nonce;
        }

        public Payload(String access_token, String nonce, String to_trade_id, int size, long from_ts, long to_ts) {
            this.access_token = access_token;
            this.nonce = nonce;
            this.to_trade_id = to_trade_id;
            this.size = size;
            this.from_ts = from_ts;
            this.to_ts = to_ts;
        }
    }


    // 아직 jsonToList 적용 전
    // 내 지갑
    // 1. 주문이 안걸린 잔고  2. 주문이 걸린 잔고   3. 평단가    4. 코인 이름
    public List<List<String>> getAccounts() throws ParseException {
        String ENDPOINT = "https://api.coinone.co.kr/v2.1/account/balance/all";
        String nonce = UUID.randomUUID().toString();
        Payload payload = new Payload(ACCESS_TOKEN, nonce);
        String base64EncodedPayload = makeBase64EncodedPayload(payload);
        String signature = makeSignature(base64EncodedPayload);
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(ENDPOINT);
            String body = om.writeValueAsString(payload);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.addHeader("X-COINONE-PAYLOAD", base64EncodedPayload);
            httpPost.addHeader("X-COINONE-SIGNATURE", signature);

            StringEntity requestEntity = new StringEntity(body);
            httpPost.setEntity(requestEntity);
            HttpResponse httpResponse = httpClient.execute(httpPost);

            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
        result = jsonObject.get("balances").toString();
        return publicMethod.jsonToList(result);
    }

    public List<LogDto> getAllLog() throws ParseException {
        List<List<String>> notDone = getNotDoneOrder();
        List<List<String>> done  = getDoneOrder();
        List<LogDto> result = new ArrayList<>();

        if(!notDone.get(0).isEmpty()) {
            for (int i = 0; i < notDone.size(); i++) {
                Long milliSec = Long.valueOf(notDone.get(i).get(10));
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliSec), ZoneId.systemDefault());
                LogDto_Coinone oneData = LogDto_Coinone.builder()
                        .state("waiting")
                        .orderAmount(notDone.get(i).get(7))
                        .orderSort(notDone.get(i).get(4))
                        .remainAmount(notDone.get(i).get(6))
                        .signedAmount(notDone.get(i).get(9))
                        .thatTimePrice(notDone.get(i).get(13))
                        .orderTime(time)
                        .coinName(notDone.get(i).get(3))
                        .trader("https://coinone.co.kr/common/assets/images/coinone_logo/coinone_logo_blue.svg")
                        .build();
                result.add(oneData);
            }
        }

        for(int i = 0; i < done.size(); i++){
            Long milliSec = Long.valueOf(done.get(i).get(12));
            LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliSec), ZoneId.systemDefault());
            String sort = "";
            if(done.get(i).get(5).equals("true")){
                sort = "매도";
            }else {
                sort = "매수";
            }
            LogDto_Coinone oneData = LogDto_Coinone.builder()
                    .state("done")
                    .orderAmount(done.get(i).get(8))
                    .orderSort(sort)
                    .coinName(done.get(i).get(0))
                    .remainAmount("0")
                    .signedAmount(done.get(i).get(8))
                    .trader("https://coinone.co.kr/common/assets/images/coinone_logo/coinone_logo_blue.svg")
                    .thatTimePrice(done.get(i).get(7))
                    .orderTime(time)
                    .build();
            result.add( oneData);

        }
        Collections.sort(result, new OrderTimeComparator());
        return result;
    }

    // 과거 체결 내역 조회                 market : 시장가    limit : 이득으로 걸어둠    limit stop : 손해로 걸어둠
    // 1. 체결 id    2. 주문id   3.마켓 기준 통화(KRW)   4.주문 체결된 코인   5.주문방식??   6. 매도 주문인지   7. 마켓주문여부
    // 8. 체결된 금액  9. 시간    10. 수수료
    public List<List<String>> getDoneOrder() throws ParseException {
        String nonce = UUID.randomUUID().toString();
        String ENDPOINT = "https://api.coinone.co.kr/v2.1/order/completed_orders/all";

        Long startTime = System.currentTimeMillis();
        Long endTime = startTime - 7_776_000_000L;
        Payload payload = new Payload(ACCESS_TOKEN, nonce, null, 100, endTime, startTime);
        // 조회 개수  조회 시작 날짜   조회 끝 날짜
        String base64EncodedPayload = makeBase64EncodedPayload(payload);
        String signature = makeSignature(base64EncodedPayload);
        String result = "";

        try {
            HttpPost httpPost = new HttpPost(ENDPOINT);
            String body = om.writeValueAsString(payload);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.addHeader("X-COINONE-PAYLOAD", base64EncodedPayload);
            httpPost.addHeader("X-COINONE-SIGNATURE", signature);

            StringEntity requestEntity = new StringEntity(body);
            httpPost.setEntity(requestEntity);
            HttpResponse httpResponse = httpClient.execute(httpPost);

            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
        result = jsonObject.get("completed_orders").toString();
        return publicMethod.jsonToList(result);
    }

    // 미체결 주문
    // 1. 주문  2. 주문 방식  3. 기준통화  4. 코인 이름  5. 매수/매도  6. 주문금액  7. 남은 수량  8. 최초 주문 수량  9. 취소 수량
    // 10. 체결 수량  11. 시간  12. 수수료  13. 수수료율  14. 체결 주문 평단가
    public List<List<String>> getNotDoneOrder() throws ParseException {
        String nonce = UUID.randomUUID().toString();
        String ENDPOINT = "https://api.coinone.co.kr/v2.1/order/open_orders/all";
        Payload payload = new Payload(ACCESS_TOKEN, nonce);
        // 조회 개수  조회 시작 날짜   조회 끝 날짜
        String base64EncodedPayload = makeBase64EncodedPayload(payload);
        String signature = makeSignature(base64EncodedPayload);
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(ENDPOINT);
            String body = om.writeValueAsString(payload);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.addHeader("X-COINONE-PAYLOAD", base64EncodedPayload);
            httpPost.addHeader("X-COINONE-SIGNATURE", signature);

            StringEntity requestEntity = new StringEntity(body);
            httpPost.setEntity(requestEntity);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
        result = jsonObject.get("open_orders").toString();
        return publicMethod.jsonToList(result);
    }

    // 내 코인 현재 가격 정보 가져오기
    public Map<String, String> getMyCoinPrice() throws ParseException {
        Map<String, String> coinMap = new HashMap<>();
        String ENDPOINT = "https://api.coinone.co.kr/public/v2/ticker_new/KRW";
        try {
            HttpGet httpGet = new HttpGet(ENDPOINT);
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String str = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            JSONObject jsonObject = (JSONObject) jsonParser.parse(str);
            JSONArray tickers = (JSONArray) jsonObject.get("tickers");
            for (int i = 0; i < tickers.size(); i++) {
                JSONObject rowOne = (JSONObject) tickers.get(i);
                coinMap.put(rowOne.get("target_currency").toString(), rowOne.get("last").toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return coinMap;
    }


    // 암호화 코드
    private String makeBase64EncodedPayload(Payload payload) {
        try {
            byte[] bytesPayload = om.writeValueAsBytes(payload);
            return Base64.getEncoder().encodeToString(bytesPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // 몰루?
    private String makeSignature(String base64EncodedPayload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA512");
            mac.init(keySpec);
            byte[] messageDigest = mac.doFinal(base64EncodedPayload.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AccountDto_Coinone> accountDtoMaker() throws ParseException {
        List<AccountDto_Coinone> result = new ArrayList<>();
        List<List<String>> accounts = getAccounts();
        Map<String, String> coinMap = getMyCoinPrice();
        for (int i = 0; i < accounts.size(); i++) {
            AccountDto_Coinone oneData = null;
            String coin = accounts.get(i).get(3);
            double amount = Double.valueOf(accounts.get(i).get(0)) + Double.valueOf(accounts.get(i).get(1));
            double buyPrice = Double.parseDouble(accounts.get(i).get(2));
            if (amount == 0) {
                continue;
            }
            String mapValue = coinMap.get(coin.toLowerCase());
            double price;
            if( mapValue == null){
                price =0;
            }else {
                price = Double.parseDouble(mapValue);
            }

            if (price == 0) {
                oneData = AccountDto_Coinone.builder()
                        .coinName(coin)
                        .ownAmount(amount)
                        .bigAmount(BigDecimal.valueOf(amount).toPlainString())
                        .bigBuy(BigDecimal.valueOf(buyPrice).toPlainString())
                        .bigNow(BigDecimal.valueOf(price).toPlainString())
                        .buyPrice(buyPrice)
                        .nowPrice(0)
                        .trader("https://coinone.co.kr/common/assets/images/coinone_logo/coinone_logo_blue.svg")
                        .build();
            } else {
                oneData = AccountDto_Coinone.builder()
                        .coinName(coin)
                        .ownAmount(amount)
                        .bigAmount(BigDecimal.valueOf(amount).toPlainString())
                        .buyPrice(Double.valueOf(buyPrice))
                        .bigBuy(BigDecimal.valueOf(buyPrice).toPlainString())
                        .trader("https://coinone.co.kr/common/assets/images/coinone_logo/coinone_logo_blue.svg")
                        .nowPrice(Double.valueOf(price))
                        .bigNow(BigDecimal.valueOf(price).toPlainString())
                        .build();
            }
            oneData.setSumNowPrice((int) (Double.valueOf(oneData.getNowPrice()) * oneData.getOwnAmount()));
            oneData.setEarning((int) ((oneData.getNowPrice() * oneData.getOwnAmount()) - (oneData.getBuyPrice() * oneData.getOwnAmount())));
            double rate = (oneData.getNowPrice() / oneData.getBuyPrice() * 100d) - 100;
            oneData.setRateOfReturn((Math.round(rate * 100)) / 100d);
            oneData.setSumBuyPrice(oneData.getSumNowPrice() + Math.abs(oneData.getEarning()));
            result.add(oneData);
        }
        Collections.sort( result, new CoinSumBuyPriceComparator());
        return result;
    }
}

