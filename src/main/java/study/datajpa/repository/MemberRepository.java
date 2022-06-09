package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

//    List<Member> findByUsername(String userName);

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

//    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findByUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) " +
            "from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username = :name")
    Member findMembers(@Param("name") String username);

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    // 다양한 반환타입 가능
    List<Member> findListByUsername(String name); //컬렉션
    Member findMemberByUsername(String name); //단건
    Optional<Member> findOptionalByUsername(String name); //단건 Optional

    Page<Member> findByAge(int age, Pageable pageable);

    @Query(value = "select m from Member m left outer join m.team t where m.age = :age"
            , countQuery = "select count(m) from Member m")
        // count 분리 가능
    Page<Member> findByAgeQuery(@Param("age") int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, PageRequest pageRequest);

    List<Member> findTop3ByAge(int age);

//    @Modifying
    @Modifying(clearAutomatically = true) //이 옵션 없이 다시 조회하면 영속성 컨텍스트에 과거 값이 남아서 문제가 될 수있다. 다시 조회해야 하면 꼭 영속성 컨텍스트를 초기화 하자.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
}