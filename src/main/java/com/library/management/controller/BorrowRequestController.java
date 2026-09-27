package com.library.management.controller;

import com.library.management.model.*;
import com.library.management.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reader")
public class BorrowRequestController {
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final BorrowRequestRepository requestRepo;
    private final LoanCardRepository loanRepo;
    public BorrowRequestController(BookRepository bookRepo,
                                   UserRepository userRepo,
                                   BorrowRequestRepository requestRepo,
                                   LoanCardRepository loanRepo) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.requestRepo = requestRepo;
        this.loanRepo = loanRepo;
    }
    @PostMapping("/request-borrow")
    public String requestBorrow(@RequestParam Long userId,
                                @RequestParam String isbn) {
        Book book = bookRepo.findById(isbn).orElse(null);
        if (book == null) return "redirect:/library/books?error=Book+not+found";
        if (!book.isAvailable()) {
            return "redirect:/library/books?error=Book+out+of+stock";
        }
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return "redirect:/library/books?error=User+not+found";
        if (requestRepo.findByBook_IsbnAndStatus(isbn, "PENDING").isPresent()) {
            return "redirect:/library/books?error=Already+requested";
        }
        if (loanRepo.findByBook_IsbnAndStatus(isbn, "OPEN").isPresent()) {
             return "redirect:/library/books?error=You+are+keeping+this+book";
        }
        BorrowRequest req = new BorrowRequest();
        req.setBook(book);
        req.setUser(user);
        req.setRequestDate(LocalDate.now());
        req.setStatus("PENDING");
        requestRepo.save(req);
        return "redirect:/library/books?success=true";
    }
    @GetMapping("/history")
    public String showHistoryPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/auth/login";
        List<LoanCard> history = loanRepo.findByUser_Id(user.getId());
        model.addAttribute("history", history);
        return "history";
    }
    @GetMapping("/current-loans")
    public String showCurrentLoans(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/auth/login";
        List<LoanCard> allLoans = loanRepo.findByUser_Id(user.getId());
        List<LoanCard> activeLoans = new ArrayList<>();
        for(LoanCard l : allLoans) {
            if("OPEN".equals(l.getStatus()) || "RETURNING".equals(l.getStatus())) {
                activeLoans.add(l);
            }
        }
        model.addAttribute("loans", activeLoans);
        return "current-loans";
    }
    @PostMapping("/return-request")
    public String requestReturn(@RequestParam Long loanId) {
        LoanCard loan = loanRepo.findById(loanId).orElse(null);
        if (loan != null && "OPEN".equals(loan.getStatus())) {
            loan.setStatus("RETURNING"); 
            loanRepo.save(loan);
        }
        return "redirect:/reader/current-loans";
    }
}