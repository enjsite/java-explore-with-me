package ru.practicum.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.model.dto.CompilationDto;
import ru.practicum.compilation.model.dto.NewCompilationDto;
import ru.practicum.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationServiceImpl implements CompilationService {

    CompilationRepository compilationRepository;
    EventRepository eventRepository;
    CompilationMapper compilationMapper;

    String compilationIsNotExistErr = "Подборка с id %d не существует";

    @Override
    public CompilationDto add(NewCompilationDto compilationDto) {

        Compilation compilation = compilationMapper.newCompilationDtoToCompilation(compilationDto);

        if (compilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIn(compilationDto.getEvents());
            compilation.setEvents(events);
        }

        compilation = compilationRepository.save(compilation);

        return compilationMapper.compilationToCompilationDto(compilation);
    }

    @Override
    public CompilationDto get(Long compId) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(compilationIsNotExistErr, compId));
        });

        return compilationMapper.compilationToCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {

        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(from / size, size, sort);

        if (pinned != null) {
            List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);
            return compilations.stream().map(compilationMapper::compilationToCompilationDto).collect(Collectors.toList());
        } else {
            Page<Compilation> compilations = compilationRepository.findAll(pageable);
            return compilations.stream().map(compilationMapper::compilationToCompilationDto).collect(Collectors.toList());
        }
    }

    @Override
    public void delete(Long compId) {

        try {
            compilationRepository.deleteById(compId);
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException(String.format(compilationIsNotExistErr, compId));
        }
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest compRequest) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(compilationIsNotExistErr, compId));
        });

        updateCompilation(compilation, compRequest);
        compilation = compilationRepository.save(compilation);

        return compilationMapper.compilationToCompilationDto(compilation);
    }

    private void updateCompilation(Compilation compilation, UpdateCompilationRequest compRequest) {

        if (compRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIn(compRequest.getEvents());
            compilation.setEvents(events);
        }

        if (compRequest.getTitle() != null) {
            compilation.setTitle(compRequest.getTitle());
        }

        if (compRequest.getPinned() != null) {
            compilation.setPinned(compRequest.getPinned());
        }
    }
}
