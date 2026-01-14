namespace ReadBookApi.Models;

public class Chapter
{
    public long Id { get; set; }
    public long BookId { get; set; }
    public int ChapterNumber { get; set; }
    public string? Title { get; set; }
    public string Content { get; set; } = string.Empty; // HTML content
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    
    // Navigation property
    public Book? Book { get; set; }
}

