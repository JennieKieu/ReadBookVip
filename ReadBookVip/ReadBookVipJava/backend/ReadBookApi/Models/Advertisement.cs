namespace ReadBookApi.Models;

public class Advertisement
{
    public long Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string VideoUrl { get; set; } = string.Empty; // Google Drive link
    public string? Url { get; set; } // URL to open when ad is clicked
    public string? ThumbnailUrl { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    public int ViewCount { get; set; } = 0;
}

