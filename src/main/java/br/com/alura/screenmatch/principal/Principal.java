package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.SerieRepository;
import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    List<Serie> series = new ArrayList<>();
    List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository serieRepository;

    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {

        var opcao = -1;

        while (opcao != 0) {

            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Buscar top 5 séries 
                    7 - Buscar séries por categoria
                    8 - Buscar séries por quantidade de temporadas 
                    9 - Buscar top 5 episódios por serie 
                    10 - Buscar episódios por data
                               
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorNome();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorQuantidadeTemporadas();
                    break;
                case 9:
                    buscarTop5EpisodiosPorSerie();
                    break;

                case 10:
                    buscarEpisodiosPorData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }




    private void listarSeriesBuscadas() {

       series = serieRepository.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        serieRepository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){

        listarSeriesBuscadas();

        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();
        List<DadosTemporada> temporadas = new ArrayList<>();

        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()){

            var serieEncontrada = serie.get();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

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

    private void buscarSeriePorNome() {
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();
        serieBuscada = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()){
            System.out.println("Dados da série " + serieBuscada.get());
        }else {
            System.out.println("Série não encontrada!");
        }

    }

    private void buscarSeriesPorAtor() {
        System.out.println("Escolha uma série pelo nome do ator");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliação da série a partir de ");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesPorAtor = serieRepository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor,avaliacao);

        seriesPorAtor.forEach(s -> System.out.println("Título: " + s.getTitulo() + " Avaliação: " +s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> top5Series = serieRepository.findTop5ByOrderByAvaliacaoDesc();

        top5Series.forEach(s -> System.out.println("Título: " + s.getTitulo() + " Avaliação: " +s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Escolha uma série pela categoria");
        var categoria = leitura.nextLine();


        List<Serie> seriesporCategoria = serieRepository.findByGenero(Categoria.fromPortugues(categoria));

        seriesporCategoria.forEach(System.out::println);

    }

    private void buscarSeriesPorQuantidadeTemporadas() {

        System.out.println("Escolha a quantidade de temporadas");
        var qtdTemporadas = leitura.nextLine();

        System.out.println("Avaliação minima da serie");
        var avaliacao = leitura.nextLine();

        List<Serie> seriesPorTemporada = serieRepository.
                findByTotalTemporadasAndAvaliacao(Integer.parseInt(qtdTemporadas),
                        Double.parseDouble(avaliacao));

        seriesPorTemporada.forEach(System.out::println);
    }

    private void buscarTop5EpisodiosPorSerie() {

        buscarSeriePorNome();

        if (serieBuscada.isPresent()){
            List<Episodio> topEpisodios = serieRepository.findTop5Episodios(serieBuscada);
            topEpisodios.forEach(e -> System.out.printf("Série: %s Temporada %s - Episódio %s - %s Avaliação %s\n",
                    e.getSerie().getTitulo(), e.getTemporada(),
                    e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao() ));
        }

    }

    public void buscarEpisodiosPorData(){
        buscarSeriePorNome();

        if (serieBuscada.isPresent()){
            var serie = serieBuscada.get();

            System.out.println("Episódios a partir do ano");
            var ano = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodiosAno = serieRepository.buscarEpisodiosPorAno(serie,ano);
            episodiosAno.forEach(System.out::println);

        }

    }
}