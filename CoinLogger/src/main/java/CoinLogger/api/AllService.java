package CoinLogger.api;

import CoinLogger.api.binance.Binance;
import CoinLogger.api.binance.BinanceRepository;
import CoinLogger.api.coinone.Coinone;
import CoinLogger.api.coinone.CoinoneRepository;
import CoinLogger.api.upbit.Upbit;
import CoinLogger.api.upbit.UpbitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AllService {
    private final UpbitRepository upbitRepository;
    private final CoinoneRepository coinoneRepository;
    private final BinanceRepository binanceRepository;

    public void saveKeys(Map<String, String[]> data){
        String[] upbitKey = data.get("upbit");
        String[] coinoneKey = data.get("coinone");
        String[] binanceKey = data.get("binance");
        // id는 따로
        String id = "test2222";
        Upbit upbit = Upbit.builder()
                .ownerId(id)
                .accessKey(upbitKey[0])
                .secretKey(upbitKey[1])
                .build();
        Coinone coinone = Coinone.builder()
                .ownerId(id)
                .accessKey(coinoneKey[0])
                .secretKey(coinoneKey[1])
                .build();
        Binance binance = Binance.builder()
                .ownerId(id)
                .accessKey(binanceKey[0])
                .secretKey(binanceKey[1])
                .build();
        Upbit upEntity = upbitRepository.findByOwnerId(id);
        Binance biEntity= binanceRepository.findByOwnerId(id);
        Coinone coEntity = coinoneRepository.findByOwnerId(id);
        if(upEntity != null){
            upbit.setIdx(upEntity.getIdx());
        }
        if(biEntity != null){
            binance.setIdx(biEntity.getIdx());
        }
        if(coEntity != null){
            coinone.setIdx(coEntity.getIdx());
        }
        upbitRepository.save(upbit);
        binanceRepository.save(binance);
        coinoneRepository.save(coinone);
    }
}
