package com.library.management.controller;

import com.library.management.model.*;
import com.library.management.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final BookRepository bookRepo;
    private final BorrowRequestRepository requestRepo;
    private final LoanCardRepository loanRepo;
    private final UserRepository userRepo;
    public AdminController(BookRepository bookRepo,
                           BorrowRequestRepository requestRepo,
                           LoanCardRepository loanRepo,
                           UserRepository userRepo) {
        this.bookRepo = bookRepo;
        this.requestRepo = requestRepo;
        this.loanRepo = loanRepo;
        this.userRepo = userRepo;
}
    @GetMapping("/approve-requests")
    public String showApprovePage(Model model) {
        model.addAttribute("requests", requestRepo.findByStatus("PENDING"));
        return "admin-approve";
    }
    @PostMapping("/approve/{requestId}")
    @Transactional
    public String approveBorrow(@PathVariable Long requestId) {
        BorrowRequest req = requestRepo.findById(requestId).orElse(null);
        if (req == null || !"PENDING".equals(req.getStatus())) {
            return "redirect:/admin/approve-requests?error=Invalid";
        }
        Book book = req.getBook();
        if (book.getQuantity() <= 0) {
            req.setStatus("REJECTED");
            requestRepo.save(req);
            return "redirect:/admin/approve-requests?error=OutOfStock";
        }
        LoanCard loan = new LoanCard();
        loan.setBook(book);
        loan.setUser(req.getUser());
        loan.setIssueDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus("OPEN");
        loanRepo.save(loan);
        req.setStatus("APPROVED");
        requestRepo.save(req);
        book.setQuantity(book.getQuantity() - 1);
        bookRepo.save(book);
        return "redirect:/admin/approve-requests?success=true";
    }
    @GetMapping("/add-book")
    public String showAddBookForm() { return "add-book"; }
    @PostMapping("/add-book")
    public String addBook(@ModelAttribute Book book) {
        if (bookRepo.existsById(book.getIsbn())) return "redirect:/admin/add-book?error=Exists";
        bookRepo.save(book);
        return "redirect:/library/books";
    }
    @GetMapping("/edit-book/{isbn}")
    public String showEditForm(@PathVariable String isbn, Model model) {
        Book book = bookRepo.findById(isbn).orElse(null);
        model.addAttribute("book", book);
        return "edit-book";
    }
    @PostMapping("/update-book")
    public String updateBook(@ModelAttribute Book book) {
        bookRepo.save(book);
        return "redirect:/library/books";
    }
    @GetMapping("/delete-book/{isbn}")
    public String deleteBook(@PathVariable String isbn) {
        boolean isBorrowed = loanRepo.findByBook_IsbnAndStatus(isbn, "OPEN").isPresent() 
                          || loanRepo.findByBook_IsbnAndStatus(isbn, "RETURNING").isPresent();
        if (isBorrowed) {
            return "redirect:/library/books?error=Book+is+currently+borrowed+cannot+delete";
        }
        boolean isRequested = requestRepo.findByBook_IsbnAndStatus(isbn, "PENDING").isPresent();
        if (isRequested) {
             return "redirect:/library/books?error=Book+has+pending+requests+cannot+delete";
        }
        bookRepo.deleteById(isbn);
        return "redirect:/library/books?success=true";
    }
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepo.findByRole("READER"));
        return "admin-users";
    }
    @GetMapping("/return-book")
    public String showReturnList(Model model) {
        model.addAttribute("returns", loanRepo.findByStatus("RETURNING"));
        return "admin-return"; 
    }
    @PostMapping("/confirm-return")
    public String confirmReturn(@RequestParam Long loanId) {
        LoanCard loan = loanRepo.findById(loanId).orElse(null);
        if (loan != null && "RETURNING".equals(loan.getStatus())) {
            loan.setReturnDate(LocalDate.now());
            loan.setStatus("CLOSED");
            loanRepo.save(loan);

            Book book = loan.getBook();
            book.setQuantity(book.getQuantity() + 1);
            bookRepo.save(book);
        }
        return "redirect:/admin/return-book?success=true";
    }
    @GetMapping("/user-history/{userId}")
    public String viewUserHistory(@PathVariable Long userId, Model model, jakarta.servlet.http.HttpSession session) {
        model.addAttribute("history", loanRepo.findByUser_Id(userId));    
        return "history"; 
    }
}