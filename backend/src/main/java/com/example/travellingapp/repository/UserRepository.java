package com.example.travellingapp.repository;

import com.example.travellingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the User if found, or empty if not found
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findByUsernameAndActive(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.isActive = :isActive")
    Optional<User> findByPhoneNumberAndActive(@Param("phoneNumber") String phoneNumber, @Param("isActive") boolean isActive);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = :isActive")
    Optional<User> findByEmailAndActive(@Param("email") String email, @Param("isActive") boolean isActive);
}
