package org.example.Service.Interfaces;

import org.example.Domain.Models.Category.GetAllCategoriesResponse;

import java.util.List;

public interface ICategoryService {
    List<GetAllCategoriesResponse> getAllCategories();
}
