namespace ReadBookApi.Models;

public class BookHistory
{
    public long Id { get; set; }
    public long BookId { get; set; }
    public string UserEmail { get; set; } = string.Empty;
    public long? ChapterId { get; set; }
    public int ChapterNumber { get; set; }
    public DateTime LastReadAt { get; set; }
}

