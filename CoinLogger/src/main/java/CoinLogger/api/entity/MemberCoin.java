package CoinLogger.api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "member_coin_list")
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class MemberCoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;
    private String owner_id;
    @Column(name = "coin_name")
    private String coinName;
    private double avg_buy_price;
    private double amount;
    private long mill_time;
}
