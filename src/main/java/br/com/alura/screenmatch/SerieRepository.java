package br.com.alura.screenmatch;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie>  findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeAtor, Double avaliacao);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    List<Serie> findByGenero(Categoria categoria);

    List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(int totalTemporadas, double avaliacao);

    @Query("select s from Serie s where s.totalTemporadas <= :totalTemporadas and s.avaliacao >= :avaliacao")
    List<Serie> findByTotalTemporadasAndAvaliacao(int totalTemporadas, double avaliacao);

    @Query("select e from Serie s join s.episodios e where s = :serieBuscada order by e.avaliacao desc limit 5")
    List<Episodio> findTop5Episodios(Optional<Serie> serieBuscada);

    @Query("select e from Serie s join s.episodios e where s = :serie and year(e.dataLancamento) >= :ano ")
    List<Episodio> buscarEpisodiosPorAno(Serie serie, int ano);
}
