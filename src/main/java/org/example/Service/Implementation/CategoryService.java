package org.example.Service.Implementation;

import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Category.GetAllCategoriesResponse;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;
import org.example.Repository.CategoryRepository;
import org.example.Service.Interfaces.ICategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final Mapper mapper;

    public CategoryService(CategoryRepository categoryRepository, Mapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    public List<GetAllCategoriesResponse> getAllCategories() {
        List<GetAllCategoriesResponse> list = new ArrayList<>();
        categoryRepository.findAll().forEach(category -> list.add(mapper.CategoryToGetAllCategoriesResponse(category)));
        return list;
    }
}
