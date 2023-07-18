package CoinLogger.api.controller;


import CoinLogger.api.other.PublicMethod;
import CoinLogger.api.service.BinanceService;
import CoinLogger.api.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class BinanceController {
    private final BinanceService binanceService;
    private final PublicMethod publicMethod;

    @GetMapping("/binance/account")
    public String getAccount(Model model) throws IOException, ParseException {
        if(binanceService.getKeys()){
            List<AccountDto> dtoList = binanceService.accountDtoMaker();
            Map<String, String> secondData = publicMethod.makeSumData(dtoList);
            model.addAttribute("data", dtoList);
            model.addAttribute("secondData", secondData);
        }else {
            model.addAttribute("data", null);
            model.addAttribute("secondData", null);
        }

        return "AccountsListPage";
    }

    @GetMapping("/binance/wallet")
    public String makeAvgPriceAndSave(Model model) throws IOException, ParseException {
        if(binanceService.getKeys()) {
            List<String> coinName = binanceService.getMyCoinName();
            model.addAttribute("coinName", coinName);
        }else {
            model.addAttribute("coinName", null);
        }
        return "binanceCoin";
    }

    @GetMapping("/binance/all-trade-log")
    public String getLog(Model model) throws ParseException, IOException {
        if(binanceService.getKeys()) {
            model.addAttribute("log", binanceService.getAllCoinLog());
            model.addAttribute("binance");
        }else {
            model.addAttribute("log", null);
        }
        return "LogListPage";
    }

    @PostMapping("/test")
    @ResponseBody
    public String saveWalletInfo(@RequestBody Map data){
        binanceService.saveWalletInfo(data);
        return "1";
    }
}
