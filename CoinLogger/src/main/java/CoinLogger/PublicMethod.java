package CoinLogger;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PublicMethod {
    public MultiValueMap<String, String> jsonToMap(String json){
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>() {
        };
        Map<String, String> result = new HashMap<>();
        json = json.replaceAll("\\[","").replaceAll("]","");
        String[] list = json.split("}");
        for(int i = 0; i < list.length; i++){
            list[i] = list[i].replaceAll("\\{","")
                    .replaceAll("}","").replaceAll(" ","").replaceAll("\n","");
            String[] innerList = list[i].split(",");
            for(int j = 0; j< innerList.length; j++){
                if(innerList[j].equals("") || innerList[j].equals(",")){
                    continue;
                }
                String preMap[] = innerList[j].split(":");
                multiValueMap.add(preMap[0], preMap[1]);
            }
        }
        return multiValueMap;
    }

    public static void main(String[] args) {
        PublicMethod aa = new PublicMethod();
        MultiValueMap<String, String> dd = aa.jsonToMap("[\n" +
                "  {\n" +
                "    \"currency\":\"KRW\",\n" +
                "    \"balance\":\"1000000.0\",\n" +
                "    \"locked\":\"0.0\",\n" +
                "    \"avg_buy_price\":\"0\",\n" +
                "    \"avg_buy_price_modified\":false,\n" +
                "    \"unit_currency\": \"KRW\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"currency\":\"BTC\",\n" +
                "    \"balance\":\"2.0\",\n" +
                "    \"locked\":\"0.0\",\n" +
                "    \"avg_buy_price\":\"101000\",\n" +
                "    \"avg_buy_price_modified\":false,\n" +
                "    \"unit_currency\": \"KRW\",\n" +
                "  }\n" +
                "]");

    }
}
