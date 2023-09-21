package ru.practicum.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.category.model.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.RequestConflictException;
import ru.practicum.exception.SQLConstraintViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryMapper categoryMapper;
    CategoryRepository categoryRepository;
    EventRepository eventRepository;

    String categoryIsNotExistErr = "Категория с id %d не существует.";

    @Override
    public CategoryDto add(NewCategoryDto newCategoryDto) {

        Category category = categoryMapper.newCategoryDtoToCategory(newCategoryDto);

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new SQLConstraintViolationException(String.format("Имя категории должно быть уникальным %s.", newCategoryDto.getName()));
        }

        return categoryMapper.categoryToCategoryDto(category);
    }

    @Override
    public CategoryDto get(Long catId) {

        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(categoryIsNotExistErr, catId));
        });

        return categoryMapper.categoryToCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {

        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(from / size, size, sort);

        List<CategoryDto> categories = categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::categoryToCategoryDto)
                .collect(Collectors.toList());

        return (!categories.isEmpty()) ? categories : new ArrayList<>();
    }

    @Override
    public void delete(Long catId) {

        if (eventRepository.existsByCategoryId(catId)) {
            throw new RequestConflictException("Ошибка удаления категории.");
        }

        try {
            categoryRepository.deleteById(catId);
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException(String.format(categoryIsNotExistErr, catId));
        }
    }

    @Override
    public CategoryDto update(Long catId, NewCategoryDto categoryDto) {

        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(categoryIsNotExistErr, catId));
        });

        category.setName(categoryDto.getName());

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new SQLConstraintViolationException(String.format("Имя категории должно быть уникальным %s.", categoryDto.getName()));
        }

        return categoryMapper.categoryToCategoryDto(category);
    }
}