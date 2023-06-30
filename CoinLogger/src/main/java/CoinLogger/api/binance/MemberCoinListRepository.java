package CoinLogger.api.binance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberCoinListRepository extends JpaRepository<MemberCoinList, String> {
}
