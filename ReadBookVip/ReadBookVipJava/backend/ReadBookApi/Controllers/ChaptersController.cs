using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Models.DTOs;
using System.Net;
using System.Text.RegularExpressions;

namespace ReadBookApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ChaptersController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public ChaptersController(ApplicationDbContext context)
    {
        _context = context;
    }

    [HttpGet("books/{bookId}")]
    public async Task<ActionResult<List<ChapterDto>>> GetChaptersByBook(long bookId)
    {
        var chapters = await _context.Chapters
            .Where(c => c.BookId == bookId)
            .OrderBy(c => c.ChapterNumber)
            .Select(c => new ChapterDto
            {
                Id = c.Id,
                BookId = c.BookId,
                ChapterNumber = c.ChapterNumber,
                Title = c.Title,
                Content = c.Content
            })
            .ToListAsync();

        return Ok(chapters);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ChapterDto>> GetChapter(long id)
    {
        var chapter = await _context.Chapters.FindAsync(id);
        if (chapter == null) return NotFound();

        return Ok(new ChapterDto
        {
            Id = chapter.Id,
            BookId = chapter.BookId,
            ChapterNumber = chapter.ChapterNumber,
            Title = chapter.Title,
            Content = chapter.Content
        });
    }

    [HttpPost]
    public async Task<ActionResult<ChapterDto>> CreateChapter(CreateChapterDto createChapterDto)
    {
        var chapter = new Models.Chapter
        {
            BookId = createChapterDto.BookId,
            ChapterNumber = createChapterDto.ChapterNumber,
            Title = createChapterDto.Title,
            Content = createChapterDto.Content,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        _context.Chapters.Add(chapter);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetChapter), new { id = chapter.Id }, new ChapterDto
        {
            Id = chapter.Id,
            BookId = chapter.BookId,
            ChapterNumber = chapter.ChapterNumber,
            Title = chapter.Title,
            Content = chapter.Content
        });
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ChapterDto>> UpdateChapter(long id, UpdateChapterDto updateChapterDto)
    {
        var chapter = await _context.Chapters.FindAsync(id);
        if (chapter == null) return NotFound();

        chapter.ChapterNumber = updateChapterDto.ChapterNumber;
        chapter.Title = updateChapterDto.Title;
        chapter.Content = updateChapterDto.Content;
        chapter.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        return Ok(new ChapterDto
        {
            Id = chapter.Id,
            BookId = chapter.BookId,
            ChapterNumber = chapter.ChapterNumber,
            Title = chapter.Title,
            Content = chapter.Content
        });
    }

    // One-time cleanup for legacy HTML content
    [HttpPost("cleanup-html")]
    public async Task<ActionResult<object>> CleanupHtml([FromQuery] long? bookId)
    {
        var query = _context.Chapters.AsQueryable();
        if (bookId.HasValue)
        {
            query = query.Where(c => c.BookId == bookId.Value);
        }

        var chapters = await query.ToListAsync();
        var updatedCount = 0;

        foreach (var chapter in chapters)
        {
            var cleaned = CleanHtmlToPlainText(chapter.Content);
            if (!string.Equals(cleaned, chapter.Content, StringComparison.Ordinal))
            {
                chapter.Content = cleaned;
                chapter.UpdatedAt = DateTime.UtcNow;
                updatedCount++;
            }
        }

        if (updatedCount > 0)
        {
            await _context.SaveChangesAsync();
        }

        return Ok(new { updated = updatedCount });
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteChapter(long id)
    {
        var chapter = await _context.Chapters.FindAsync(id);
        if (chapter == null) return NotFound();

        _context.Chapters.Remove(chapter);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    private static string CleanHtmlToPlainText(string? html)
    {
        if (string.IsNullOrWhiteSpace(html)) return string.Empty;

        var text = html;
        text = Regex.Replace(text, "(?i)<br\\s*/?>", "\n");
        text = Regex.Replace(text, "(?i)</p>|</div>|</li>", "\n");
        text = Regex.Replace(text, "(?i)<li>", "- ");
        text = Regex.Replace(text, "<[^>]+>", string.Empty);
        text = WebUtility.HtmlDecode(text);

        text = text.Replace("\r\n", "\n").Replace("\r", "\n");
        text = Regex.Replace(text, "\n{3,}", "\n\n");
        return text.Trim();
    }
}

