namespace ReadBookApi.Models;

public class BookFavorite
{
    public long Id { get; set; }
    public long BookId { get; set; }
    public string UserEmail { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }
}

