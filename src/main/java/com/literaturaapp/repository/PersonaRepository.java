
package com.literaturaapp.repository;

import com.literaturaapp.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    List<Persona> findByAnioNacimientoLessThanEqualAndAnioFallecimientoGreaterThanEqual(Integer anio, Integer anio2);
    List<Persona> findByNombreContainingIgnoreCase(String nombre);
    List<Persona> findByAnioNacimiento(Integer anioNacimiento);
    List<Persona> findByAnioFallecimiento(Integer anioFallecimiento);
}
