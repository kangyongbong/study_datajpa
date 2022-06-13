package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom{

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

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();
    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();
    //메서드 이름으로 쿼리에서 특히 편리하다.
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(String username);

    @EntityGraph("Member.all") // fetch join 을 사용해도 됨 , 간단한건 EntityGraph / 복잡한건 fetch join
    @Query("select m from Member m")
    List<Member> findMemberNamedEntityGraph();

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @QueryHints(value = { @QueryHint(name = "org.hibernate.readOnly", value = "true") },
            forCounting = true)
    Page<Member> findReadOnlyByUsername(String name, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);
}