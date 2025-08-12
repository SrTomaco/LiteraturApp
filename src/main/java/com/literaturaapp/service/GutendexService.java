package com.literaturaapp.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.literaturaapp.model.Book;
import com.literaturaapp.model.Author;
import com.literaturaapp.model.GutendexResponse;

@Service
public class GutendexService {

    private final WebClient webClient;
    private List<Book> cachedBooks = null;

    public GutendexService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://gutendex.com")
                .build();
    }

    public GutendexResponse searchBooks(String query, int page) {
        try {
            String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String uri = "/books/?search=" + q + "&page=" + page;
            return webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(GutendexResponse.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error searchBooks: " + e.getMessage());
            return null;
        }
    }

    public Book getBookById(int id) {
        try {
            String uri = "/books/" + id + "/";
            return webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Book.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized List<Book> listAllBooks() {
        if (cachedBooks != null) return cachedBooks;
        List<Book> all = new ArrayList<>();
        try {
            int page = 1;
            while (true) {
                String uri = "/books/?page=" + page;
                GutendexResponse resp = webClient.get()
                        .uri(uri)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(GutendexResponse.class)
                        .block();
                if (resp == null || resp.getResults() == null || resp.getResults().isEmpty()) break;
                all.addAll(resp.getResults());
                if (resp.getNext() == null) break;
                page++;
                // safety cap
                if (page > 50) break;
            }
        } catch (Exception e) {
            System.err.println("Error listAllBooks: " + e.getMessage());
        }
        cachedBooks = all;
        return cachedBooks;
    }

    public synchronized void refreshCache() {
        this.cachedBooks = null;
        listAllBooks();
    }

    public List<Author> listAllAuthors() {
        List<Book> all = listAllBooks();
        Map<String, Author> map = new LinkedHashMap<>();
        for (Book b : all) {
            if (b.getAuthors() == null) continue;
            for (Author a : b.getAuthors()) {
                if (a == null || a.getName() == null) continue;
                map.putIfAbsent(a.getName(), a);
            }
        }
        return new ArrayList<>(map.values());
    }

    public List<Book> getTopDownloads(int n) {
        List<Book> all = listAllBooks();
        return all.stream()
                .sorted(Comparator.comparingInt(Book::getDownload_count).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public List<Author> findAuthorsByName(String q) {
        if (q == null) return Collections.emptyList();
        String low = q.toLowerCase();
        return listAllAuthors().stream()
                .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(low))
                .collect(Collectors.toList());
    }
}
