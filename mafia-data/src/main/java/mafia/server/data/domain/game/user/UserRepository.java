package mafia.server.data.domain.game.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByAccountId(Long accountId);

    boolean existsByNickname(String nickname);

    Optional<User> findByAccountId(Long accountId);

    Optional<User> findByNickname(String nickname);
}
