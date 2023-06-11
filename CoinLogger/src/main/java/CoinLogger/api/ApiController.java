package CoinLogger.api;


import CoinLogger.api.upbit.UpbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Controller
@RequiredArgsConstructor
public class ApiController {
    private final UpbitService upbit;
    @GetMapping("/upbit/{id}/all-accounts")
    public String getAllAccounts(Model model) throws IOException, NoSuchAlgorithmException {
//        upbit.getCoinList();
        upbit.getNowPrice("KRW-STEEM");
//        upbit.getOrders();
        model.addAttribute("Data" ,upbit.getAccounts());
        return "/ListPage";
    }

}
