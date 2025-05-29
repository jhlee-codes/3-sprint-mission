package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    List<User> findAllByIdIn(Collection<UUID> ids);

    Optional<User> findById(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

//
//    // 특정 채널 데이터 추가 후 저장
//    User save(User user);
//
//    // 데이터 전체 조회
//    List<User> findAll();
//
//    // 데이터 단건 조회(id)
//    Optional<User> findById(UUID id);
//
//    // 데이터 단건 조회 (username)
//    Optional<User> findByUserName(String userName);
//
//    // 데이터 존재 여부 (id)
//    boolean existsById(UUID id);
//
//    // 데이터 존재 여부 (username)
//    boolean existsByUserName(String userName);
//
//    // 데이터 존재 여부 (email)
//    boolean existsByEmail(String email);
//
//    // 데이터 삭제
//    void deleteById(UUID id);

}
