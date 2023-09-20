package ru.practicum.category.service;

import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.category.model.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto add(NewCategoryDto body);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto get(Long categoryId);

    void delete(Long catId);

    CategoryDto update(Long categoryId, NewCategoryDto newCategoryDto);
}
