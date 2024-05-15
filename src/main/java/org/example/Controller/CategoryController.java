package org.example.Controller;

import org.example.Domain.Models.Category.GetAllCategoriesResponse;
import org.example.Service.Interfaces.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/category")
public class CategoryController {
    private final ICategoryService categoryService;

    @Autowired
    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(path = "")
    public ResponseEntity<List<GetAllCategoriesResponse>> getAllCategories(
    ) {
        List<GetAllCategoriesResponse> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(categories);
    }
}
