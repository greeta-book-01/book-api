package net.greeta.book.controller;

import net.greeta.book.entity.Book;
import net.greeta.book.responsemodels.ShelfCurrentLoansResponse;
import net.greeta.book.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BookController {

    private BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/secure/currentloans")
    public List<ShelfCurrentLoansResponse> currentLoans(JwtAuthenticationToken token)
        throws Exception
    {
        String userEmail = token.getToken().getClaimAsString("email");
        return bookService.currentLoans(userEmail);
    }

    @GetMapping("/secure/currentloans/count")
    public int currentLoansCount(JwtAuthenticationToken token) {
        String userEmail = token.getToken().getClaimAsString("email");
        return bookService.currentLoansCount(userEmail);
    }

    @GetMapping("/secure/ischeckedout/byuser")
    public Boolean checkoutBookByUser(@RequestParam Long bookId, JwtAuthenticationToken token) {
        String userEmail = token.getToken().getClaimAsString("email");
        return bookService.checkoutBookByUser(userEmail, bookId);
    }

    @PutMapping("/secure/checkout")
    public Book checkoutBook (@RequestParam Long bookId, JwtAuthenticationToken token) throws Exception {
        String userEmail = token.getToken().getClaimAsString("email");
        return bookService.checkoutBook(userEmail, bookId);
    }

    @PutMapping("/secure/return")
    public void returnBook(@RequestParam Long bookId, JwtAuthenticationToken token) throws Exception {
        String userEmail = token.getToken().getClaimAsString("email");
        bookService.returnBook(userEmail, bookId);
    }

    @PutMapping("/secure/renew/loan")
    public void renewLoan(@RequestParam Long bookId, JwtAuthenticationToken token) throws Exception {
        String userEmail = token.getToken().getClaimAsString("email");
        bookService.renewLoan(userEmail, bookId);
    }

}












