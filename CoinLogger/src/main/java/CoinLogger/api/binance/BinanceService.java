package CoinLogger.api.binance;

import CoinLogger.PublicMethod;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
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


    public String getOneCoinTradeLog(String coinName) throws IOException, ParseException {
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
        return entityString;
    }

    public MemberCoin makeCoinEntityAndSave(String entityString) throws ParseException {
        MemberCoin newData = null;
        if (!entityString.contains("code") && !(entityString).equals("[]")) {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(entityString);
//          id는 나중에 회원가입 구현한 다음 다시 코드 짜기.
            String id = "test1111";
            String coinName = "";
            for(int i =0; i<jsonArray.size(); i++){
                coinName = ((JSONObject)jsonArray.get(i)).get("symbol").toString().replaceAll("USDT","");
            }
            MemberCoin dbCoin = memberCoinListRepository.findByCoinNameAndId(coinName, id);
            if(dbCoin == null){
                double amount = 0;
                double avgPrice = 0;
                long milliTime = 0;
                for(int i =0; i < jsonArray.size(); i++){
                    JSONObject jsonObject = ((JSONObject) jsonArray.get(i));
                    if(jsonObject.get("side").equals("BUY")){
                        double buyAmount = Double.parseDouble(jsonObject.get("executedQty").toString());
                        double buyPrice = Double.parseDouble(jsonObject.get("price").toString());
                        avgPrice = ((avgPrice * amount) + (buyAmount * buyPrice)) / (amount + buyAmount);
                        amount += buyAmount;

                    } else if (jsonObject.get("side").equals("SELL")) {
                        amount -= Double.parseDouble(jsonObject.get("executedQty").toString());
                    }
                    if(i == jsonArray.size()-1){
                        milliTime = Long.parseLong(jsonObject.get("time").toString());
                    }
                }
                newData = MemberCoin.builder()
                  .coinName(coinName)
                  .owner_id(id)
                  .avg_buy_price(avgPrice)
                  .amount(amount)
                  .mill_time(milliTime)
                  .build();
                memberCoinListRepository.save(newData);
            }else {
                int breakPoint = 0;
                double amount = dbCoin.getAmount();
                double avgPrice = dbCoin.getAvg_buy_price();
                long milliTime = dbCoin.getMill_time();
                for(int i =0; i<jsonArray.size(); i++){
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    breakPoint++;
                    if(dbCoin.getMill_time() < Long.parseLong(jsonObject.get("time").toString())){
                        breakPoint = i;
                    }
                }
                for(int i = breakPoint; i < jsonArray.size(); i++){
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if(jsonObject.get("side").equals("BUY")){
                        double buyAmount = Double.parseDouble(jsonObject.get("executedQty").toString());
                        double buyPrice = Double.parseDouble(jsonObject.get("price").toString());
                        avgPrice = ((avgPrice * amount) + (buyAmount * buyPrice)) / (amount + buyAmount);
                        amount += buyAmount;
                    } else if (jsonObject.get("side").equals("SELL")) {
                        amount = amount - Double.parseDouble(jsonObject.get("executedQty").toString());
                    }
                    if(i == jsonArray.size()-1){
                        milliTime = Long.parseLong(jsonObject.get("time").toString());
                    }
                }
                newData = MemberCoin.builder()
                        .coinName(coinName)
                        .owner_id(id)
                        .idx(dbCoin.getIdx())
                        .avg_buy_price(avgPrice)
                        .amount(amount)
                        .mill_time(milliTime)
                        .build();
                memberCoinListRepository.save(newData);
            }
        }
        return newData;
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

    public List<MemberCoin> getAllCoinLog() throws IOException, ParseException {
        List<String> coinNames = getMyCoinName();
        List<MemberCoin> entityList = new ArrayList<>();
        for (int i = 0; i < coinNames.size(); i++) {
            String entityString = getOneCoinTradeLog(coinNames.get(i));
            MemberCoin memberCoin = makeCoinEntityAndSave(entityString);
            entityList.add(memberCoin);
        }
        return entityList;
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
            result = Double.parseDouble((String) jsonObject.get("price"));
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
            String coinName = myCoin.get(i).get(0);
            MemberCoin entity = memberCoinListRepository.findByCoinName(coinName);
            double coinPrice = getCoinPrice(myCoin.get(i).get(0));
            double coinAmount = Double.parseDouble(myCoin.get(i).get(1));
            sumNowPriceWon = coinPrice * oneDollarWon * coinAmount;
            AccountDto_Binance dto = AccountDto_Binance.builder()
                    .coinName(myCoin.get(i).get(0))
                    .bigAmount(""+BigDecimal.valueOf(coinAmount))
                    .bigNow(BigDecimal.valueOf(coinPrice * oneDollarWon) + "")
                    .nowPrice(coinPrice * oneDollarWon)
                    .ownAmount(coinAmount)
                    .sumNowPrice((int) sumNowPriceWon)
                    .build();
            if(entity != null){
                dto.setBuyPrice(entity.getAvg_buy_price());
                dto.setBigBuy(BigDecimal.valueOf(entity.getAvg_buy_price()) + "");
                dto.setSumBuyPrice(dto.getOwnAmount() * dto.getBuyPrice());
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

    public void saveWalletInfo(Map<String, String[]> info){
        MemberCoin memberCoin = new MemberCoin();
        Iterator<String> coins = info.keySet().iterator();
        while (coins.hasNext()){
            String coinName = coins.next();
            memberCoin = MemberCoin.builder()
                    .coinName(coinName)
                    .avg_buy_price(Double.parseDouble(info.get(coinName)[0]))
                    .amount(Double.parseDouble(info.get(coinName)[1]))
                    .build();
        }
    }
}