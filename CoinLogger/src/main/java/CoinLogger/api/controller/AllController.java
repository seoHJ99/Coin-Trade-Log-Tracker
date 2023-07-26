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
    public Object getAllAccount(Model model) throws IOException, ParseException {
        List<AccountDto> allAccount = new ArrayList<>();
            if (coinoneService.getKeys()) {
                List<AccountDto> accountList = coinoneService.getAccountList();
                if(accountList.size() == 1 && accountList.get(0).getCoinName().contains("error")){
                    return ResponseEntity.ok("<script>alert('"+ accountList.get(0).getCoinName()+" ')</script>");
                }
                allAccount.addAll(accountList);
            }
            if (binanceService.getKeys()) {
                allAccount.addAll(binanceService.getAccountList());
            }
            if (upbitService.getKeys()) {
                List<AccountDto> accountList = upbitService.getAccountList();
                if(accountList.size() == 1 && accountList.get(0).getCoinName().contains("error")){
                    return ResponseEntity.ok( "<script>alert('"+ accountList.get(0).getCoinName()+" ')</script>");
                }
                allAccount.addAll(accountList);
            }

        Collections.sort(allAccount, new CoinSumBuyPriceComparator());
        Map<String, String> secondData = publicMethod.makeSumData(allAccount);
        model.addAttribute("data", allAccount);
        model.addAttribute("secondData", secondData);
        return "AccountsListPage";
    }

    @GetMapping("/all/all-trade-log")
    public Object getAllLog(Model model) throws ParseException {
        List<LogDto> allLog = new ArrayList<>();

            if (coinoneService.getKeys()) {
                List<LogDto> coinoneDto = coinoneService.getAllLogDto();
                if(coinoneDto.size() == 1 && coinoneDto.get(0).getState().contains("error")){
                    return ResponseEntity.ok("<script>" +
                            "alert('"+coinoneDto.get(0).getState() + "');" +
                            "</script>");
                }
                allLog.addAll(coinoneDto);
            }
            if (binanceService.getKeys()) {
                List<LogDto> binanceDto = binanceService.getAllLogDto();
                if(binanceDto.size() == 1 && binanceDto.get(0).getState().contains("error")){
                    return ResponseEntity.ok("<script>" +
                            "alert('"+binanceDto.get(0).getState() + "');" +
                            "</script>");
                }
                allLog.addAll(binanceDto);
            }
            if (upbitService.getKeys()) {
                List<LogDto> upbitDto = upbitService.getAllLogDto();
                if(upbitDto.size() == 1 && upbitDto.get(0).getState().contains("error")){
                    return ResponseEntity.ok("<script>" +
                            "alert('"+upbitDto.get(0).getState() + "');" +
                            "</script>");
                }
                allLog.addAll(upbitDto);
            }
            Collections.sort(allLog, new LogTimeComparator());
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

    @GetMapping("/login/error")
    @ResponseBody
    public String findIdPage(){
        return "<script>alert('없는 계정입니다.'); location.href='/login'</script>";
    }
}
