package CoinLogger.api.controller;


import CoinLogger.api.other.PublicMethod;
import CoinLogger.api.service.BinanceService;
import CoinLogger.api.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class BinanceController {
    private final BinanceService binanceService;
    private final PublicMethod publicMethod;

    @GetMapping("/binance/account")
    public Object getAccount(Model model) {
        if (binanceService.getKeys()) {
            List<AccountDto> dtoList = new ArrayList<>();
            try {
                dtoList = binanceService.getAccountList();
            } catch (IOException ioException) {
                System.out.println("통신오류");
                return ResponseEntity.ok("<script>alert('error: 통신오류!<br/> 개발자에게 연락하세요')</script>");
            } catch (ParseException parseException) {
                System.out.println("Json 파싱 오류");
                return ResponseEntity.ok("<script>alert('error: 파싱오류!<br/> 개발자에게 연락하세요')</script>");
            }
            Map<String, String> secondData = publicMethod.makeSumData(dtoList);
            model.addAttribute("data", dtoList);
            model.addAttribute("secondData", secondData);
        } else {
            model.addAttribute("data", null);
            model.addAttribute("secondData", null);
        }

        return "AccountsListPage";
    }

    @GetMapping("/binance/wallet")
    public Object makeAvgPriceAndSave(Model model) {
        List<String> coinName = new ArrayList<>();
        if (binanceService.getKeys()) {
            try {
               coinName = binanceService.getMyCoinName();

            } catch (ParseException parseException) {
                System.out.println("Json 파싱 오류");
                return ResponseEntity.ok("<script>alert('error: 파싱오류!<br/> 개발자에게 연락하세요')</script>");
            }
            model.addAttribute("coinName", coinName);
        } else {
            model.addAttribute("coinName", null);
        }

        return "binanceCoin";
    }

    @GetMapping("/binance/all-trade-log")
    public Object getLog(Model model) {
        try {
            if (binanceService.getKeys()) {
                model.addAttribute("log", binanceService.getAllLogDto());
                model.addAttribute("binance");
            } else {
                model.addAttribute("log", null);
            }
        } catch (ParseException parseException) {
            System.out.println("Json 파싱 오류");
            return ResponseEntity.ok("<script>alert('error: 파싱오류!<br/> 개발자에게 연락하세요')</script>");
        }
        return "LogListPage";
    }

    @PostMapping("/test")
    @ResponseBody
    public String saveWalletInfo(@RequestBody Map data) {
        binanceService.saveWalletInfo(data);
        return "1";
    }
}
