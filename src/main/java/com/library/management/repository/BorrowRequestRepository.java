package com.library.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.management.model.BorrowRequest;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    Optional<BorrowRequest> findByBook_IsbnAndStatus(String isbn, String status);
    List<BorrowRequest> findByStatus(String status);
}