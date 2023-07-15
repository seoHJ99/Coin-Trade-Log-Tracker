package CoinLogger.api.upbit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpbitRepository extends JpaRepository<Upbit, Integer> {
    Upbit findByOwnerId(String id);
}

