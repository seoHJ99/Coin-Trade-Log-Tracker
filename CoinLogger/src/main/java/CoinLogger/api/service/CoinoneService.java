package CoinLogger.api.service;

import CoinLogger.api.comparator.CoinSumBuyPriceComparator;
import CoinLogger.api.other.PublicMethod;
import CoinLogger.api.comparator.OrderTimeComparator;
import CoinLogger.api.entity.Coinone;
import CoinLogger.api.entity.CoinoneRepository;
import CoinLogger.api.dto.AccountDto;
import CoinLogger.api.dto.LogDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CoinoneService implements ApiServiceInter{

    private final JSONParser jsonParser;
    private final HttpClient httpClient = HttpClientBuilder.create().build();
    private String ACCESS_TOKEN;
    private String SECRET_KEY;
    private final ObjectMapper om = new ObjectMapper();
    private final CoinoneRepository coinoneRepository;
    @Autowired
    private HttpServletRequest request;


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

    public boolean getKeys() {
        String id = request.getSession().getAttribute("userId").toString();
        Coinone coinone = coinoneRepository.findByOwnerId(id);
        if (coinone != null) {
            ACCESS_TOKEN = coinone.getAccessKey();
            SECRET_KEY = coinone.getSecretKey();
            return true;
        } else {
            return false;
        }
    }


    // 내 지갑
    public String getAccounts() throws ParseException {
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
        return result;
    }

    @Override
    public List<LogDto> getAllLogDto() throws ParseException {
        List<LogDto> result = new ArrayList<>();
        String notDoneStr = getNotDoneOrder();
        String doneStr = getDoneOrder();
        if(notDoneStr.contains("error:")){
            LogDto logDto = new LogDto();
            logDto.setState(notDoneStr);
            result.add(logDto);
            return result;
        } else if (doneStr.contains("error:")) {
            LogDto logDto = new LogDto();
            logDto.setState(notDoneStr);
            result.add(logDto);
            return result;
        }
        JSONArray notDone = (JSONArray) jsonParser.parse(getNotDoneOrder());
        JSONArray done = (JSONArray) jsonParser.parse(getDoneOrder());
        if (!notDone.isEmpty()) {
            for (int i = 0; i < notDone.size(); i++) {
                JSONObject jsonObject= ((JSONObject)notDone.get(i));
                Long milliSec = Long.valueOf(jsonObject.get("ordered_at").toString());
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliSec), ZoneId.systemDefault());
                LogDto oneData = LogDto.builder()
                        .state("waiting")
                        .orderAmount(jsonObject.get("original_qty").toString())
                        .orderSort(jsonObject.get("side").toString())
                        .remainAmount(jsonObject.get("remain_qty").toString())
                        .signedAmount(jsonObject.get("remain_qty").toString())
                        .thatTimePrice(jsonObject.get("price").toString())
                        .orderTime(time)
                        .coinName(jsonObject.get("target_currency").toString())
                        .trader("https://coinone.co.kr/common/assets/images/coinone_logo/coinone_logo_blue.svg")
                        .build();
                result.add(oneData);
            }
        }

        if (!done.isEmpty()) {
            for (int i = 0; i < done.size(); i++) {
                JSONObject jsonObject = (JSONObject)done.get(i);
                Long milliSec = Long.valueOf(jsonObject.get("timestamp").toString());
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliSec), ZoneId.systemDefault());
                String sort = "";
                if (jsonObject.get("is_ask").equals("true")) {
                    sort = "매도";
                } else {
                    sort = "매수";
                }
                LogDto oneData = LogDto.builder()
                        .state("done")
                        .orderAmount(jsonObject.get("qty").toString())
                        .orderSort(sort)
                        .coinName(jsonObject.get("target_currency").toString())
                        .remainAmount("0")
                        .signedAmount(jsonObject.get("target_currency").toString())
                        .trader("https://coinone.co.kr/common/assets/images/coinone_logo/coinone_logo_blue.svg")
                        .thatTimePrice(jsonObject.get("price").toString())
                        .orderTime(time)
                        .build();
                result.add(oneData);
            }
        }
        Collections.sort(result, new OrderTimeComparator());
        return result;
    }

    // 과거 체결 내역 조회
    public String getDoneOrder() throws ParseException {
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
        try {
            result = jsonObject.get("completed_orders").toString();

        }catch (NullPointerException e) {
            Object error_code = jsonObject.get("error_code");
            if (error_code.equals("4")) {
                e.printStackTrace();
                return ("코인원 error:제한된 사용자입니다.");
            } else if (error_code.equals("12")) {
                e.printStackTrace();
                return ("코인원 error:api key의 값이 옳지 않습니다.");
            } else if (error_code.equals("40")) {
                e.printStackTrace();
                return ("코인원 error:승인되지 않은 api입니다.");
            } else if (error_code.equals("107")) {
                e.printStackTrace();
                return ("코인원 error:파라미터 에러입니다.");
            } else {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 미체결 주문
    public String getNotDoneOrder() throws ParseException {
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
            e.printStackTrace();
            return "코인원 error: 통신오류!";
        }
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
        try {
            result = jsonObject.get("open_orders").toString();
        } catch (NullPointerException e) {
            Object error_code = jsonObject.get("error_code");
            if (error_code.equals("4")) {
                e.printStackTrace();
                return ("코인원 error:제한된 사용자입니다.");
            } else if (error_code.equals("12")) {
                e.printStackTrace();
                return ("코인원 error:api key의 값이 옳지 않습니다.");
            } else if (error_code.equals("40")) {
                e.printStackTrace();
                return ("코인원 error:승인되지 않은 api입니다.");
            } else if (error_code.equals("107")) {
                e.printStackTrace();
                return ("코인원 error:파라미터 에러입니다.");
            } else {
                e.printStackTrace();
            }
        }
        return result;
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

    @Override
    public List<AccountDto> getAccountList() throws ParseException {
        List<AccountDto> result = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) jsonParser.parse( getAccounts());
        Map<String, String> coinMap = getMyCoinPrice();
            for (int i = 0; i < jsonArray.size(); i++) {
                String coin = "";
                AccountDto oneData = new AccountDto();
                JSONObject jsonObject = null;
                double amount = 0;
                double buyPrice = 0;
                try {
                    jsonObject = (JSONObject) jsonArray.get(i);
                    coin = jsonObject.get("currency").toString();
                    amount = Double.parseDouble(jsonObject.get("available").toString()) + Double.parseDouble(jsonObject.get("limit").toString());
                    buyPrice = Double.parseDouble(jsonObject.get("average_price").toString());
                }catch (NullPointerException e){
                    e.printStackTrace();
                    Object error_code = jsonObject.get("error_code");
                    if (error_code.equals("4")) {
                        oneData.setCoinName("코인원 error:제한된 사용자입니다.");
                        result.add(oneData);
                        return result;
                    } else if (error_code.equals("12")) {
                        oneData.setCoinName("코인원 error:api key의 값이 옳지 않습니다.");
                        result.add(oneData);

                        return result;
                    } else if (error_code.equals("40")) {
                        oneData.setCoinName("코인원 error:승인되지 않은 api입니다.");
                        result.add(oneData);
                        return result;
                    } else if (error_code.equals("107")) {
                        oneData.setCoinName("코인원 error:파라미터 에러입니다.");
                        result.add(oneData);
                        return result;
                    } else {
                        e.printStackTrace();
                    }
                }
                if (amount == 0) {
                    continue;
                }
                String mapValue = coinMap.get(coin.toLowerCase());
                double price;
                if (mapValue == null) {
                    price = 0;
                } else {
                    price = Double.parseDouble(mapValue);
                }

                if (price == 0) {
                    oneData = AccountDto.builder()
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
                    oneData = AccountDto.builder()
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
                oneData.setSumBuyPrice(oneData.getOwnAmount() * oneData.getBuyPrice());
                result.add(oneData);
            }

        Collections.sort(result, new CoinSumBuyPriceComparator());
        return result;
    }
}

