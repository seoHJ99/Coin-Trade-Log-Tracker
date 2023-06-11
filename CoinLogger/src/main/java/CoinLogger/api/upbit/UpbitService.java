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
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UpbitService implements ApiService {

    private final HttpClient httpClient;
    private final HttpSender httpSender;
    private final PublicMethod publicMethod;

    // 계좌 정보
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

    // 코인 이름은 ,으로 구분
    @Override
    public String getNowPrice(String coinName)  {
        try {
            httpSender.setServerUrl("https://api.upbit.com");
            httpSender.setApiRequest("/v1/ticker?markets=%20" + coinName);
            String list = httpSender.sendApi();
            System.out.println(list);
            return list;
        }catch (IOException e){
            return "geNowPrice 오류";
        }
    }

    public void getOrders() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String accessKey = ("jaGJ8xxzTrxrqJcMPxwGxeAstH38fjXRAYNemMal");
        String secretKey = ("N8qFgecKx5B9s7HJJHexrADEDlT2znPSgTYENQCD");
        String serverUrl = ("https://api.upbit.com");

        HashMap<String, String> params = new HashMap<>();
        params.put("state", "done");

//        String[] uuids = {
//                "9ca023a5-851b-4fec-9f0a-48cd83c2eaae"
//                // ...
//        };

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }
//        for(String uuid : uuids) {
//            queryElements.add("uuids[]=" + uuid);
//        }

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

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(serverUrl + "/v1/orders?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 위의 getOrders 에서 얻은 uuid나 identifier 가 반드시 필요함
    public void getOrder(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String accessKey = ("jaGJ8xxzTrxrqJcMPxwGxeAstH38fjXRAYNemMal");
        String secretKey = ("N8qFgecKx5B9s7HJJHexrADEDlT2znPSgTYENQCD");
        String serverUrl = ("https://api.upbit.com");

        HashMap<String, String> params = new HashMap<>();
        params.put("uuid", "9ca023a5-851b-4fec-9f0a-48cd83c2eaae");

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

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

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(serverUrl + "/v1/order?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
