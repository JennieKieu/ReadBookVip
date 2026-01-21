using ReadBookApi.Models.DTOs;

namespace ReadBookApi.Services;

public interface ICategoryService
{
    Task<List<CategoryDto>> GetAllCategoriesAsync();
    Task<CategoryDto?> GetCategoryByIdAsync(long id);
    Task<CategoryDto> CreateCategoryAsync(CreateCategoryDto dto);
    Task<CategoryDto?> UpdateCategoryAsync(long id, UpdateCategoryDto dto);
    Task<bool> DeleteCategoryAsync(long id);
}

