namespace ReadBookApi.Models.DTOs;

public class CategoryDto
{
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Image { get; set; }
    public int Position { get; set; }
    public int Count { get; set; } // Number of books in this category
}

public class CreateCategoryDto
{
    public string Name { get; set; } = string.Empty;
    public string? Image { get; set; }
    public int Position { get; set; }
}

public class UpdateCategoryDto
{
    public string? Name { get; set; }
    public string? Image { get; set; }
    public int? Position { get; set; }
}

