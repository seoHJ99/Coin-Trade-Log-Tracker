package CoinLogger.api.upbit;


import CoinLogger.CoinSumBuyPriceComparator;
import CoinLogger.PublicMethod;
import CoinLogger.api.ApiService;
import CoinLogger.api.HttpSender;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UpbitService implements ApiService {
    private final HttpClient httpClient;
    private final JSONParser jsonParser;
    private final HttpSender httpSender;
    private final PublicMethod publicMethod;
    private final UpbitRepository upbitRepository;
    private String accessKey; // 받아오기
    private String secretKey; // 받아오기

    public boolean getKeys(){
        //id는 나중에
        String id = "test2222";
        //
        Upbit upbit = upbitRepository.findByOwnerId(id);
        if(upbit != null){
            accessKey = upbit.getAccessKey();
            secretKey = upbit.getSecretKey();
            return true;
        }else {
            return false;
        }
    }

    // 계좌 정보
    //         1              2                  3         4            5              6
    // 순서 : 이름, 보유 수량 중 주문 가능한 수량, 묶여있는 수량, 매수평균가, 매수평균가 수정여부, 평단가 기준 화폐
    @Override
    public String getAccounts() throws IOException {
        httpSender.setServerUrl("https://api.upbit.com");
        httpSender.setApiRequest("/v1/accounts");

        String responseJson = httpSender.sendApi(accessKey, secretKey);
        return responseJson;
    }

    // 코인 이름 리스트
    public String getCoinList() {
        String result = "";
        try {
            httpSender.setServerUrl("https://api.upbit.com");
            httpSender.setApiRequest("/v1/market/all?isDetails=false");
            result = httpSender.sendApi();

        }catch (IOException e){
            System.out.println(e);
            System.out.println("getCoinList 오류");
        }
        return result;
    }

    // 코인 하나 가격
    @Override
    public String getNowPrice(String coinName)  {
        try {
            httpSender.setServerUrl("https://api.upbit.com");
            httpSender.setApiRequest("/v1/ticker?markets=%20" + coinName);
            String list = httpSender.sendApi();
            return list;
        }catch (IOException e){
            return "geNowPrice 오류";
        }
    }

    // 내 모든 코인 가격
    public List<String> getMyCoinPrice() throws IOException, ParseException {
        JSONArray jsonArray = (JSONArray) jsonParser.parse(getAccounts());
        List<String> result = new ArrayList<>();
        String coinPrice = "";
        for(int i =0; i<jsonArray.size(); i++){
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String coinName = jsonObject.get("currency").toString();
            if(coinName.equals("KRW")){
                result.add("0");
                continue;
            }
            coinPrice = getNowPrice("KRW-" + coinName);
            coinPrice = coinPrice.replaceAll("\\[","").replaceAll("]","");
            if(coinPrice.contains("error")){
                coinPrice = "0";
            }else {
                JSONObject jsonObj = (JSONObject) jsonParser.parse(coinPrice);
                String token =  jsonObj.get("trade_price").toString();
                coinPrice = token;
            }
            result.add(coinPrice);
        }
        return result;
    }


    // 주문 내역 가져오기( 체결, 취소 )
    public String getOrders() throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String serverUrl = ("https://api.upbit.com");

        ArrayList<String> queryElements = new ArrayList<>();

        queryElements.add("states[]=done,cancel" );

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;
        String result = null;
        try {
            HttpGet request = new HttpGet(serverUrl + "/v1/orders?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, String> makeSumData(List<AccountDto> dtoList){
        Map<String, String> secondData = new HashMap<>();
        int totalBuyPrice = 0;
        int totalNowPrice = 0;
        int totalEarning = 0;
        double avgRate = 0;
        for(AccountDto dto : dtoList){
            totalBuyPrice += (int) (dto.getBuyPrice() * dto.getOwnAmount());
            totalEarning += dto.getEarning();
            totalNowPrice += dto.getSumNowPrice();
        }
        avgRate = (int)((double)totalEarning/totalBuyPrice * 10000d)/100d;
        secondData.put("totalBuyPrice", totalBuyPrice + "");
        secondData.put("totalNowPrice", totalNowPrice + "");
        secondData.put("totalEarning", totalEarning + "");
        secondData.put("avgRate", avgRate + "");
        return secondData;
    }

    public List<AccountDto> accountDtoMaker() throws IOException, ParseException {
        List<AccountDto> result = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(getAccounts());
        List<String> myCoinPrice = getMyCoinPrice();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            AccountDto oneData = null;
            String coin = jsonObject.get("currency").toString();
            double amount = Double.parseDouble(jsonObject.get("balance").toString()) + Double.parseDouble(jsonObject.get("locked").toString());
            double buyPrice = Double.parseDouble(jsonObject.get("avg_buy_price").toString());
            double price = Double.parseDouble(myCoinPrice.get(i));
            if (amount == 0) {
                continue;
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
                        .build();
            } else {
                oneData = AccountDto.builder()
                        .coinName(coin)
                        .ownAmount(amount)
                        .sumBuyPrice(buyPrice * amount)
                        .bigAmount(BigDecimal.valueOf(amount).toPlainString())
                        .buyPrice(buyPrice)
                        .bigBuy(BigDecimal.valueOf(buyPrice).toPlainString())
                        .nowPrice(price)
                        .bigNow(BigDecimal.valueOf(price).toPlainString())
                        .build();
            }
            oneData.setTrader("https://files.readme.io/40e45a0-small-upbit_color.png");
            oneData.setSumNowPrice((int) (Double.valueOf(oneData.getNowPrice()) * oneData.getOwnAmount()));
            oneData.setEarning((int) ((oneData.getNowPrice() * oneData.getOwnAmount()) - (oneData.getBuyPrice() * oneData.getOwnAmount())));
            double rate = (oneData.getNowPrice() / oneData.getBuyPrice() * 100d) - 100;
            oneData.setRateOfReturn((Math.round(rate * 100)) / 100d);
            result.add(oneData);
        }
        Collections.sort( result, new CoinSumBuyPriceComparator());
        return result;
    }

    public List<LogDto> makeLogList() throws UnsupportedEncodingException, NoSuchAlgorithmException, ParseException {
        List<LogDto> logList = new ArrayList<>();
        String allLog = getOrders();
        System.out.println("allLog = " + allLog);
        String[] a = allLog.replaceAll("\\[","").replaceAll("]","").split("},");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        for(int i =0; i<a.length; i++){
            if(i < a.length-1){
                a[i] = a[i] + "}";
            }
            JSONObject jsonObject = (JSONObject) jsonParser.parse(a[i]);
            LogDto logDto = LogDto.builder()
                    .state((String) jsonObject.get("state"))
                    .remainAmount((String) jsonObject.get("remaining_volume"))
                    .signedAmount((String) jsonObject.get("executed_volume"))
                    .orderAmount((String) jsonObject.get("volume"))
                    .thatTimePrice((String) jsonObject.get("price"))
                    .coinName((String) jsonObject.get("market"))
                    .orderTime(LocalDateTime.parse( jsonObject.get("created_at").toString(), formatter))
                    .trader("https://files.readme.io/40e45a0-small-upbit_color.png")
                    .build();
            if(jsonObject.get("side").equals("bid")){
                logDto.setOrderSort("매수");
            }else {
                logDto.setOrderSort("매도");
            }
            if(jsonObject.get("state").equals("done")){
                logDto.setState("체결");
            }else {
                logDto.setState("취소");
            }
            logList.add(logDto);
        }
        return logList;
    }

}
