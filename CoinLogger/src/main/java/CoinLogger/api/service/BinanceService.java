package CoinLogger.api.service;

import CoinLogger.api.comparator.CoinSumBuyPriceComparator;
import CoinLogger.api.other.PublicMethod;
import CoinLogger.api.binance.HmacSignatureGenerator;
import CoinLogger.api.comparator.LogTimeComparator;
import CoinLogger.api.entity.Binance;
import CoinLogger.api.entity.BinanceRepository;
import CoinLogger.api.entity.MemberCoin;
import CoinLogger.api.entity.MemberCoinListRepository;
import CoinLogger.api.dto.AccountDto;
import CoinLogger.api.dto.LogDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BinanceService implements ApiServiceInter {
    private final HttpClient httpClient;
    private final JSONParser jsonParser;
    private final PublicMethod publicMethod;
    private final MemberCoinListRepository memberCoinListRepository;

    private final BinanceRepository binanceRepository;
    private double oneDollarWon;
    String secretKey;
    String accessKey;
    String serverUrl = "https://api.binance.com";
    int plusTime = 0;

    @Autowired
    private HttpServletRequest request;

    public boolean getKeys(){
        String id = request.getSession().getAttribute("userId").toString();
        Binance binance = binanceRepository.findByOwnerId(id);
        if(binance != null) {
            secretKey = binance.getSecretKey();
            accessKey = binance.getAccessKey();
            return true;
        }else {
            return false;
        }
    }

    public String getOneCoinTradeLog(String coinName) throws IOException {
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

    public void makeCoinEntityAndSave(String entityString) throws ParseException {
        MemberCoin newData = null;
        if (!entityString.contains("code") && !(entityString).equals("[]")) {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(entityString);
            String id = request.getSession().getAttribute("userId").toString();
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
    }


    public String getAccountCoin() throws IOException {

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
            return getAccountCoin();

        } else if (entityString.contains("-1131")) {
            plusTime += 1000;
            return getAccountCoin();
        }
        return entityString;
    }

    public List<String> getMyCoinName() throws IOException, ParseException {
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

        JSONArray jsonArray = (JSONArray) jsonParser.parse(entityString);
        List<String> coinNames = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject)jsonArray.get(i);
            coinNames.add(jsonObject.get("asset").toString());
        }
        return coinNames;
    }

    @Override
    public List<LogDto> getAllLogDto() throws IOException, ParseException {
        List<String> coinNames = getMyCoinName();
        getDollar();
        List<LogDto> logList = new ArrayList<>();
        for (int i = 0; i < coinNames.size(); i++) {
            String entityString = getOneCoinTradeLog(coinNames.get(i));
            makeCoinEntityAndSave(entityString);
            if(!entityString.contains("code")){
                JSONParser jsonParser = new JSONParser();
                JSONArray jsonArray = (JSONArray) jsonParser.parse(entityString);
                List<LogDto> oneList = makeOneCoinLogList(jsonArray);
                logList.addAll(oneList);
            }
        }
        Collections.sort(logList, new LogTimeComparator());
        return logList;
    }

    public List<LogDto> makeOneCoinLogList(JSONArray jsonArray){
        List<LogDto> logList = new ArrayList<>();
        for(int i =0; i<jsonArray.size(); i++){
            LogDto logDto = new LogDto((JSONObject) jsonArray.get(i), oneDollarWon);
            logList.add(logDto);
        }
        return logList;
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

    public void getDollar () throws IOException, ParseException {
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
    }

    public Map<String, String> makeSumData(List<AccountDto> dtoList){
        Map<String, String> secondData = new HashMap<>();
        int totalBuyPrice = 0;
        int totalNowPrice = 0;
        int totalEarning = 0;

        for(AccountDto dto : dtoList){
            totalBuyPrice += (int) (dto.getBuyPrice() * dto.getOwnAmount());
            totalEarning += dto.getEarning();
            totalNowPrice += dto.getSumNowPrice();
        }
        double avgRate = (int)((double)totalEarning/totalBuyPrice * 10000d)/100d;
        secondData.put("totalBuyPrice", totalBuyPrice + "");
        secondData.put("totalNowPrice", totalNowPrice + "");
        secondData.put("totalEarning", totalEarning + "");
        secondData.put("avgRate", avgRate + "");
        return secondData;
    }

    @Override
    public List<AccountDto> getAccountList() throws IOException, ParseException {
        JSONArray jsonArray = (JSONArray) jsonParser.parse(getAccountCoin());
        String id = request.getSession().getAttribute("userId").toString();
        getDollar();
        double sumNowPriceWon;
        List<AccountDto> dtoList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String coinName = jsonObject.get("asset").toString();
            MemberCoin entity = memberCoinListRepository.findByCoinNameAndId(coinName, id);
            double coinPrice = getCoinPrice(coinName);
            double coinAmount = Double.parseDouble(jsonObject.get("free").toString());
            sumNowPriceWon = coinPrice * oneDollarWon * coinAmount;
            AccountDto dto = AccountDto.builder()
                    .coinName(coinName)
                    .bigAmount(""+BigDecimal.valueOf(coinAmount))
                    .bigNow(BigDecimal.valueOf(coinPrice * oneDollarWon) + "")
                    .nowPrice(coinPrice * oneDollarWon)
                    .ownAmount(coinAmount)
                    .sumNowPrice((int) sumNowPriceWon)
                    .trader("https://i.namu.wiki/i/Bf3cdzU4HpGUuQP0ZKI682ODWFFj6SBsfA-VL05m25ksKHjOHuHI9lKpg0ydpayw0J66lSqsxUd12acGFGNMnPnjjN8PgKBIbBDSumu09Yud2a42cpXV-Op-tZajllmChje-6s5QNucl-korqJEu2A.svg")
                    .build();
            if(entity != null){
                dto.setBuyPrice(entity.getAvg_buy_price());
                dto.setBigBuy(BigDecimal.valueOf(entity.getAvg_buy_price()) + "");
                dto.setSumBuyPrice(dto.getOwnAmount() * dto.getBuyPrice());
            }

            dto.setEarning((int) ((dto.getNowPrice() * dto.getOwnAmount()) - (dto.getBuyPrice() * dto.getOwnAmount())));
            double rate = (dto.getNowPrice() / dto.getBuyPrice() * 100d) - 100;
            if( (Math.round(rate * 100)) / 100d == 9.223372036854776E16){
                dto.setRateOfReturn(0);
            }else {
                dto.setRateOfReturn((Math.round(rate * 100)) / 100d);
            }
            dtoList.add(dto);
        }
        Collections.sort( dtoList, new CoinSumBuyPriceComparator());
        return dtoList;
    }

    public void saveWalletInfo(Map<String, List<String>> info){
        MemberCoin memberCoin;
        Iterator<String> coins = info.keySet().iterator();
        List<String> zeroList = new ArrayList<>();
        zeroList.add("0");
        zeroList.add("0");
        String id = request.getSession().getAttribute("userId").toString();
        while (coins.hasNext()){
            String coinName = coins.next();
            if(info.get(coinName).toString().equals("[, ]")){
                info.put(coinName, zeroList);
            }
            memberCoin = memberCoinListRepository.findByCoinNameAndId(coinName,id);
            if(memberCoin == null){
                memberCoin = MemberCoin.builder()
                        .coinName(coinName)
                        .owner_id(id)
                        .avg_buy_price(Double.parseDouble(info.get(coinName).get(0)))
                        .amount(Double.parseDouble(info.get(coinName).get(1)))
                        .build();
                memberCoinListRepository.save(memberCoin);
            }else {
                memberCoin = MemberCoin.builder()
                        .idx(memberCoin.getIdx())
                        .coinName(coinName)
                        .owner_id(id)
                        .avg_buy_price(Double.parseDouble(info.get(coinName).get(0)))
                        .mill_time(System.currentTimeMillis())
                        .amount(Double.parseDouble(info.get(coinName).get(1)))
                        .build();
                memberCoinListRepository.save(memberCoin);
            }
        }
    }
}