package com.pharmadesk.backend.repository;

import com.pharmadesk.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles"})
    java.util.Optional<User> findByUsername(String username);
    
    long countByStatus(String status);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles"})
    java.util.List<User> findByStatus(String status);

    java.util.List<User> findAllByDeletedFalse();

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :id")
    void updateLastLogin(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("lastLogin") java.time.LocalDateTime lastLogin);
}
