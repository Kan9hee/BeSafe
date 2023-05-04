package Bright.BeSafeProject.repository;

import Bright.BeSafeProject.entity.MemberEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends CrudRepository<MemberEntity,Long> {
}
