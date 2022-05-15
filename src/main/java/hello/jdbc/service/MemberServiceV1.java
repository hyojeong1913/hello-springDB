package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    /**
     * fromId 의 회원을 조회해서 toId 의 회원에게 money 만큼의 돈을 계좌이체 하는 로직
     *
     * @param fromId
     * @param toId
     * @param money
     * @throws SQLException
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // fromId 회원의 돈을 money 만큼 감소
        memberRepository.update(fromId, fromMember.getMoney() - money);
        
        validation(toMember);

        // toId 회원의 돈을 money 만큼 증가
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    /**
     * 예외 상황을 테스트해보기 위해 toId 가 "ex" 인 경우 예외 발생
     *
     * @param toMember
     */
    private void validation(Member toMember) {

        if (toMember.getMemberId().equals("ex")) {

            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
