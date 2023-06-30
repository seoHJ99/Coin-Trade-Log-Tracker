package CoinLogger.api.binance;

import lombok.RequiredArgsConstructor;

import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;



@Controller
@RequiredArgsConstructor
public class BinanceController {
    private final BinanceService binanceService;

    @GetMapping("/binance/{id}/account")
    public String getAccount(Model model) throws IOException, ParseException {
        binanceService.makeAccountDto();
        binanceService.getAllCoinLog();
        return "AccountsListPage";
    }
}
