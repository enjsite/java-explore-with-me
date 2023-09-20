package ru.practicum.compilation.service;

import org.springframework.stereotype.Service;
import ru.practicum.compilation.model.dto.CompilationDto;
import ru.practicum.compilation.model.dto.NewCompilationDto;
import ru.practicum.compilation.model.dto.UpdateCompilationRequest;

import java.util.List;

@Service
public interface CompilationService {

    CompilationDto add(NewCompilationDto compilationDto);

    CompilationDto get(Long compId);

    List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size);

    void delete(Long compilationId);

    CompilationDto update(Long compId, UpdateCompilationRequest compilationRequest);
}
