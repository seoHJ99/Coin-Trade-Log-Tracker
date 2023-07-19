package CoinLogger.api.controller;

import CoinLogger.api.comparator.CoinSumBuyPriceComparator;
import CoinLogger.api.comparator.LogTimeComparator;
import CoinLogger.api.other.PublicMethod;
import CoinLogger.api.service.AllService;
import CoinLogger.api.service.BinanceService;
import CoinLogger.api.service.CoinoneService;
import CoinLogger.api.dto.AccountDto;
import CoinLogger.api.dto.LogDto;
import CoinLogger.api.service.UpbitService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RequiredArgsConstructor
@Controller
public class AllController {
    private final BinanceService binanceService;
    private final UpbitService upbitService;
    private final CoinoneService coinoneService;
    private final PublicMethod publicMethod;
    private final AllService allService;

    @GetMapping("/all/account")
    public Object getAllAccount(Model model) {
        List<AccountDto> allAccount = new ArrayList<>();
        try {
            if (coinoneService.getKeys()) {
                allAccount.addAll(coinoneService.getAccountList());
            }
            if (binanceService.getKeys()) {
                allAccount.addAll(binanceService.getAccountList());
            }
            if (upbitService.getKeys()) {
                allAccount.addAll(upbitService.getAccountList());
            }
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("통신 오류");
            return ResponseEntity.ok("<script>alert('error: 통신오류!<br/> 개발자에게 연락하세요')</script>");
        }catch (ParseException e){
            e.printStackTrace();
            System.out.println("파싱 오류");
            return ResponseEntity.ok("<script>alert('error: 파싱오류!<br/> 개발자에게 연락하세요')</script>");
        }
        Collections.sort(allAccount, new CoinSumBuyPriceComparator());
        Map<String, String> secondData = publicMethod.makeSumData(allAccount);
        model.addAttribute("data", allAccount);
        model.addAttribute("secondData", secondData);
        return "AccountsListPage";
    }

    @GetMapping("/all/all-trade-log")
    public Object getAllLog(Model model)  {
        List<LogDto> allLog = new ArrayList<>();
        List<LogDto> result = new ArrayList<>();
        try {
            if (coinoneService.getKeys()) {
                result = coinoneService.getAllLogDto();
                if(result.size() == 1 && result.get(0).getState().contains("error")){
                    return ResponseEntity.ok("<script>" +
                            "alert('"+result.get(0).getState() + "');" +
                            "</script>");
                }
                allLog.addAll(result);
            }
            if (binanceService.getKeys()) {
                List<LogDto> allLogDto = binanceService.getAllLogDto();
                if(allLogDto.size() == 1 && allLogDto.get(0).getState().contains("error")){
                    return ResponseEntity.ok("<script>" +
                            "alert('"+allLogDto.get(0).getState() + "');" +
                            "</script>");
                }
                allLog.addAll(binanceService.getAllLogDto());
            }
            if (upbitService.getKeys()) {
                List<LogDto> allLogDto = upbitService.getAllLogDto();
                if(allLogDto.size() == 1 && allLogDto.get(0).getState().contains("error")){
                    return ResponseEntity.ok("<script>" +
                            "alert('"+allLogDto.get(0).getState() + "');" +
                            "</script>");
                }else {
                    model.addAttribute("log", allLogDto);
                }
            }
            Collections.sort(allLog, new LogTimeComparator());
        }catch (ParseException parseException){
            System.out.println("Json 파싱 오류");
            return ResponseEntity.ok("<script>alert('error: 파싱오류!<br/> 개발자에게 연락하세요')</script>");
        }
        model.addAttribute("log", allLog);
        return "LogListPage";
    }

    @GetMapping("/myinfo/keys")
    public String myApiKeys(Model model){
        model.addAttribute("keys", allService.getApiKeys());
        return "MyInfo";
    }

    @PostMapping("/api/key")
    @ResponseBody
    public String saveKeys(@RequestBody Map<String, String[]> data){
        allService.saveKeys(data);
        return "";
    }

    @GetMapping("/login")
    public String loginView(){
        return "LoginPage";
    }

    @GetMapping("/join")
    public String joinForm(){
        return "JoinPage";
    }

    @PostMapping("/member/join")
    @ResponseBody
    public int memberJoin(HttpServletRequest request){ // 원인 불명의 이유로 map형태로 데이터를 못받아옴
        allService.saveMember(request);
        return 1;
    }

    @PostMapping("/id/check")
    @ResponseBody
    public int checkDuplicate( Map<String, String> data){
        boolean duplication = allService.checkDuplicate(data);
        if(duplication == true){
            return 0;
        }else {
            return 1;
        }
    }
}
