using Microsoft.AspNetCore.Mvc;
using ReadBookApi.Models.DTOs;
using ReadBookApi.Services;

namespace ReadBookApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CategoriesController : ControllerBase
{
    private readonly ICategoryService _categoryService;

    public CategoriesController(ICategoryService categoryService)
    {
        _categoryService = categoryService;
    }

    [HttpGet]
    public async Task<ActionResult<List<CategoryDto>>> GetAllCategories()
    {
        var categories = await _categoryService.GetAllCategoriesAsync();
        return Ok(categories);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<CategoryDto>> GetCategory(long id)
    {
        var category = await _categoryService.GetCategoryByIdAsync(id);
        if (category == null) return NotFound();
        return Ok(category);
    }

    [HttpPost]
    public async Task<ActionResult<CategoryDto>> CreateCategory(CreateCategoryDto createCategoryDto)
    {
        var category = await _categoryService.CreateCategoryAsync(createCategoryDto);
        return CreatedAtAction(nameof(GetCategory), new { id = category.Id }, category);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<CategoryDto>> UpdateCategory(long id, UpdateCategoryDto updateCategoryDto)
    {
        var category = await _categoryService.UpdateCategoryAsync(id, updateCategoryDto);
        if (category == null) return NotFound();
        return Ok(category);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteCategory(long id)
    {
        var result = await _categoryService.DeleteCategoryAsync(id);
        if (!result) return NotFound();
        return NoContent();
    }
}

