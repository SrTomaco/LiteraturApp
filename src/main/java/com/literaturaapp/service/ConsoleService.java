package com.literaturaapp.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.literaturaapp.model.Author;
import com.literaturaapp.model.Book;

@Service
public class ConsoleService {

    private final GutendexService gutendexService;
    private final Scanner scanner = new Scanner(System.in);

    // ANSI colors
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public ConsoleService(GutendexService gutendexService) {
        this.gutendexService = gutendexService;
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1": listAllBooksUI(); break;
                case "2": listAuthorsUI(); break;
                case "3": topDownloadsUI(); break;
                case "4": statsUI(); break;
                case "5": searchByLanguageUI(); break;
                case "6": searchAuthorByNameUI(); break;
                case "7": refreshCacheUI(); break;
                case "0":
                case "salir":
                case "Salir":
                    System.out.println();
                    System.out.println(ANSI_CYAN + "Hasta luego 👋" + ANSI_RESET);
                    running = false;
                    break;
                default:
                    System.out.println(ANSI_YELLOW + "Opción inválida. Intentá de nuevo." + ANSI_RESET);
            }
        }
        System.exit(0);
    }

    private void printMenu() {
        String border = "╔" + "═".repeat(58) + "╗";
        String footer = "╚" + "═".repeat(58) + "╝";
        System.out.println();
        System.out.println(border);
        System.out.printf("║%s%-58s%s║%n", ANSI_CYAN, " LITERATURAPP - MENÚ PRINCIPAL", ANSI_RESET);
        System.out.println("╠" + "═".repeat(58) + "╣");
        System.out.printf("║ 1) Listar todos los libros                       %s║%n", "");
        System.out.printf("║ 2) Listar autores                                %s║%n", "");
        System.out.printf("║ 3) Top 10 más descargados                        %s║%n", "");
        System.out.printf("║ 4) Estadísticas                                  %s║%n", "");
        System.out.printf("║ 5) Buscar libros por idioma                      %s║%n", "");
        System.out.printf("║ 6) Buscar autor por nombre                       %s║%n", "");
        System.out.printf("║ 7) Refrescar cache                               %s║%n", "");
        System.out.printf("║ 0) Salir                                         %s║%n", "");
        System.out.println(footer);
        System.out.print(ANSI_GREEN + "Seleccioná una opción: " + ANSI_RESET);
    }

    private void listAllBooksUI() {
        List<Book> books = gutendexService.listAllBooks();
        printBooksTable(books);
    }

    private void listAuthorsUI() {
        List<Author> authors = gutendexService.listAllAuthors();
        printAuthorsTable(authors);
    }

    private void topDownloadsUI() {
        List<Book> top = gutendexService.getTopDownloads(10);
        printBooksTable(top);
    }

    private void statsUI() {
        DoubleSummaryStatistics stats = downloadStats();
        System.out.println(ANSI_CYAN + "Estadísticas de descargas:" + ANSI_RESET);
        System.out.println("Mín: " + stats.getMin() + " | Máx: " + stats.getMax() + " | Prom: " + String.format("%.2f", stats.getAverage()));
    }

    private void searchAuthorByNameUI() {
        System.out.print("Ingrese el nombre (o parte) del autor: ");
        String q = scanner.nextLine().trim();
        List<Author> found = gutendexService.findAuthorsByName(q);
        if (found == null || found.isEmpty()) {
            System.out.println(ANSI_YELLOW + "⚠ No se encontró el autor." + ANSI_RESET);
            return;
        }
        printAuthorsTable(found);
    }

    private void refreshCacheUI() {
        gutendexService.refreshCache();
        System.out.println(ANSI_GREEN + "Cache refrescada." + ANSI_RESET);
    }

    private void searchByLanguageUI() {
        List<Book> all = gutendexService.listAllBooks();
        if (all == null || all.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No hay libros disponibles para obtener idiomas." + ANSI_RESET);
            return;
        }
        Set<String> codesSet = new HashSet<>();
        for (Book b : all) {
            if (b.getLanguages() == null) continue;
            codesSet.addAll(b.getLanguages());
        }
        List<String> codes = new ArrayList<>(codesSet);
        Collections.sort(codes);
        System.out.println();
        System.out.println(ANSI_CYAN + "Idiomas disponibles:" + ANSI_RESET);
        for (int i = 0; i < codes.size(); i++) {
            System.out.printf("%3d) %-20s (%s)%n", i+1, languageNameFallback(codes.get(i)), codes.get(i));
        }
        System.out.print(ANSI_GREEN + "Seleccioná por número o código de idioma: " + ANSI_RESET);
        String sel = scanner.nextLine().trim();
        String chosenCode = null;
        try {
            int idx = Integer.parseInt(sel);
            if (idx >= 1 && idx <= codes.size()) chosenCode = codes.get(idx-1);
        } catch (Exception e) {
            if (codes.contains(sel)) chosenCode = sel;
        }
        if (chosenCode == null) {
            System.out.println(ANSI_YELLOW + "Selección inválida." + ANSI_RESET);
            return;
        }
        final String langChosen = chosenCode;
        List<Book> results = all.stream()
                .filter(b -> b.getLanguages() != null && b.getLanguages().contains(langChosen))
                .collect(Collectors.toList());
        if (results.isEmpty()) {
            System.out.println(ANSI_YELLOW + "⚠ No se encontraron libros en este idioma." + ANSI_RESET);
            return;
        }
        printBooksTable(results);
    }

    private String languageNameFallback(String code) {
        Map<String,String> m = Map.of(
            "es","Español",
            "en","Inglés",
            "fr","Francés",
            "de","Alemán",
            "pt","Portugués",
            "it","Italiano"
        );
        return m.getOrDefault(code, code);
    }

    
    private void printBooksTable(List<Book> books) {
        if (books == null || books.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Sin resultados." + ANSI_RESET);
            return;
        }
        // compute widths with limits
        int maxTitle = Math.min(50, books.stream().map(b -> b.getTitle()==null?0:b.getTitle().length()).max(Integer::compareTo).orElse(10));
        int wTitle = Math.max(20, maxTitle);
        int maxAuth = Math.min(30, books.stream().map(b -> {
            if (b.getAuthors()!=null && !b.getAuthors().isEmpty() && b.getAuthors().get(0)!=null)
                return b.getAuthors().get(0).getName()==null?0:b.getAuthors().get(0).getName().length();
            return 0;
        }).max(Integer::compareTo).orElse(6));
        int wAuthor = Math.max(10, maxAuth);
        int wId = 6, wLang = 6, wD = 10, wYear = 6;
        String fmtHeader = "│ %-" + (wId-2) + "s │ %-" + (wTitle-2) + "s │ %-" + (wAuthor-2) + "s │ %-" + (wLang-2) + "s │ %" + (wD-1) + "s │%n";
        String sep = "│";
        // top border
        System.out.println(ANSI_GREEN + "┌" + "─".repeat(wId) + "┬" + "─".repeat(wTitle) + "┬" + "─".repeat(wAuthor) + "┬" + "─".repeat(wLang) + "┬" + "─".repeat(wD) + "┐" + ANSI_RESET);
        System.out.printf(fmtHeader, "ID", "TÍTULO", "AUTOR", "IDI", "DESCARGAS");
        System.out.println(ANSI_GREEN + "├" + "─".repeat(wId) + "┼" + "─".repeat(wTitle) + "┼" + "─".repeat(wAuthor) + "┼" + "─".repeat(wLang) + "┼" + "─".repeat(wD) + "┤" + ANSI_RESET);
        for (Book b : books) {
            String title = truncate(b.getTitle(), wTitle-2);
            String author = "—";
            if (b.getAuthors() != null && !b.getAuthors().isEmpty() && b.getAuthors().get(0) != null)
                author = truncate(b.getAuthors().get(0).getName(), wAuthor-2);
            String lang = (b.getLanguages() != null && !b.getLanguages().isEmpty()) ? b.getLanguages().get(0) : "—";
            String downloads = String.valueOf(b.getDownload_count());
            System.out.printf(fmtHeader, b.getId(), title, author, lang, downloads);
        }
        System.out.println(ANSI_GREEN + "└" + "─".repeat(wId) + "┴" + "─".repeat(wTitle) + "┴" + "─".repeat(wAuthor) + "┴" + "─".repeat(wLang) + "┴" + "─".repeat(wD) + "┘" + ANSI_RESET);
    }


    private void printAuthorsTable(List<Author> authors) {
        if (authors == null || authors.isEmpty()) {
            System.out.println(ANSI_YELLOW + "⚠ No hay autores para mostrar." + ANSI_RESET);
            return;
        }
        int wName = 35;
        int wBirth = 8;
        String horiz = "┌" + "─".repeat(wName) + "┬" + "─".repeat(wBirth) + "┐";
        System.out.println(horiz);
        System.out.printf("│ %-"+wName+"s │ %-"+(wBirth-1)+"s │%n", "AUTOR", "AÑO");
        System.out.println("├" + "─".repeat(wName) + "┼" + "─".repeat(wBirth) + "┤");
        for (Author a : authors) {
            String name = truncate(a.getName(), wName);
            String year = a.getBirth_year() != null ? a.getBirth_year().toString() : "—";
            System.out.printf("│ %-"+wName+"s │ %"+(wBirth-2)+"s │%n", name, year);
        }
        System.out.println("└" + "─".repeat(wName) + "┴" + "─".repeat(wBirth) + "┘");
    }

    private String truncate(String s, int w) {
        if (s == null) return "—";
        if (s.length() <= w) return s;
        return s.substring(0, w-3) + "...";
    }

    public DoubleSummaryStatistics downloadStats() {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        List<Book> books = gutendexService.listAllBooks();
        if (books != null) {
            for (Book b : books) {
                stats.accept(b.getDownload_count());
            }
        }
        return stats;
    }
}
