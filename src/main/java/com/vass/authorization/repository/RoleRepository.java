package com.vass.authorization.repository;

import com.vass.authorization.entity.RoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);

    Optional<RoleEntity> findFirstByPermissions_CodeIgnoreCase(String code);
}
