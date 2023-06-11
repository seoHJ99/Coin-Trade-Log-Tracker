package CoinLogger;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PublicMethod {
    public Map<String, List<String>> jsonToMap(String json){
        Map<String, List<String >> multiValueMap = new HashMap<>();
        List<String> valueList = new ArrayList<>();

        json = json.replaceAll("\\[","").replaceAll("]","");
        String[] list = json.split("}");
        for(int i = 0; i < list.length; i++){
            list[i] = list[i].replaceAll("\\{","")
                    .replaceAll("}","").replaceAll(" ","")
                    .replaceAll("\n","").replaceAll("\"","" );
            String[] innerList = list[i].split(",");
            for(int j = 0; j< innerList.length; j++){
                if(innerList[j].equals("") || innerList[j].equals(",")){
                    continue;
                }
                String preMap[] = innerList[j].split(":");
                valueList.add(preMap[1]);

            }
        }
        return multiValueMap;
    }
}
