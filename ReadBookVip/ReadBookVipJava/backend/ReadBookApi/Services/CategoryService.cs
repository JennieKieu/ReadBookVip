using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Models;
using ReadBookApi.Models.DTOs;

namespace ReadBookApi.Services;

public class CategoryService : ICategoryService
{
    private readonly ApplicationDbContext _context;

    public CategoryService(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<List<CategoryDto>> GetAllCategoriesAsync()
    {
        return await _context.Categories
            .OrderBy(c => c.Position)
            .Select(c => new CategoryDto
            {
                Id = c.Id,
                Name = c.Name,
                Image = c.Image,
                Position = c.Position,
                Count = _context.Books.Count(b => b.CategoryId == c.Id)
            })
            .ToListAsync();
    }

    public async Task<CategoryDto?> GetCategoryByIdAsync(long id)
    {
        var category = await _context.Categories.FindAsync(id);
        if (category == null) return null;

        return new CategoryDto
        {
            Id = category.Id,
            Name = category.Name,
            Image = category.Image,
            Position = category.Position,
            Count = await _context.Books.CountAsync(b => b.CategoryId == id)
        };
    }

    public async Task<CategoryDto> CreateCategoryAsync(CreateCategoryDto dto)
    {
        var category = new Category
        {
            Name = dto.Name,
            Image = dto.Image,
            Position = dto.Position,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        _context.Categories.Add(category);
        await _context.SaveChangesAsync();

        return new CategoryDto
        {
            Id = category.Id,
            Name = category.Name,
            Image = category.Image,
            Position = category.Position,
            Count = 0
        };
    }

    public async Task<CategoryDto?> UpdateCategoryAsync(long id, UpdateCategoryDto dto)
    {
        var category = await _context.Categories.FindAsync(id);
        if (category == null) return null;

        if (dto.Name != null) category.Name = dto.Name;
        if (dto.Image != null) category.Image = dto.Image;
        if (dto.Position.HasValue) category.Position = dto.Position.Value;
        category.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        return new CategoryDto
        {
            Id = category.Id,
            Name = category.Name,
            Image = category.Image,
            Position = category.Position,
            Count = await _context.Books.CountAsync(b => b.CategoryId == id)
        };
    }

    public async Task<bool> DeleteCategoryAsync(long id)
    {
        var category = await _context.Categories.FindAsync(id);
        if (category == null) return false;

        _context.Categories.Remove(category);
        await _context.SaveChangesAsync();
        return true;
    }
}

