namespace ReadBookApi.Models;

public class Book
{
    public long Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string? Image { get; set; }
    public string? Banner { get; set; }
    public long? CategoryId { get; set; }
    public string? CategoryName { get; set; }
    public bool Featured { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    
    // Navigation properties
    public List<Chapter> Chapters { get; set; } = new();
}

