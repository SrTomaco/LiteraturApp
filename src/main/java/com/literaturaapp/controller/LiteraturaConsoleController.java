
package com.literaturaapp.controller;

import com.literaturaapp.service.ServicioLiteratura;
import com.literaturaapp.model.Book;
import com.literaturaapp.model.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;
import java.util.DoubleSummaryStatistics;

@Component
public class LiteraturaConsoleController implements CommandLineRunner {

    private final ServicioLiteratura servicio;
    private final Scanner scanner = new Scanner(System.in);

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";

    @Autowired
    public LiteraturaConsoleController(ServicioLiteratura servicio) {
        this.servicio = servicio;
    }

    @Override
    public void run(String... args) throws Exception {
        mainMenuLoop();
    }

    private void mainMenuLoop() {
        while (true) {
            clearScreen();
            printHeader();
            printMenu();
            System.out.print(ANSI_CYAN + "Seleccione una opción: " + ANSI_RESET);
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "0":
                    farewellAndExit();
                    return;
                case "1":
                    uiSearchByTitle();
                    break;
                case "2":
                    uiListAllBooks();
                    break;
                case "3":
                    uiListAllAuthors();
                    break;
                case "4":
                    uiListAuthorsAliveInYear();
                    break;
                case "5":
                    uiFilterByLanguage();
                    break;
                case "6":
                    uiTop10();
                    break;
                case "7":
                    uiSearchAuthorByName();
                    break;
                case "8":
                    uiShowStatistics();
                    break;
                case "9":
                    servicio.refreshCache();
                    System.out.println(ANSI_GREEN + "Cache refrescada. Pulsa ENTER para continuar..." + ANSI_RESET);
                    scanner.nextLine();
                    break;
                default:
                    System.out.println(ANSI_YELLOW + "Opción inválida. Pulsa ENTER para continuar..." + ANSI_RESET);
                    scanner.nextLine();
                    break;
            }
        }
    }

    private void printHeader() {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "┌" + "─".repeat(33) + "┐" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + "│" + ANSI_CYAN + "      APLICACIÓN LITERATURA    " + " ".repeat(2) + ANSI_BLUE + "│" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + "└" + "─".repeat(33) + "┘" + ANSI_RESET);
        System.out.println();
    }

    private void printMenu() {
        System.out.println(ANSI_BOLD + "  Menú principal" + ANSI_RESET);
        System.out.println("  1) Buscar libro por título");
        System.out.println("  2) Listar todos los libros registrados");
        System.out.println("  3) Listar todos los autores registrados");
        System.out.println("  4) Listar autores vivos en un año dado");
        System.out.println("  5) Filtrar por idioma (listado completo desde la API)");
        System.out.println("  6) Top 10 libros más descargados");
        System.out.println("  7) Buscar autor por nombre");
        System.out.println("  8) Mostrar estadísticas generales");
        System.out.println("  9) Refrescar cache de la API");
        System.out.println("  0) Salir");
        System.out.println();
    }

    private void uiSearchByTitle() {
        System.out.print("Ingresá título (o parte del título): ");
        String q = scanner.nextLine().trim();
        // ask if user wants to apply a language filter
        System.out.print("¿Deseás filtrar por idioma? (s/N): ");
        String ans = scanner.nextLine().trim().toLowerCase();
        String lang = "";
        if (ans.equals("s") || ans.equals("si") || ans.equals("y") || ans.equals("yes")) {
            System.out.print("Ingresá código de idioma (ej: en, es, fr) o dejá vacío para todos: ");
            lang = scanner.nextLine().trim();
        }
        List<Book> res = servicio.searchByTitle(q);
        if (res == null || res.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No se encontraron libros para: " + q + ANSI_RESET);
        } else {
            if (lang != null && !lang.isEmpty()) {
                final String code = lang.toLowerCase();
                res = res.stream().filter(b -> b.getLanguages()!=null && b.getLanguages().stream().anyMatch(l -> l.equalsIgnoreCase(code))).collect(java.util.stream.Collectors.toList());
            }
            printBooksTable(res);
        }
        pause();
    }

    private void uiListAllBooks() {
        List<Book> list = servicio.listAllBooks();
        if (list == null || list.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No hay libros cargados." + ANSI_RESET);
            pause();
            return;
        }
        // ask if user wants to apply a language filter
        System.out.print("¿Deseás filtrar la lista por idioma? (s/N): ");
        String ans = scanner.nextLine().trim().toLowerCase();
        if (ans.equals("s") || ans.equals("si") || ans.equals("y") || ans.equals("yes")) {
            System.out.print("Ingresá código de idioma (ej: en, es, fr) o dejá vacío para todos: ");
            String lang = scanner.nextLine().trim();
            if (lang != null && !lang.isEmpty()) {
                final String code = lang.toLowerCase();
                list = list.stream().filter(b -> b.getLanguages()!=null && b.getLanguages().stream().anyMatch(l -> l.equalsIgnoreCase(code))).collect(java.util.stream.Collectors.toList());
            }
        }
        printBooksTable(list);
        pause();
    }

    private void uiListAllAuthors() {
        List<Author> authors = servicio.listAllAuthorsObjects();
        printAuthorsTable(authors);
        pause();
    }

    private void uiListAuthorsAliveInYear() {
        System.out.print("Ingresá el año: ");
        String s = scanner.nextLine().trim();
        try {
            int year = Integer.parseInt(s);
            List<Author> list = servicio.listAuthorsAliveInYearObjects(year);
            if (list == null || list.isEmpty()) {
                System.out.println(ANSI_YELLOW + "No se encontraron autores vivos en " + year + ANSI_RESET);
            } else {
                printAuthorsTable(list);
            }
        } catch (NumberFormatException ex) {
            System.out.println(ANSI_YELLOW + "Año inválido" + ANSI_RESET);
        }
        pause();
    }

    private void uiFilterByLanguage() {
        System.out.print("Ingresá el código de idioma (ej: en, es, fr) o dejá vacío para listar todos: ");
        String lang = scanner.nextLine().trim();
        List<Book> list = servicio.listBooksByLanguage(lang);
        if (list == null || list.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No se encontraron libros para el idioma: " + (lang.isEmpty() ? "todos" : lang) + ANSI_RESET);
        } else {
            printBooksTable(list);
        }
        pause();
    }

    private void uiTop10() {
        List<Book> top = servicio.topNByDownloads(10);
        printBooksTable(top);
        pause();
    }

    private void uiSearchAuthorByName() {
        System.out.print("Ingresá el nombre (o parte) del autor: ");
        String q = scanner.nextLine().trim();
        List<Author> found = servicio.findAuthorsByName(q);
        if (found == null || found.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No se encontraron autores para: " + q + ANSI_RESET);
        } else {
            printAuthorsTable(found);
        }
        pause();
    }

    private void uiShowStatistics() {
        DoubleSummaryStatistics st = servicio.downloadStats();
        System.out.println("Estadísticas de descargas:");
        System.out.println("  Total libros: " + servicio.listAllBooks().size());
        System.out.println("  Min: " + (long)st.getMin());
        System.out.println("  Max: " + (long)st.getMax());
        System.out.println("  Avg: " + (long)st.getAverage());
        System.out.println("  Sum: " + (long)st.getSum());
        pause();
    }

    // Helpers to print tables (simple, fixed-width)
    private void printBooksTable(List<Book> books) {
        if (books == null || books.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Sin resultados." + ANSI_RESET);
            return;
        }
        int wId = 6, wTitle = 40, wAuth = 20, wLang = 6, wD = 10;
        String sep = "│";
        System.out.println(ANSI_GREEN + "┌" + "─".repeat(wId) + "┬" + "─".repeat(wTitle) + "┬" + "─".repeat(wAuth) + "┬" + "─".repeat(wLang) + "┬" + "─".repeat(wD) + "┐" + ANSI_RESET);
        System.out.printf("│ %-4s │ %-38s │ %-18s │ %-4s │ %-8s │%n", "ID", "TÍTULO", "AUTOR", "IDI", "DESCARGAS");
        System.out.println(ANSI_GREEN + "├" + "─".repeat(wId) + "┼" + "─".repeat(wTitle) + "┼" + "─".repeat(wAuth) + "┼" + "─".repeat(wLang) + "┼" + "─".repeat(wD) + "┤" + ANSI_RESET);
        for (Book b : books) {
            String title = truncate(b.getTitle(), 38);
            String author = "—";
            if (b.getAuthors() != null && !b.getAuthors().isEmpty() && b.getAuthors().get(0) != null) author = truncate(b.getAuthors().get(0).getName(), 18);
            String lang = (b.getLanguages() != null && !b.getLanguages().isEmpty()) ? b.getLanguages().get(0) : "—";
            String downloads = String.valueOf(b.getDownload_count());
            System.out.printf("│ %-4d │ %-38s │ %-18s │ %-4s │ %8s │%n", b.getId(), title, author, lang, downloads);
        }
        System.out.println(ANSI_GREEN + "└" + "─".repeat(wId) + "┴" + "─".repeat(wTitle) + "┴" + "─".repeat(wAuth) + "┴" + "─".repeat(wLang) + "┴" + "─".repeat(wD) + "┘" + ANSI_RESET);
    }

    private void printAuthorsTable(List<Author> authors) {
        if (authors == null || authors.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Sin resultados." + ANSI_RESET);
            return;
        }
        int wName = 40, wBirth = 8, wDeath = 8;
        System.out.println(ANSI_CYAN + "┌" + "─".repeat(wName) + "┬" + "─".repeat(wBirth) + "┬" + "─".repeat(wDeath) + "┐" + ANSI_RESET);
        System.out.printf("│ %-38s │ %-6s │ %-6s │%n", "NOMBRE", "NAC", "FALL");
        System.out.println(ANSI_CYAN + "├" + "─".repeat(wName) + "┼" + "─".repeat(wBirth) + "┼" + "─".repeat(wDeath) + "┤" + ANSI_RESET);
        for (Author a : authors) {
            String name = truncate(a.getName(), 38);
            String b = a.getBirth_year() == null ? "—" : String.valueOf(a.getBirth_year());
            String d = a.getDeath_year() == null ? "—" : String.valueOf(a.getDeath_year());
            System.out.printf("│ %-38s │ %6s │ %6s │%n", name, b, d);
        }
        System.out.println(ANSI_CYAN + "└" + "─".repeat(wName) + "┴" + "─".repeat(wBirth) + "┴" + "─".repeat(wDeath) + "┘" + ANSI_RESET);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max-3) + "...";
    }

    private void pause() {
        System.out.println();
        System.out.print(ANSI_YELLOW + "Pulsa ENTER para continuar..." + ANSI_RESET);
        scanner.nextLine();
    }

    private void clearScreen() {
        System.out.print("\n\n");
    }

    private void farewellAndExit() {
        System.out.println(ANSI_GREEN + "Gracias por usar la aplicación. ¡Adiós!" + ANSI_RESET);
        try { Thread.sleep(400); } catch (InterruptedException e) { /* ignorar */ }
        System.exit(0);
    }
}
