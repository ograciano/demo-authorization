package com.vass.authorization.repository;

import com.vass.authorization.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select distinct u
            from UserEntity u
            left join fetch u.roles r
            left join fetch r.permissions
            where u.id = :id
            """)
    Optional<UserEntity> findDetailedById(@Param("id") Long id);
}
