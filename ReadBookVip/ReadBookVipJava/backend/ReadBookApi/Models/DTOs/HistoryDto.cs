namespace ReadBookApi.Models.DTOs;

public class BookHistoryDto
{
    public long Id { get; set; }
    public long BookId { get; set; }
    public string UserEmail { get; set; } = string.Empty;
    public long? ChapterId { get; set; }
    public int ChapterNumber { get; set; }
    public DateTime LastReadAt { get; set; }
}

public class SaveHistoryDto
{
    public long BookId { get; set; }
    public string UserEmail { get; set; } = string.Empty;
    public long? ChapterId { get; set; }
    public int ChapterNumber { get; set; }
}

