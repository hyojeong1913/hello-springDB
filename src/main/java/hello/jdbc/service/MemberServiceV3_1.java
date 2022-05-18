package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 트랜잭션 시작
        // 현재 트랜잭션의 상태 정보가 포함되어 있으며, 이후 트랜잭션을 커밋, 롤백할 때 필요
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {

            // 트랜잭션이 시작된 커넥션을 전달하면서 비즈니스 로직을 수행
            bizLogic(fromId, toId, money);

            // 비즈니스 로직이 정상 수행되면 트랜잭션을 커밋
            transactionManager.commit(status);
        }
        catch (Exception e) {

            // 비즈니스 로직 수행 도중에 예외가 발생하면 트랜잭션을 롤백
            transactionManager.rollback(status);

            throw new IllegalStateException(e);
        }
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);

        validation(toMember);

        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {

        if (toMember.getMemberId().equals("ex")) {

            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
