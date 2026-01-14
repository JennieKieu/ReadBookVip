using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Models.DTOs;

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

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteChapter(long id)
    {
        var chapter = await _context.Chapters.FindAsync(id);
        if (chapter == null) return NotFound();

        _context.Chapters.Remove(chapter);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}

