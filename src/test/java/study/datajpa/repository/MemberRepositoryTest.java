package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성보장
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findByUsername() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result = memberRepository.findByUsername("AAA");
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(10);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findUser() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result = memberRepository.findUser("AAA",10);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(10);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findMemberDto() {
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(teamA);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void resultType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        Member m3 = new Member("CCC", 30);
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);

        // 없으면 null , 2건이상이면 NonUniqueResultException -> spring exception 으로 변경 incorrectResultSizeDataAccessException (Db 에 따른 수정이 필요없음 )
        Member member = memberRepository.findMemberByUsername("AAA");
        // 데이터가 없으면 null 이 아니라 빈 Collection 반환 , null 이 아닌게 보장
        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        // 없으면 optional empty
        Optional<Member> optional = memberRepository.findOptionalByUsername("FFF");
        Member member1 = optional.orElse(m2);
        Member member2 = optional.orElseGet(() -> memberRepository.findMembers("CCC"));
        System.out.println("member1 = " + member1);
        System.out.println("member2 = " + member2);
    }

    @Test
    public void page() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(10, pageRequest); // 반환타입이 page 이면 totalCount 를 자동으로 실행
        Page<Member> page2 = memberRepository.findByAgeQuery(10, pageRequest); // count query 분리

        //then
        List<Member> content = page.getContent(); //조회된 데이터
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?

        Page<MemberDto> dtoPage = page.map(m -> new MemberDto()); // 생성자에서 특정 값만 넘겨도 가능, page 는 api 로 넘기면 json으로 count 등 page 정보가 자동으로 매핑된다

        List<Member> content2 = page2.getContent(); //조회된 데이터
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page2.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(page2.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page2.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(page2.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page2.hasNext()).isTrue(); //다음 페이지가 있는가?

        Slice<Member> slice = memberRepository.findSliceByAge(10, pageRequest); // slice는 totalCount가 필요없어서 실행하지 않음 , size +1 개 로딩
        List<Member> content3 = slice.getContent(); //조회된 데이터
        assertThat(content3.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(slice.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(slice.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(slice.hasNext()).isTrue(); //다음 페이지가 있는가?

        List<Member> top3ByAge = memberRepository.findTop3ByAge(10);
        assertThat(top3ByAge.size()).isEqualTo(3);
    }
}