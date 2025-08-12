package com.literaturaapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Autor {
    private String name;
    private Integer birth_year;
    private Integer death_year;

    public Autor() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getBirth_year() { return birth_year; }
    public void setBirth_year(Integer birth_year) { this.birth_year = birth_year; }

    public Integer getDeath_year() { return death_year; }
    public void setDeath_year(Integer death_year) { this.death_year = death_year; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) sb.append(name);
        if (birth_year != null) sb.append(" (nac: " + birth_year + (death_year != null ? " - " + death_year : "") + ")");
        return sb.toString();
    }
}
