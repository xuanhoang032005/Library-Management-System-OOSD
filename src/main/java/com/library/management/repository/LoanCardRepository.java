package com.library.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.management.model.LoanCard;

public interface LoanCardRepository extends JpaRepository<LoanCard, Long> {
    List<LoanCard> findByUser_Id(Long userId);
    Optional<LoanCard> findByBook_IsbnAndStatus(String isbn, String status);
    List<LoanCard> findByUser_IdAndStatus(Long userId, String status);
    List<LoanCard> findByStatus(String status);
}