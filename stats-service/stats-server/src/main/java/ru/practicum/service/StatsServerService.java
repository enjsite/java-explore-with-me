package ru.practicum.service;

import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsServerService {
    void addHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getHitsWithViews(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
