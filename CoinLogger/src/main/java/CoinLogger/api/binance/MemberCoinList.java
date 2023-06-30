package CoinLogger.api.binance;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table()
public class MemberCoinList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String userId;
}
