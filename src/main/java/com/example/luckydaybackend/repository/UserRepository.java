package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByUsername(String userId);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String userId);
    Optional<User> findById(Long id);

    @Query("SELECT u FROM User u JOIN FETCH u.profile WHERE u IN :users")
    List<User> findAllWithProfileByUsers(@Param("users") List<User> users);

    @Query("SELECT u FROM User u JOIN FETCH u.profile WHERE u.id IN :ids")
    List<User> findAllWithProfileByIdIn(@Param("ids") List<Long> ids);

    List<User> findByUsernameContainingIgnoreCase(String userId);

}


