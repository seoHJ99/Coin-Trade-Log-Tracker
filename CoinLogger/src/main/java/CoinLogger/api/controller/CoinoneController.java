package CoinLogger.api.controller;

import CoinLogger.api.other.PublicMethod;
import CoinLogger.api.service.CoinoneService;
import CoinLogger.api.dto.AccountDto;
import CoinLogger.api.dto.LogDto;
import lombok.*;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class CoinoneController {

    private final PublicMethod publicMethod;
    private final CoinoneService coinoneService;

    @GetMapping("/coinone/account")
    public Object getAccount(Model model) throws ParseException {
        if(coinoneService.getKeys()) {
            List<AccountDto> dtoList = coinoneService.getAccountList();
            if(dtoList.size() == 1 && dtoList.get(0).getCoinName().contains("error")){
                return ResponseEntity.ok( "<script>alert('"+ dtoList.get(0).getCoinName()+" ')</script>");
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

    @GetMapping("/coinone/all-trade-log")
    public Object getLog(Model model) throws ParseException {
        if(coinoneService.getKeys()) {
            List<LogDto> result = coinoneService.getAllLogDto();
            if(result.size() == 1 && result.get(0).getState().contains("error")){
                return ResponseEntity.ok("<script>" +
                        "alert('"+result.get(0).getState() + "');" +
                        "history.back();" +
                        "</script>");
            }
            model.addAttribute("log", result);
        }else {
            model.addAttribute("log", null);
        }
        return "LogListPage";
    }
}
