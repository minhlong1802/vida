package com.example.vida.repository;

import com.example.vida.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserByEmailAndPassword(String email, String password);

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findAll(Specification<User> sepecification, Pageable pageable);
    @Query("SELECT u FROM User u WHERE "
            + "(:searchText IS NULL OR u.username LIKE %:searchText%) AND "
            + "(:companyId IS NULL OR u.department.company.id = :companyId) AND "
            + "(:departmentId IS NULL OR u.department.id = :departmentId) AND "
            + "(:status IS NULL OR u.status = :status)")
    List<User> searchUsersForExport(@Param("searchText") String searchText,
                                    @Param("companyId") Integer companyId,
                                    @Param("departmentId") Integer departmentId,
                                    @Param("status") Integer status);

}