package CoinLogger;

import CoinLogger.api.upbit.AccountDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PublicMethod {

    // 받아온 데이터를 List 형태로 변환
    public List<List<String>> jsonToList(String json){
        List<List<String>> resultList = new ArrayList<>();
        json = json.replaceAll("\\[","").replaceAll("]","");
        String[] list = json.replaceAll("\n","").split("}");
        for(int i = 0; i < list.length; i++){
            list[i] = list[i].replaceAll("\\{","")
                    .replaceAll("}","").replaceAll(" ","")
                    .replaceAll("\"","" );

            String [] partData = list[i].split(",");
            List<String> rowData = new ArrayList<>();
            for(int j =0; j< partData.length; j++){
                if( partData[j].equals("")){
                    continue;
                }
                rowData.add(partData[j].split(":")[1]);
            }
            resultList.add( rowData );
        }
        return resultList;
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
}
