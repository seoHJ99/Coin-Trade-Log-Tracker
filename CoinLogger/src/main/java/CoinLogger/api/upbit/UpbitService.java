package CoinLogger.api.upbit;


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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UpbitService implements ApiService {
    private final JSONParser jsonParser;
    private final HttpSender httpSender;
    private final PublicMethod publicMethod;

    // 계좌 정보
    //         1              2                  3         4            5              6
    // 순서 : 이름, 보유 수량 중 주문 가능한 수량, 묶여있는 수량, 매수평균가, 매수평균가 수정여부, 평단가 기준 화폐
    @Override
    public List<List<String>> getAccounts() throws IOException {
        String accessKey = ("jaGJ8xxzTrxrqJcMPxwGxeAstH38fjXRAYNemMal"); // 받아오기
        String secretKey = ("N8qFgecKx5B9s7HJJHexrADEDlT2znPSgTYENQCD"); // 받아오기

        httpSender.setServerUrl("https://api.upbit.com");
        httpSender.setApiRequest("/v1/accounts");

        String responseJson = httpSender.sendApi(accessKey, secretKey);
        List<List<String>>  result = publicMethod.jsonToList(responseJson);
        return result;
    }



    // 코인 이름 리스트
    public List<List<String>> getCoinList() {
        List<List<String>> result = null;
        try {
            httpSender.setServerUrl("https://api.upbit.com");
            httpSender.setApiRequest("/v1/market/all?isDetails=false");
            String list = httpSender.sendApi();
            System.out.println(list);
            result = publicMethod.jsonToList(list);

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
        List<List<String>> accounts = getAccounts();
        List<String> result = new ArrayList<>();
        String coinPrice = "";
        for(int i =0; i<accounts.size(); i++){
            String coinName = accounts.get(i).get(0);
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
    public List<List<String>> getOrders() throws NoSuchAlgorithmException, UnsupportedEncodingException {
//        String accessKey = ("jaGJ8xxzTrxrqJcMPxwGxeAstH38fjXRAYNemMal");
//        String secretKey = ("N8qFgecKx5B9s7HJJHexrADEDlT2znPSgTYENQCD");
        String serverUrl = ("https://api.upbit.com");
        String accessKey = ("jaGJ8xxzTrxrqJcMPxwGxeAstH38fjXRAYNemMal"); // 받아오기
        String secretKey = ("N8qFgecKx5B9s7HJJHexrADEDlT2znPSgTYENQCD"); // 받아오기

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
        return publicMethod.jsonToList(result);
    }



    public List< AccountDto_Upbit > accountDtoMaker() throws IOException, ParseException {
        List<AccountDto_Upbit> result = new ArrayList<>();
        List<List<String>> accounts = getAccounts();
        List<String> myCoinPrice = getMyCoinPrice();
        for(int i =0; i< myCoinPrice.size(); i++){
            AccountDto_Upbit oneData = AccountDto_Upbit.builder().coinName(accounts.get(i).get(0))
                    .ownAmount(Double.valueOf(accounts.get(i).get(1)) + Double.valueOf(accounts.get(i).get(2)))
                    .buyPrice(Double.valueOf( accounts.get(i).get(3)))
                    .nowPrice( Math.floor( Double.valueOf( myCoinPrice.get(i))))
                    .build();
            oneData.setSumNowPrice( (int)( Double.valueOf(oneData.getNowPrice()) * oneData.getOwnAmount() ));
            oneData.setEarning( (int)( (oneData.getNowPrice() * oneData.getOwnAmount()) - (oneData.getBuyPrice() * oneData.getOwnAmount())));
            double rate = (double) oneData.getEarning() / (oneData.getSumNowPrice() - oneData.getEarning()) * 100d;
            oneData.setRateOfReturn( (Math.round(rate*100))/100d );
            result.add(oneData);
        }
        return result;
    }

}
