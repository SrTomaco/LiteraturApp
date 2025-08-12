package com.literaturaapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    private Integer id;

    private String title;

    @JsonProperty("download_count")
    private Integer downloadCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "libro_languages")
    @Column(name = "language")
    private List<String> languages;

    // authors from Gutendex; not persisted as a relation in this simple model
    @Transient
    private List<Autor> authors;

    public Libro() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTítulo() { return title; }
    public void setTítulo(String title) { this.title = title; }

    public int getDownloadCount() { return downloadCount == null ? 0 : downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public List<String> getIdiomas() { return languages; }
    public void setIdiomas(List<String> languages) { this.languages = languages; }

    public List<Autor> getAutores() { return authors; }
    public void setAutores(List<Autor> authors) { this.authors = authors; }

    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", downloadCount=" + downloadCount +
                ", languages=" + languages +
                '}';
    }
}
