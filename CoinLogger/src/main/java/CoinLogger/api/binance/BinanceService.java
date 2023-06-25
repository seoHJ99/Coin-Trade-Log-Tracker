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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BinanceService {
    private final HttpClient httpClient;
    private final JSONParser jsonParser;
    private final PublicMethod publicMethod;
    private static double oneDollarWon;
    String secretKey = "";
    String accessKey = "";
    String serverUrl = "https://api.binance.com";
    public List<List<String>> getAccountCoin() throws IOException, ParseException {

        HmacSignatureGenerator signature = new HmacSignatureGenerator(secretKey);

        String timestamp = Long.toString(System.currentTimeMillis());
        String queryString = "timestamp=" + timestamp;
        String actualSign = signature.getSignature(queryString);

        HttpPost request = new HttpPost(serverUrl + "/sapi/v3/asset/getUserAsset?" + queryString + "&signature="+actualSign);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-MBX-APIKEY", accessKey);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();;

        String entityString = EntityUtils.toString(entity, "UTF-8");
        List<List<String>> result = publicMethod.jsonToList(entityString);
        System.out.println(entityString);
        return result;
    }

//
//    public double getBTCValue() throws IOException, ParseException {
//        HttpGet request = new HttpGet(serverUrl + "/api/v3/avgPrice?symbol=BTCUSDT");
//        request.addHeader("Content-Type", "application/json");
//        request.addHeader("X-MBX-APIKEY", accessKey);
//        HttpResponse response = httpClient.execute(request);
//        HttpEntity entity = response.getEntity();
//        String result = EntityUtils.toString(entity, "UTF-8");
//        JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
//        jsonObject = new JSONObject(jsonObject);
//        return Double.valueOf((String) jsonObject.get("price"));
//    }
    // 코인 이름으로 차트 조회 돌아야 할듯.
    // 계좌 정보 가져오기는 실시간 변동이 안됨

   public void coinBTCValueToWon(List<List<String>> myCoin) throws IOException, ParseException {
        if(oneDollarWon == 0){
            HttpGet request = new HttpGet("https://quotation-api-cdn.dunamu.com/v1/forex/recent?codes=FRX.KRWUSD");
            request.addHeader("Content-Type", "application/json");
            request.addHeader("X-MBX-APIKEY", accessKey);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            JSONArray jsonObject = (JSONArray) jsonParser.parse(result);
            JSONObject a = (JSONObject)jsonObject.get(0);
            oneDollarWon = (double) a.get("basePrice");
        }
//        double btc = getBTCValue();
        for(int i =0; i<myCoin.size(); i++){
            double coinBtcValue = Double.valueOf( myCoin.get(i).get(6));
            System.out.println(myCoin.get(i).get(0));
            System.out.println("coinBtcValue = " + coinBtcValue);
//            double price = btc  * coinBtcValue;
//            System.out.println("price = " + price);

        }
        return ;
   }
}
