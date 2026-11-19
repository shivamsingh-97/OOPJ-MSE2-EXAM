package com.college.library.controller;

import com.college.library.model.Book;
import com.college.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    // Helper: builds {"message": "..."} response body
    private Map<String, String> message(String text) {
        Map<String, String> map = new HashMap<>();
        map.put("message", text);
        return map;
    }

    // Helper: checks the book fields. Returns an error text, or null if everything is valid.
    private String validate(Book book, boolean checkId) {
        if (book == null) {
            return "Request body is missing";
        }
        if (checkId && book.getId() == null) {
            return "Book id is required";
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return "Title is required";
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            return "Author is required";
        }
        if (book.getCategory() == null || book.getCategory().trim().isEmpty()) {
            return "Category is required";
        }
        if (book.getAvailableCopies() == null || book.getAvailableCopies() < 0) {
            return "availableCopies is required and cannot be negative";
        }
        return null;
    }

    // b) Add a new book
    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        String error = validate(book, true);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message(error));
        }
        boolean added = bookService.addBook(book);
        if (!added) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(message("Book with id " + book.getId() + " already exists"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    // c) Get all books
    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // c) Get a book by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Integer id) {
        Book book = bookService.getBookById(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(message("Book with id " + id + " not found"));
        }
        return ResponseEntity.ok(book);
    }

    // d) Update a book
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Integer id, @RequestBody Book book) {
        String error = validate(book, false);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message(error));
        }
        Book updated = bookService.updateBook(id, book);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(message("Book with id " + id + " not found"));
        }
        return ResponseEntity.ok(updated);
    }

    // e) Delete a book
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Integer id) {
        boolean deleted = bookService.deleteBook(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(message("Book with id " + id + " not found"));
        }
        return ResponseEntity.ok(message("Book with id " + id + " deleted successfully"));
    }
}