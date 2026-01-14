using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Models;

namespace ReadBookApi.Controllers;

[ApiController]
[Route("api/books")]
public class FavoritesController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public FavoritesController(ApplicationDbContext context)
    {
        _context = context;
    }

    [HttpGet("favorites")]
    public async Task<ActionResult<List<long>>> GetFavorites([FromQuery] string userEmail)
    {
        var favoriteBookIds = await _context.BookFavorites
            .Where(f => f.UserEmail == userEmail)
            .Select(f => f.BookId)
            .ToListAsync();

        return Ok(favoriteBookIds);
    }

    [HttpPost("{bookId}/favorites")]
    public async Task<IActionResult> AddFavorite(long bookId, [FromQuery] string userEmail)
    {
        var exists = await _context.BookFavorites
            .AnyAsync(f => f.BookId == bookId && f.UserEmail == userEmail);

        if (exists) return Ok(); // Already favorited

        var favorite = new BookFavorite
        {
            BookId = bookId,
            UserEmail = userEmail,
            CreatedAt = DateTime.UtcNow
        };

        _context.BookFavorites.Add(favorite);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetFavorites), new { userEmail }, favorite);
    }

    [HttpDelete("{bookId}/favorites")]
    public async Task<IActionResult> RemoveFavorite(long bookId, [FromQuery] string userEmail)
    {
        var favorite = await _context.BookFavorites
            .FirstOrDefaultAsync(f => f.BookId == bookId && f.UserEmail == userEmail);

        if (favorite == null) return NotFound();

        _context.BookFavorites.Remove(favorite);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}

