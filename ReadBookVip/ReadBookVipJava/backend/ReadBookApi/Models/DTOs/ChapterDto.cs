namespace ReadBookApi.Models.DTOs;

public class ChapterDto
{
    public long Id { get; set; }
    public long BookId { get; set; }
    public int ChapterNumber { get; set; }
    public string? Title { get; set; }
    public string Content { get; set; } = string.Empty;
}

public class CreateChapterDto
{
    public long BookId { get; set; }
    public int ChapterNumber { get; set; }
    public string? Title { get; set; }
    public string Content { get; set; } = string.Empty;
}

public class UpdateChapterDto
{
    public int ChapterNumber { get; set; }
    public string? Title { get; set; }
    public string Content { get; set; } = string.Empty;
}

