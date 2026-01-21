namespace ReadBookApi.Models;

public class Category
{
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Image { get; set; }
    public int Position { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    
    // Navigation property
    public List<Book> Books { get; set; } = new();
}

