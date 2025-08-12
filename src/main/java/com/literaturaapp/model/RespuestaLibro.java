
package com.literaturaapp.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaLibro {

    private List<ResultadoLibro> results;

    public List<ResultadoLibro> getResults() {
        return results;
    }

    public void setResults(List<ResultadoLibro> results) {
        this.results = results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultadoLibro {
        private int id;
        private String title;    @ElementCollection

        private List<String> languages;
        private int download_count;
        private List<Autor> authors;

        public int getId() { return id; }
        public String getTitle() { return title; }
        public List<String> getLanguages() { return languages; }
        public int getDownload_count() { return download_count; }
        public List<Autor> getAuthors() { return authors; }

        public void setId(int id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setLanguages(List<String> languages) { this.languages = languages; }
        public void setDownload_count(int download_count) { this.download_count = download_count; }
        public void setAuthors(List<Autor> authors) { this.authors = authors; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Autor {
        private String name;
        private Integer birth_year;
        private Integer death_year;

        public String getName() { return name; }
        public Integer getBirth_year() { return birth_year; }
        public Integer getDeath_year() { return death_year; }

        public void setName(String name) { this.name = name; }
        public void setBirth_year(Integer birth_year) { this.birth_year = birth_year; }
        public void setDeath_year(Integer death_year) { this.death_year = death_year; }
    }
}
