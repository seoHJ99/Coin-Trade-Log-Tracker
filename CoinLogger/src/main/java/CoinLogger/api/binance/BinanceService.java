package CoinLogger.api.binance;

import CoinLogger.CoinSumBuyPriceComparator;
import CoinLogger.PublicMethod;
import CoinLogger.api.AccountDto;
import CoinLogger.api.coinone.AccountDto_Coinone;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BinanceService {
    private final HttpClient httpClient;
    private final JSONParser jsonParser;
    private final PublicMethod publicMethod;
    private final MemberCoinListRepository memberCoinListRepository;
    private static double oneDollarWon;
    String secretKey = "";
    String accessKey = "";
    String serverUrl = "https://api.binance.com";
    int plusTime = 0;


    public void getOneCoinTradeLog(String coinName) throws IOException, ParseException {
        HmacSignatureGenerator signature = new HmacSignatureGenerator(secretKey);

        String timestamp = Long.toString(System.currentTimeMillis() + plusTime);
        String queryString = "timestamp=" + timestamp + "&symbol=" + coinName + "USDT";
        String actualSign = signature.getSignature(queryString);

        HttpGet request = new HttpGet(serverUrl + "/api/v3/allOrders?"
                + queryString + "&signature=" + actualSign);

        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-MBX-APIKEY", accessKey);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        ;

        String entityString = EntityUtils.toString(entity, "UTF-8");

        if (entityString.contains("-1021")) {
            plusTime -= 1000;
            getOneCoinTradeLog(coinName);

        } else if (entityString.contains("-1131")) {
            plusTime += 1000;
            getOneCoinTradeLog(coinName);
        }

        if (!entityString.contains("code") && !(entityString).equals("[]")) {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(entityString);

//          id는 나중에 회원가입 구현한 다음 다시 코드 짜기.
            String id = "test1111";
//
            MemberCoin dbCoin = memberCoinListRepository.findByCoinName(coinName, id);
            JSONObject jsonObject = ((JSONObject) jsonArray.get(jsonArray.size() - 1));
            if(dbCoin != null) {
                System.out.println("jsonObject = " + jsonObject.toString());
                double newAvgPrice = 0;
                double amount = 0;
                double executedQty = Double.parseDouble(jsonObject.get("executedQty").toString());
                double price = Double.parseDouble(jsonObject.get("price").toString());
                long time = Long.parseLong(jsonObject.get("time").toString());
                if (time > dbCoin.getMill_time()) {
                    if (jsonObject.get("side").equals("BUY")) {
                        newAvgPrice = (dbCoin.getAvg_buy_price() * dbCoin.getAmount() +
                                price * executedQty);
                        amount = dbCoin.getAmount() + executedQty;
                        newAvgPrice = newAvgPrice / amount;
                    } else if (jsonObject.get("side").equals("SELL")) {
                        newAvgPrice = dbCoin.getAvg_buy_price();
                        amount = dbCoin.getAmount() - executedQty;
                    }
                    MemberCoin updatedCoin = MemberCoin.builder()
                            .coin_name(coinName)
                            .idx(dbCoin.getIdx())
                            .owner_id(dbCoin.getOwner_id())
                            .amount(amount)
                            .avg_buy_price(newAvgPrice)
                            .mill_time(time)
                            .build();
                    memberCoinListRepository.save(updatedCoin);
                }
            }else if (jsonObject.get("side").equals("BUY")){
                String name = coinName;
                double executedQty = Double.parseDouble(jsonObject.get("executedQty").toString());
                double price = Double.parseDouble(jsonObject.get("price").toString());
                long time = Long.parseLong(jsonObject.get("time").toString());
                MemberCoin updatedCoin = MemberCoin.builder()
                        .coin_name(coinName)
                        .owner_id(id)
                        .amount(executedQty)
                        .avg_buy_price(price)
                        .mill_time(time)
                        .build();
                memberCoinListRepository.save(updatedCoin);
            }
        }
    }


    public List<List<String>> getAccountCoin() throws IOException, ParseException {

        HmacSignatureGenerator signature = new HmacSignatureGenerator(secretKey);

        String timestamp = Long.toString(System.currentTimeMillis() + plusTime);
        String queryString = "timestamp=" + timestamp;
        String actualSign = signature.getSignature(queryString);

        HttpPost request = new HttpPost(serverUrl + "/sapi/v3/asset/getUserAsset?"
                + queryString + "&signature=" + actualSign);

        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-MBX-APIKEY", accessKey);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        ;
        String entityString = EntityUtils.toString(entity, "UTF-8");

        if (entityString.contains("-1021")) {
            plusTime -= 1000;
            return getAccountCoin();

        } else if (entityString.contains("-1131")) {
            plusTime += 1000;
            return getAccountCoin();
        }
        List<List<String>> result = publicMethod.jsonToList(entityString);
        return result;
    }

    public List<String> getMyCoinName() throws IOException {
        HmacSignatureGenerator signature = new HmacSignatureGenerator(secretKey);

        String timestamp = Long.toString(System.currentTimeMillis() + plusTime);
        String queryString = "timestamp=" + timestamp;
        String actualSign = signature.getSignature(queryString);

        HttpPost request = new HttpPost(serverUrl + "/sapi/v3/asset/getUserAsset?"
                + queryString + "&signature=" + actualSign);

        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-MBX-APIKEY", accessKey);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        ;

        String entityString = EntityUtils.toString(entity, "UTF-8");

        if (entityString.contains("-1021")) {
            plusTime -= 1000;
            return getMyCoinName();

        } else if (entityString.contains("-1131")) {
            plusTime += 1000;
            return getMyCoinName();
        }

        List<List<String>> result = publicMethod.jsonToList(entityString);
        List<String> coinNames = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            coinNames.add(result.get(i).get(0));
        }
        return coinNames;
    }

    public void getAllCoinLog() throws IOException, ParseException {
        List<String> coinNames = getMyCoinName();
        for (int i = 0; i < coinNames.size(); i++) {
            getOneCoinTradeLog(coinNames.get(i));
        }

    }


    public double getCoinPrice(String coinName) throws IOException, ParseException {
        HttpGet request = new HttpGet(serverUrl + "/api/v3/avgPrice?symbol=" + coinName + "USDT");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-MBX-APIKEY", accessKey);
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity, "UTF-8");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
        jsonObject = new JSONObject(jsonObject);
        double result = 0;
        if (jsonObject.containsKey("price")) {
            result = Double.valueOf((String) jsonObject.get("price"));
        }
        return result;
    }

    public List<AccountDto_Binance> makeAccountDto() throws IOException, ParseException {
        List<List<String>> myCoin = getAccountCoin();
        if (oneDollarWon == 0) {
            HttpGet request = new HttpGet("https://quotation-api-cdn.dunamu.com/v1/forex/recent?codes=FRX.KRWUSD");
            request.addHeader("Content-Type", "application/json");
            request.addHeader("X-MBX-APIKEY", accessKey);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            JSONArray jsonObject = (JSONArray) jsonParser.parse(result);
            JSONObject a = (JSONObject) jsonObject.get(0);
            oneDollarWon = (double) a.get("basePrice");
        }
        double sumNowPriceWon;
        List<AccountDto_Binance> dtoList = new ArrayList<>();
        for (int i = 0; i < myCoin.size(); i++) {
            double coinPrice = getCoinPrice(myCoin.get(i).get(0));
            double coinAmount = Double.valueOf(myCoin.get(i).get(1));
            sumNowPriceWon = coinPrice * oneDollarWon * coinAmount;
            AccountDto_Binance dto = AccountDto_Binance.builder()
                    .coinName(myCoin.get(i).get(0))
                    .nowPrice(coinPrice * oneDollarWon)
                    .ownAmount(coinAmount)
                    .sumNowPrice((int) sumNowPriceWon)
                    .build();
            dtoList.add(dto);
        }
        return dtoList;
    }
}
