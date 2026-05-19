package com.vass.authorization.repository;

import com.vass.authorization.entity.UserEntity;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("""
        select distinct permission.code
        from UserEntity user
        join user.roles role
        join role.permissions permission
        where user.id = :userId
          and user.active = true
          and role.active = true
        """)
    Set<String> findActivePermissionCodesByUserId(@Param("userId") Long userId);

    @Query("""
        select distinct permission.code
        from UserEntity user
        join user.directPermissions permission
        where user.id = :userId
          and user.active = true
        """)
    Set<String> findDirectPermissionCodesByUserId(@Param("userId") Long userId);
}
