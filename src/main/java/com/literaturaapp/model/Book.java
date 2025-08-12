package com.literaturaapp.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private int id;
    private String title;
    private List<Author> authors;
    private List<String> languages;
    private int download_count;
    private Map<String,String> formats;

    public Book() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public int getDownload_count() { return download_count; }
    public void setDownload_count(int download_count) { this.download_count = download_count; }

    public Map<String, String> getFormats() { return formats; }
    public void setFormats(Map<String, String> formats) { this.formats = formats; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("────────────────────────────────────────────────").append("\n");
        sb.append("TÍTULO: ").append(title).append("\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("AUTOR(ES): ");
        if (authors == null || authors.isEmpty()) sb.append("[desconocido]"); else {
            for (int i=0;i<authors.size();i++) {
                sb.append(authors.get(i).toString());
                if (i<authors.size()-1) sb.append(", ");
            }
        }
        sb.append("\n");
        sb.append("IDIOMAS: ").append(languages==null?"[]":languages.toString()).append("\n");
        sb.append("DESCARGAS: ").append(download_count).append("\n");
        if (formats != null && !formats.isEmpty()) {
            sb.append("FORMATOS DISPONIBLES:\n");
            int c=0;
            for (var e: formats.entrySet()) {
                sb.append("  - ").append(e.getKey()).append(" => ").append(e.getValue()).append("\n");
                c++; if (c>=10) { sb.append("  ... (más formatos)\n"); break; }
            }
        }
        sb.append("────────────────────────────────────────────────");
        return sb.toString();
    }
}
