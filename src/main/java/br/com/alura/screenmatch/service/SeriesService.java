package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.SerieRepository;
import br.com.alura.screenmatch.controller.EpisodioDto;
import br.com.alura.screenmatch.dto.SerieDto;
import br.com.alura.screenmatch.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeriesService {
    @Autowired
    private SerieRepository serieRepository;

    private ConverteDados conversor = new ConverteDados();
    private ConsumoApi consumo = new ConsumoApi();

    @Value("${endereco}")
    private String endereco;
    @Value("${apikey}")
    private String apikey;

    public List<SerieDto> buscarTodasSeries() {
        return converteDados(serieRepository.findAll());
    }

    public List<SerieDto> buscarTop5Series() {
        return converteDados(serieRepository.findTop5ByOrderByAvaliacaoDesc());
    }

    public List<SerieDto> obterLancamentos() {
        return converteDados(serieRepository.encontrarEpisodiosMaisRecentes());

    }

    public SerieDto obterPorId(Long id) {

        Optional<Serie> serieOpt = serieRepository.findById(id);
        if(serieOpt.isPresent()){
            Serie s = serieOpt.get();
            return new SerieDto(s.getId(), s.getTitulo(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse(),
                    s.getTotalTemporadas(), s.getAvaliacao());
        }

        return null;
    }

    private List<SerieDto>converteDados(List<Serie> series){
        return series
                .stream()
                .map(s -> new SerieDto(s.getId(), s.getTitulo(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse(), s.getTotalTemporadas(),
                        s.getAvaliacao())).collect(Collectors.toList());
    }

    public void obterSeriePorTitulo(String titulo) {
        DadosSerie dados = getDadosSerie(titulo);
        Serie serie = new Serie(dados);
        serieRepository.save(serie);
    }

    private DadosSerie getDadosSerie(String titulo) {
        var json = consumo.obterDados(endereco + titulo.replace(" ", "+") + apikey);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    public void obterEpisodiosPorTitulo(String titulo) {

        List<DadosTemporada> temporadas = new ArrayList<>();

        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(titulo);

        if (serie.isPresent()){

            var serieEncontrada = serie.get();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(endereco + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + apikey);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        }else {
            System.out.println("Série não encontrada");
        }
    }

    public List<EpisodioDto> obterTodasTemporadas(Long id) {

        Optional<Serie> serieOpt = serieRepository.findById(id);
        if(serieOpt.isPresent()){
            List<Episodio> episodioList = serieOpt.get().getEpisodios();

            return episodioList.stream()
                    .map(e -> new EpisodioDto(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                    .collect(Collectors.toList());
        }

        return null;
    }

    public List<EpisodioDto> obterTemporadas(Long id, Integer idTemporada) {

        List<Episodio> episodios = serieRepository.buscarTemporada(id,idTemporada);

            return episodios.stream()
                    .map(e -> new EpisodioDto(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                    .collect(Collectors.toList());
    }

    public List<SerieDto> obterSeriesPorGenero(String genero) {

        List<Serie> byGenero = serieRepository.findByGenero(Categoria.fromPortugues(genero));
        return converteDados(byGenero);

    }
}
