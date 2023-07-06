package CoinLogger.api.binance;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "member_coin_list")
@RequiredArgsConstructor
@Builder
public class MemberCoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private int idx;
    private String owner_id;
    private String coin_name;
    private double avg_buy_price;
    private double amount;
}
