package com.college.library.service;

import com.college.library.model.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    // In-memory storage
    private final List<Book> books = new ArrayList<>();

    // Add a book. Returns false if a book with the same ID already exists.
    public boolean addBook(Book book) {
        if (getBookById(book.getId()) != null) {
            return false;
        }
        books.add(book);
        return true;
    }

    // Get all books
    public List<Book> getAllBooks() {
        return books;
    }

    // Get one book by ID. Returns null if not found.
    public Book getBookById(Integer id) {
        for (Book b : books) {
            if (b.getId().equals(id)) {
                return b;
            }
        }
        return null;
    }

    // Update a book. Returns the updated book, or null if not found.
    public Book updateBook(Integer id, Book updatedBook) {
        Book existing = getBookById(id);
        if (existing == null) {
            return null;
        }
        existing.setTitle(updatedBook.getTitle());
        existing.setAuthor(updatedBook.getAuthor());
        existing.setCategory(updatedBook.getCategory());
        existing.setAvailableCopies(updatedBook.getAvailableCopies());
        return existing;
    }

    // Delete a book. Returns true if deleted, false if not found.
    public boolean deleteBook(Integer id) {
        Book existing = getBookById(id);
        if (existing == null) {
            return false;
        }
        books.remove(existing);
        return true;
    }
}
