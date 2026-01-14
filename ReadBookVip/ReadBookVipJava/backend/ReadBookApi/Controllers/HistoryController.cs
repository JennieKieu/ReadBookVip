using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Models.DTOs;

namespace ReadBookApi.Controllers;

[ApiController]
[Route("api/books/{bookId}/history")]
public class HistoryController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public HistoryController(ApplicationDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<BookHistoryDto>> GetHistory(long bookId, [FromQuery] string userEmail)
    {
        var history = await _context.BookHistories
            .FirstOrDefaultAsync(h => h.BookId == bookId && h.UserEmail == userEmail);

        if (history == null) return NotFound();

        return Ok(new BookHistoryDto
        {
            Id = history.Id,
            BookId = history.BookId,
            UserEmail = history.UserEmail,
            ChapterId = history.ChapterId,
            ChapterNumber = history.ChapterNumber,
            LastReadAt = history.LastReadAt
        });
    }

    [HttpPost]
    public async Task<ActionResult<BookHistoryDto>> SaveHistory(long bookId, SaveHistoryDto saveHistoryDto)
    {
        if (saveHistoryDto.BookId != bookId)
            return BadRequest("BookId mismatch");

        var existingHistory = await _context.BookHistories
            .FirstOrDefaultAsync(h => h.BookId == bookId && h.UserEmail == saveHistoryDto.UserEmail);

        if (existingHistory != null)
        {
            existingHistory.ChapterId = saveHistoryDto.ChapterId;
            existingHistory.ChapterNumber = saveHistoryDto.ChapterNumber;
            existingHistory.LastReadAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return Ok(new BookHistoryDto
            {
                Id = existingHistory.Id,
                BookId = existingHistory.BookId,
                UserEmail = existingHistory.UserEmail,
                ChapterId = existingHistory.ChapterId,
                ChapterNumber = existingHistory.ChapterNumber,
                LastReadAt = existingHistory.LastReadAt
            });
        }

        var history = new Models.BookHistory
        {
            BookId = saveHistoryDto.BookId,
            UserEmail = saveHistoryDto.UserEmail,
            ChapterId = saveHistoryDto.ChapterId,
            ChapterNumber = saveHistoryDto.ChapterNumber,
            LastReadAt = DateTime.UtcNow
        };

        _context.BookHistories.Add(history);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetHistory), new { bookId, userEmail = saveHistoryDto.UserEmail },
            new BookHistoryDto
            {
                Id = history.Id,
                BookId = history.BookId,
                UserEmail = history.UserEmail,
                ChapterId = history.ChapterId,
                ChapterNumber = history.ChapterNumber,
                LastReadAt = history.LastReadAt
            });
    }
}

