package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitsRepository extends JpaRepository<Hit, Integer> {

    @Query(value = "SELECT new ru.practicum.ViewStatsDto(a.name, h.uri, COUNT(h.ip)) " +
            "FROM Hit as h " +
            "LEFT JOIN App as a ON a.id = h.app.id " +
            "WHERE h.timestamp between ?1 AND ?2 " +
            "AND h.uri IN (?3) " +
            "GROUP BY a.name, h.uri " +
            "ORDER BY COUNT(h.ip) DESC ")
    List<ViewStatsDto> getAllHitsWithUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT new ru.practicum.ViewStatsDto(a.name, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit as h " +
            "LEFT JOIN App as a ON a.id = h.app.id " +
            "WHERE h.timestamp between ?1 AND ?2 " +
            "AND h.uri IN (?3) " +
            "GROUP BY a.name, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC ")
    List<ViewStatsDto> getUniqueIpHitsWithUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT new ru.practicum.ViewStatsDto(a.name, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit as h " +
            "LEFT JOIN App as a ON a.id = h.app.id " +
            "WHERE h.timestamp between ?1 AND ?2 " +
            "GROUP BY a.name, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC ")
    List<ViewStatsDto> getUniqueIpHitsWithoutUri(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT new ru.practicum.ViewStatsDto(a.name, h.uri, COUNT(h.ip)) " +
            "FROM Hit as h " +
            "LEFT JOIN App as a ON a.id = h.app.id " +
            "WHERE h.timestamp between ?1 AND ?2 " +
            "GROUP BY a.name, h.uri " +
            "ORDER BY COUNT(h.ip) DESC ")
    List<ViewStatsDto> getAllHitsWithoutUri(LocalDateTime start, LocalDateTime end);
}
