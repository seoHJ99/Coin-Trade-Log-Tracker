package CoinLogger.api.entity;

import CoinLogger.api.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    @Query(value = "select * from member where member_id = :Id" ,nativeQuery = true)
    Member findByUserId(@Param("Id") String id);
}
