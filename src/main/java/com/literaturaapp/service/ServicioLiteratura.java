package com.literaturaapp.service;

import com.literaturaapp.model.Book;
import com.literaturaapp.model.GutendexResponse;
import com.literaturaapp.model.Author;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.DoubleSummaryStatistics;

@Service
public class ServicioLiteratura {

    private final RestTemplate restTemplate;
    private final String BASE = "https://gutendex.com";

    // simple cache to avoid many API calls in a short run; can be refreshed
    private List<Book> cachedResults = null;

    public ServicioLiteratura() {
        this.restTemplate = new RestTemplate();
    }

    // Search books by title using Gutendex search endpoint
    public List<Book> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) return Collections.emptyList();
        String uri = UriComponentsBuilder.fromHttpUrl(BASE + "/books")
                .queryParam("search", title)
                .queryParam("page_size", 20)
                .toUriString();
        try {
            GutendexResponse resp = restTemplate.getForObject(uri, GutendexResponse.class);
            if (resp == null || resp.getResults() == null) return Collections.emptyList();
            return resp.getResults();
        } catch (Exception e) {
            System.err.println("Error searching books: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // List books - fetch first page (or cached)
    public List<Book> listAllBooks() {
        if (cachedResults != null && !cachedResults.isEmpty()) return cachedResults;
        List<Book> all = new ArrayList<>();
        String uri = UriComponentsBuilder.fromHttpUrl(BASE + "/books")
                .queryParam("page_size", 40)
                .toUriString();
        try {
            GutendexResponse resp = restTemplate.getForObject(uri, GutendexResponse.class);
            if (resp != null && resp.getResults() != null) {
                all.addAll(resp.getResults());
            }
        } catch (Exception e) {
            System.err.println("Error listing books: " + e.getMessage());
        }
        this.cachedResults = all;
        return all;
    }

    public List<String> listAllAuthors() {
        List<Book> books = listAllBooks();
        Set<String> names = new TreeSet<>();
        for (Book b : books) {
            if (b.getAuthors() != null) {
                for (Author a : b.getAuthors()) {
                    if (a.getName()!=null) names.add(a.getName());
                }
            }
        }
        return new ArrayList<>(names);
    }

    public List<String> listAuthorsAliveInYear(int year) {
        List<Book> books = listAllBooks();
        Set<String> alive = new TreeSet<>();
        for (Book b : books) {
            if (b.getAuthors()!=null) {
                for (Author a : b.getAuthors()) {
                    Integer birth = a.getBirth_year();
                    Integer death = a.getDeath_year();
                    boolean aliveFlag = (birth==null || birth <= year) && (death==null || death>year);
                    if (a.getName()!=null && aliveFlag) {
                        String name = a.getName();
                        if (birth!=null) name += " ("+birth+(death!=null?("-"+death):"")+")";
                        alive.add(name);
                    }
                }
            }
        }
        return new ArrayList<>(alive);
    }

    
    public List<Book> listBooksByLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            return listAllBooks();
        }
        String code = lang.trim().toLowerCase();
        // Try remote API query for the language to get more complete results
        String uri = UriComponentsBuilder.fromHttpUrl(BASE + "/books")
                .queryParam("languages", code)
                .queryParam("page_size", 40)
                .toUriString();
        try {
            GutendexResponse resp = restTemplate.getForObject(uri, GutendexResponse.class);
            if (resp != null && resp.getResults() != null) {
                return resp.getResults();
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            // Fall back to cached filtering if remote call fails
            List<Book> all = listAllBooks();
            return all.stream()
                    .filter(b -> b.getLanguages()!=null && b.getLanguages().stream().anyMatch(l -> l.equalsIgnoreCase(code)))
                    .collect(Collectors.toList());
        }
    }


    // Aggregate first N pages and sort by download_count
    public List<Book> topNMostDownloaded(int pages) {
        List<Book> all = new ArrayList<>();
        try {
            for (int p=1;p<=pages;p++) {
                String uri = UriComponentsBuilder.fromHttpUrl(BASE + "/books")
                        .queryParam("page", p)
                        .queryParam("page_size", 100)
                        .toUriString();
                GutendexResponse resp = restTemplate.getForObject(uri, GutendexResponse.class);
                if (resp==null || resp.getResults()==null || resp.getResults().isEmpty()) break;
                all.addAll(resp.getResults());
                if (resp.getNext()==null) break;
            }
        } catch (Exception e) {
            System.err.println("Error fetching pages for top: " + e.getMessage());
        }
        return all.stream().sorted(Comparator.comparingInt(Book::getDownload_count).reversed()).collect(Collectors.toList());
    }

    public List<String> searchAuthorByName(String name) {
        if (name==null || name.trim().isEmpty()) return Collections.emptyList();
        String uri = UriComponentsBuilder.fromHttpUrl(BASE + "/books")
                .queryParam("search", name)
                .queryParam("page_size", 50)
                .toUriString();
        try {
            GutendexResponse resp = restTemplate.getForObject(uri, GutendexResponse.class);
            if (resp==null || resp.getResults()==null) return Collections.emptyList();
            Set<String> authors = new TreeSet<>();
            for (Book b: resp.getResults()) {
                if (b.getAuthors()!=null) {
                    for (Author a : b.getAuthors()) {
                        if (a.getName()!=null && a.getName().toLowerCase().contains(name.toLowerCase())) {
                            authors.add(a.toString());
                        }
                    }
                }
            }
            return new ArrayList<>(authors);
        } catch (Exception e) {
            System.err.println("Error searching author: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    
    // --- Compatibility wrapper methods added to support controller calls ---

    /**
     * Search by title and optionally filter by language code.
     * If lang is empty or null, returns the plain search results.
     */
    public List<Book> searchByTitle(String title, String lang) {
        List<Book> results = searchByTitle(title);
        if (results == null) return Collections.emptyList();
        if (lang == null || lang.trim().isEmpty()) return results;
        final String code = lang.trim().toLowerCase();
        return results.stream()
                .filter(b -> b.getLanguages()!=null && b.getLanguages().stream().anyMatch(l -> l.equalsIgnoreCase(code)))
                .collect(Collectors.toList());
    }

    /**
     * Return all cached books or filtered by language if provided.
     */
    public List<Book> listAllBooksFiltered(String lang) {
        if (lang == null || lang.trim().isEmpty()) return listAllBooks();
        return listBooksByLanguage(lang);
    }

    /**
     * Return a list of Author objects aggregated from books (unique by name).
     */
    public List<Author> listAllAuthorsObjects() {
        List<Book> books = listAllBooks();
        Map<String, Author> map = new LinkedHashMap<>();
        for (Book b : books) {
            if (b.getAuthors() != null) {
                for (Author a : b.getAuthors()) {
                    if (a.getName() != null && !map.containsKey(a.getName())) {
                        map.put(a.getName(), a);
                    }
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    /**
     * Return authors alive in the given year as Author objects.
     */
    public List<Author> listAuthorsAliveInYearObjects(int year) {
        List<Book> books = listAllBooks();
        Map<String, Author> map = new LinkedHashMap<>();
        for (Book b : books) {
            if (b.getAuthors() != null) {
                for (Author a : b.getAuthors()) {
                    if (a.getName() == null) continue;
                    boolean alive = false;
                    if (a.getBirth_year() != null) {
                        if (a.getDeath_year() == null) {
                            if (a.getBirth_year() <= year) alive = true;
                        } else {
                            if (a.getBirth_year() <= year && a.getDeath_year() >= year) alive = true;
                        }
                    }
                    if (alive && !map.containsKey(a.getName())) map.put(a.getName(), a);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    /**
     * Return top N books by download_count using the available aggregated method.
     * If topNMostDownloaded exists, use it (pages param); otherwise fallback to cached results.
     */
    public List<Book> topNByDownloads(int n) {
        try {
            // try using topNMostDownloaded(5) to gather many results
            List<Book> candidates = topNMostDownloaded(5);
            if (candidates == null) candidates = listAllBooks();
            return candidates.stream()
                    .sorted(Comparator.comparingInt(Book::getDownload_count).reversed())
                    .limit(n)
                    .collect(Collectors.toList());
        } catch (NoSuchMethodError | Exception e) {
            List<Book> all = listAllBooks();
            return all.stream()
                    .sorted(Comparator.comparingInt(Book::getDownload_count).reversed())
                    .limit(n)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Find authors by name (case-insensitive) and return Author objects.
     */
    public List<Author> findAuthorsByName(String q) {
        if (q == null || q.trim().isEmpty()) return Collections.emptyList();
        String low = q.toLowerCase();
        return listAllAuthorsObjects().stream()
                .filter(a -> a.getName()!=null && a.getName().toLowerCase().contains(low))
                .collect(Collectors.toList());
    }

    /**
     * Find a book by id within cached/listed books.
     */
    public Book findBookById(int id) {
        return listAllBooks().stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }

    // --- End compatibility wrappers ---

public DoubleSummaryStatistics downloadStats() {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        List<Book> books = listAllBooks();
        for (Book b : books) {
            stats.accept(b.getDownload_count());
        }
        return stats;
    }

    public void refreshCache() {
        this.cachedResults = null;
        listAllBooks();
    }
}
