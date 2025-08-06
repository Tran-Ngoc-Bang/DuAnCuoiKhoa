package com.fpoly.shared_learning_materials.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsernameOrEmail(String username, String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	Page<User> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

	List<User> findByDeletedAtIsNull();
}
