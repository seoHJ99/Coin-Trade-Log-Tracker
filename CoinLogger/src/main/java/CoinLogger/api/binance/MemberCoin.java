package CoinLogger.api.binance;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "membercoinlist")
public class MemberCoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String ownerId;
    private String coinName;
    private double avgBuyPrice;
    private double amount;
}
