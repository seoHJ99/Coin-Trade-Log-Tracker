package CoinLogger.api.service;

import CoinLogger.api.dto.ApiDto;
import CoinLogger.api.entity.Binance;
import CoinLogger.api.entity.BinanceRepository;
import CoinLogger.api.entity.Coinone;
import CoinLogger.api.entity.CoinoneRepository;
import CoinLogger.api.entity.Member;
import CoinLogger.api.entity.MemberRepository;
import CoinLogger.api.entity.Upbit;
import CoinLogger.api.entity.UpbitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AllService {
    private final UpbitRepository upbitRepository;
    private final CoinoneRepository coinoneRepository;
    private final BinanceRepository binanceRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private HttpServletRequest request;

    public void saveKeys(Map<String, String[]> data){
        String[] upbitKey = data.get("upbit");
        String[] coinoneKey = data.get("coinone");
        String[] binanceKey = data.get("binance");
        String id = request.getSession().getAttribute("userId").toString();
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

    public Map<String, ApiDto> getApiKeys(){
        String id = request.getSession().getAttribute("userId").toString();
        Upbit upbit = upbitRepository.findByOwnerId(id);
        Coinone coinone = coinoneRepository.findByOwnerId(id);
        Binance binance = binanceRepository.findByOwnerId(id);
        Map<String, ApiDto> apiMap = new HashMap<>();
        ApiDto apiDto = new ApiDto("","");
        apiMap.put("upbit", apiDto);
        apiMap.put("coinone", apiDto);
        apiMap.put("binance", apiDto);
        if(upbit != null){
            apiMap.put("upbit", new ApiDto(upbit.getAccessKey(), upbit.getSecretKey()));
        }
        if(coinone != null){
            apiMap.put("coinone",new ApiDto(coinone.getAccessKey(), coinone.getSecretKey()));
        }
        if(binance != null){
            apiMap.put("binance",new ApiDto(binance.getAccessKey(), binance.getSecretKey()));
        }
        return apiMap;
    }

    public void saveMember(HttpServletRequest data){
        Member member= new  Member(data.getParameter("memberId"), passwordEncoder.encode(data.getParameter("memberPw")));
        memberRepository.save(member);
    }

    public boolean checkDuplicate(Map<String, String> data){
        if(memberRepository.findByUserId(data.get("memberId")) != null){
            return true;
        }else {
            return false;
        }
    }
}
