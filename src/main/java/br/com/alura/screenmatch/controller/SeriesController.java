package br.com.alura.screenmatch.controller;

import br.com.alura.screenmatch.dto.SerieDto;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.SeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("series")
public class SeriesController {
    @Autowired
    private SeriesService service;

    @GetMapping()
    public List<SerieDto> buscarSeries() {
        return service.buscarTodasSeries();
    }

    @GetMapping("/top5")
    public List<SerieDto> buscarTop5Series() {
        return service.buscarTop5Series();
    }

    @GetMapping("/lancamentos")
    public List<SerieDto> obterlancamentos() {
        return service.obterLancamentos();
    }

    @GetMapping("/{id}")
    public SerieDto obterPorId(@PathVariable Long id) {
        return service.obterPorId(id);
    }

    @GetMapping("/buscar/{titulo}")
    public void obterSeriePorTitulo(@PathVariable String titulo) {
        service.obterSeriePorTitulo(titulo);
    }

    @GetMapping("/buscar/episodios/{titulo}")
    public void obterEpisodiosPorTitulo(@PathVariable String titulo) {
        service.obterEpisodiosPorTitulo(titulo);
    }

    @GetMapping("/{id}/temporadas/todas")
    public List<EpisodioDto> obterTodasTemporadas(@PathVariable Long id) {
        return service.obterTodasTemporadas(id);
    }

    @GetMapping("/{id}/temporadas/{idTemporada}")
    public List<EpisodioDto> obterTemporada(@PathVariable Long id, @PathVariable Integer idTemporada) {
        return service.obterTemporadas(id, idTemporada);
    }

    @GetMapping("/categoria/{genero}")
    public List<SerieDto> obterSeriesPorGenero(@PathVariable String genero) {
        return service.obterSeriesPorGenero(genero);
    }
}
