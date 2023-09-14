package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.App;
import ru.practicum.model.Hit;
import ru.practicum.repository.AppRepository;
import ru.practicum.repository.HitsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatsServerServiceImpl implements StatsServerService {

    private final HitsRepository hitsRepository;
    private final AppRepository appRepository;
    private final HitMapper mapper;

    public void addHit(EndpointHitDto endpointHitDto) {
        Optional<App> optionalApp = appRepository.findByName(endpointHitDto.getApp());

        App app = optionalApp.orElseGet(() -> appRepository.save(new App(endpointHitDto.getApp())));

        Hit hit = mapper.toHit(endpointHitDto);
        hit.setApp(app);
        hitsRepository.save(hit);
    }

    public List<ViewStatsDto> getHitsWithViews(LocalDateTime start,
                                               LocalDateTime end,
                                               List<String> uris,
                                               Boolean unique) {

        if (unique) {
            if (uris == null || uris.isEmpty()) {
                return hitsRepository.getUniqueIpHitsWithoutUri(start, end);
            }
            return hitsRepository.getUniqueIpHitsWithUri(start, end, uris);
        } else {
            if (uris == null || uris.isEmpty()) {
                return hitsRepository.getAllHitsWithoutUri(start, end);
            }
            return hitsRepository.getAllHitsWithUri(start, end, uris);
        }
    }
}
