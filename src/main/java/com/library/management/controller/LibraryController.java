package com.library.management.controller;

import com.library.management.model.*;
import com.library.management.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/library")
public class LibraryController {
    private final BookRepository bookRepo;
    private final LoanCardRepository loanRepo;
    private final BorrowRequestRepository requestRepo;
    public LibraryController(BookRepository bookRepo, 
                             LoanCardRepository loanRepo, 
                             BorrowRequestRepository requestRepo) {
        this.bookRepo = bookRepo;
        this.loanRepo = loanRepo;
        this.requestRepo = requestRepo;
    }
    @GetMapping("/books")
    public String showBooksPage(@RequestParam(required = false) String keyword, 
                                Model model, 
                                HttpSession session) {
        List<Book> books;
        if (keyword != null && !keyword.isEmpty()) {
            books = bookRepo.search(keyword);
        } else {
            books = bookRepo.findAll();
        }
        User currentUser = (User) session.getAttribute("currentUser");
        List<BookStatusDTO> dtoList = new ArrayList<>();
        for (Book b : books) {
            String status = (b.getQuantity() > 0) ? "AVAILABLE" : "OUT_OF_STOCK";
            boolean borrowedByMe = false;
            boolean requestedByMe = false;
            if (currentUser != null && "READER".equals(currentUser.getRole())) {
                if (loanRepo.findByBook_IsbnAndStatus(b.getIsbn(), "OPEN").isPresent()) {
                     List<LoanCard> myLoans = loanRepo.findByUser_IdAndStatus(currentUser.getId(), "OPEN");
                     for (LoanCard l : myLoans) {
                         if (l.getBook().getIsbn().equals(b.getIsbn())) {
                             borrowedByMe = true;
                             break;
                         }
                     }
                }
                if (requestRepo.findByBook_IsbnAndStatus(b.getIsbn(), "PENDING").isPresent()) {
                     requestedByMe = true; 
                }
            }
            dtoList.add(new BookStatusDTO(
                    b.getIsbn(), 
                    b.getTitle(), 
                    b.getAuthor(), 
                    status,
                    b.getQuantity(),
                    borrowedByMe, 
                    requestedByMe  
            ));
        }
        model.addAttribute("books", dtoList);
        model.addAttribute("keyword", keyword);
        return "books";
    }
}