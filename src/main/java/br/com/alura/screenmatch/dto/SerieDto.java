package br.com.alura.screenmatch.dto;

import br.com.alura.screenmatch.model.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record SerieDto(Long id, String titulo, Categoria genero, String atores, String poster, String sinopse,
                       Integer totalTemporadas, Double avaliacao) {
}
