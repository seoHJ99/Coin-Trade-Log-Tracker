package CoinLogger.api.upbit;


import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final UpbitService upbit;
    @GetMapping("/upbit/{id}/accounts")
    public String getAllAccounts(Model model) throws IOException, ParseException {
        List<AccountDto> dtoList = upbit.accountDtoMaker();
        Map<String, String> secondData = upbit.makeSumData(dtoList);

        model.addAttribute( "data", dtoList);
        model.addAttribute("secondData", secondData);
        return "AccountsListPage";
    }

    @GetMapping("upbit/{id}/all-trade-log")
    public String getTradeLog(Model model) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        List<LogDto_Upbit> logList = new ArrayList<>();
        List<List<String>> allLog = upbit.getOrders();
         for(int i =0; i<allLog.size(); i++){
             LogDto_Upbit logDto = LogDto_Upbit.builder()
//                     .orderTime(allLog.get(i).get(6))
                     .coinName(allLog.get(i).get(5))
                     .thatTimePrice(allLog.get(i).get(3))
                     .orderAmount(allLog.get(i).get(7))
                     .signedAmount(allLog.get(i).get(13))
                     .remainAmount(allLog.get(i).get(8))
                     .trader("https://files.readme.io/40e45a0-small-upbit_color.png")
                     .build();
             if(allLog.get(i).get(1).equals("bid")){
                 logDto.setOrderSort("매수");
             }else {
                 logDto.setOrderSort("매도");
             }
             if(allLog.get(i).get(4).equals("done")){
                 logDto.setState("체결");
             }else {
                 logDto.setState("취소");
             }
             logList.add(logDto);
         }
         model.addAttribute("log", logList);
         return "LogListPage";
    }
}
