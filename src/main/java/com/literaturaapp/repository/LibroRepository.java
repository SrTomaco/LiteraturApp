package com.literaturaapp.repository;

import com.literaturaapp.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Integer> {

    @Query("select l from Libro l join l.languages lang where lower(lang) = lower(:idioma)")
    List<Libro> findByLanguage(@Param("idioma") String idioma);

    List<Libro> findTop10ByOrderByDownloadCountDesc();

    Libro findByTitleIgnoreCase(String titulo);
}
