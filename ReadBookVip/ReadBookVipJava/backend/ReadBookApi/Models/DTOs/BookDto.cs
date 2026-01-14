namespace ReadBookApi.Models.DTOs;

public class BookDto
{
    public long Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string? Image { get; set; }
    public string? Banner { get; set; }
    public long? CategoryId { get; set; }
    public string? CategoryName { get; set; }
    public bool Featured { get; set; }
    public int ChapterCount { get; set; }
}

public class CreateBookDto
{
    public string Title { get; set; } = string.Empty;
    public string? Image { get; set; }
    public string? Banner { get; set; }
    public long? CategoryId { get; set; }
    public string? CategoryName { get; set; }
    public bool Featured { get; set; }
}

public class UpdateBookDto
{
    public string Title { get; set; } = string.Empty;
    public string? Image { get; set; }
    public string? Banner { get; set; }
    public long? CategoryId { get; set; }
    public string? CategoryName { get; set; }
    public bool Featured { get; set; }
}

