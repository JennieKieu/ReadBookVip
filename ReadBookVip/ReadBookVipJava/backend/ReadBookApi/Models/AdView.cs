namespace ReadBookApi.Models;

public class AdView
{
    public long Id { get; set; }
    public long AdvertisementId { get; set; }
    public string AdvertisementTitle { get; set; } = string.Empty; // For easier querying
    public string UserEmail { get; set; } = string.Empty;
    public DateTime ViewedAt { get; set; } = DateTime.UtcNow;
    public int Duration { get; set; } = 0; // Duration in seconds (optional)
    public bool Completed { get; set; } = false; // Whether user watched full ad or skipped
    
    // Navigation property
    public Advertisement? Advertisement { get; set; }
}

