using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Models;
using ReadBookApi.Models.DTOs;

namespace ReadBookApi.Services;

public class BookService : IBookService
{
    private readonly ApplicationDbContext _context;

    public BookService(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<List<BookDto>> GetAllBooksAsync()
    {
        var books = await _context.Books
            .Include(b => b.Chapters)
            .OrderByDescending(b => b.CreatedAt)
            .ToListAsync();

        return books.Select(b => new BookDto
        {
            Id = b.Id,
            Title = b.Title,
            Image = b.Image,
            Banner = b.Banner,
            CategoryId = b.CategoryId,
            CategoryName = b.CategoryName,
            Description = b.Description,
            Tags = b.Tags,
            Status = b.Status ?? "ongoing", // Default to 'ongoing' if NULL
            Featured = b.Featured,
            ChapterCount = b.Chapters.Count
        }).ToList();
    }

    public async Task<BookDto?> GetBookByIdAsync(long id)
    {
        var book = await _context.Books
            .Include(b => b.Chapters)
            .FirstOrDefaultAsync(b => b.Id == id);

        if (book == null) return null;

        return new BookDto
        {
            Id = book.Id,
            Title = book.Title,
            Image = book.Image,
            Banner = book.Banner,
            CategoryId = book.CategoryId,
            CategoryName = book.CategoryName,
            Featured = book.Featured,
            ChapterCount = book.Chapters.Count
        };
    }

    public async Task<List<BookDto>> GetFeaturedBooksAsync()
    {
        var books = await _context.Books
            .Include(b => b.Chapters)
            .Where(b => b.Featured)
            .OrderByDescending(b => b.CreatedAt)
            .ToListAsync();

        return books.Select(b => new BookDto
        {
            Id = b.Id,
            Title = b.Title,
            Image = b.Image,
            Banner = b.Banner,
            CategoryId = b.CategoryId,
            CategoryName = b.CategoryName,
            Description = b.Description,
            Tags = b.Tags,
            Status = b.Status ?? "ongoing", // Default to 'ongoing' if NULL
            Featured = b.Featured,
            ChapterCount = b.Chapters.Count
        }).ToList();
    }

    public async Task<List<BookDto>> GetBooksByCategoryAsync(long categoryId)
    {
        var books = await _context.Books
            .Include(b => b.Chapters)
            .Where(b => b.CategoryId == categoryId)
            .OrderByDescending(b => b.CreatedAt)
            .ToListAsync();

        return books.Select(b => new BookDto
        {
            Id = b.Id,
            Title = b.Title,
            Image = b.Image,
            Banner = b.Banner,
            CategoryId = b.CategoryId,
            CategoryName = b.CategoryName,
            Description = b.Description,
            Tags = b.Tags,
            Status = b.Status ?? "ongoing", // Default to 'ongoing' if NULL
            Featured = b.Featured,
            ChapterCount = b.Chapters.Count
        }).ToList();
    }

    public async Task<BookDto> CreateBookAsync(CreateBookDto createBookDto)
    {
        var book = new Book
        {
            Title = createBookDto.Title,
            Image = createBookDto.Image,
            Banner = createBookDto.Banner,
            CategoryId = createBookDto.CategoryId,
            CategoryName = createBookDto.CategoryName,
            Description = createBookDto.Description,
            Tags = createBookDto.Tags,
            Status = string.IsNullOrEmpty(createBookDto.Status) ? "ongoing" : createBookDto.Status,
            Featured = createBookDto.Featured,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        _context.Books.Add(book);
        await _context.SaveChangesAsync();

        return new BookDto
        {
            Id = book.Id,
            Title = book.Title,
            Image = book.Image,
            Banner = book.Banner,
            CategoryId = book.CategoryId,
            CategoryName = book.CategoryName,
            Description = book.Description,
            Tags = book.Tags,
            Status = book.Status,
            Featured = book.Featured,
            ChapterCount = 0
        };
    }

    public async Task<BookDto?> UpdateBookAsync(long id, UpdateBookDto updateBookDto)
    {
        var book = await _context.Books.FindAsync(id);
        if (book == null) return null;

        book.Title = updateBookDto.Title;
        book.Image = updateBookDto.Image;
        book.Banner = updateBookDto.Banner;
        book.CategoryId = updateBookDto.CategoryId;
        book.CategoryName = updateBookDto.CategoryName;
        book.Description = updateBookDto.Description;
        book.Tags = updateBookDto.Tags;
        book.Status = string.IsNullOrEmpty(updateBookDto.Status) ? "ongoing" : updateBookDto.Status;
        book.Featured = updateBookDto.Featured;
        book.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        var chapterCount = await _context.Chapters.CountAsync(c => c.BookId == id);

        return new BookDto
        {
            Id = book.Id,
            Title = book.Title,
            Image = book.Image,
            Banner = book.Banner,
            CategoryId = book.CategoryId,
            CategoryName = book.CategoryName,
            Description = book.Description,
            Tags = book.Tags,
            Status = book.Status,
            Featured = book.Featured,
            ChapterCount = chapterCount
        };
    }

    public async Task<bool> DeleteBookAsync(long id)
    {
        var book = await _context.Books.FindAsync(id);
        if (book == null) return false;

        _context.Books.Remove(book);
        await _context.SaveChangesAsync();
        return true;
    }
}

