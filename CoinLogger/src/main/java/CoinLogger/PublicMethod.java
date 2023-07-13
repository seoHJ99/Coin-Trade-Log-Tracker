package CoinLogger;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

}
