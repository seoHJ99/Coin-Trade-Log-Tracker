package CoinLogger.api.binance;


import CoinLogger.api.upbit.AccountDto;
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

    @GetMapping("/binance/{id}/account")
    public String getAccount(Model model) throws IOException, ParseException {
        List<AccountDto> dtoList = binanceService.accountDtoMaker();
        Map<String, String> secondData = binanceService.makeSumData(dtoList);
        model.addAttribute("data", dtoList);
        model.addAttribute("secondData", secondData);
        return "AccountsListPage";
    }

    @GetMapping("/binance/wallet")
    public String makeAvgPriceAndSave(Model model) throws IOException, ParseException {
        List<String> coinName=binanceService.getMyCoinName();
        model.addAttribute("coinName", coinName);
        return "binanceCoin";
    }

    @GetMapping("/binance/{id}/all-trade-log")
    public String getLog(Model model) throws ParseException, IOException {
        model.addAttribute("log", binanceService.getAllCoinLog());
        return "LogListPage";
    }

    @PostMapping("/test")
    @ResponseBody
    public String saveWalletInfo(@RequestBody Map data){
        binanceService.saveWalletInfo(data);
        return "1";
    }
}
