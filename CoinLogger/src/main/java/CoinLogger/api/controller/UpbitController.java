package CoinLogger.api.controller;


import CoinLogger.api.dto.AccountDto;
import CoinLogger.api.dto.LogDto;
import CoinLogger.api.other.PublicMethod;
import CoinLogger.api.service.UpbitService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UpbitController {
    private final PublicMethod publicMethod;
    private final UpbitService upbit;
    @GetMapping("/upbit/account")
    public Object getAllAccounts(Model model) throws IOException, ParseException {
        if(upbit.getKeys()) {
            List<AccountDto> dtoList = upbit.getAccountList();
            if(dtoList.size() == 1 && dtoList.get(0).getCoinName().contains("error")){
                return ResponseEntity.ok("<script>alert('"+ dtoList.get(0).getCoinName()+" ')</script>");
            }
            Map<String, String> secondData = publicMethod.makeSumData(dtoList);
            model.addAttribute("data", dtoList);
            model.addAttribute("secondData", secondData);
        }else {
            model.addAttribute("data", null);
            model.addAttribute("secondData", null);
        }
        return "AccountsListPage";
    }

    @GetMapping("upbit/all-trade-log")
    public Object getTradeLog(Model model) throws UnsupportedEncodingException, NoSuchAlgorithmException, ParseException {
        if(upbit.getKeys()) {
            List<LogDto> allLogDto = upbit.getAllLogDto();
            if(allLogDto.size() == 1 && allLogDto.get(0).getState().contains("error")){
                return ResponseEntity.ok("<script>" +
                        "alert('"+allLogDto.get(0).getState() + "');" +
                        "</script>");
            }else {
                model.addAttribute("log", allLogDto);
            }
        }else {
            model.addAttribute("log", null);
        }
         return "LogListPage";
    }
}
