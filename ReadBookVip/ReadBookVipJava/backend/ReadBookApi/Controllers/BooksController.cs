using Microsoft.AspNetCore.Mvc;
using ReadBookApi.Models.DTOs;
using ReadBookApi.Services;

namespace ReadBookApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class BooksController : ControllerBase
{
    private readonly IBookService _bookService;

    public BooksController(IBookService bookService)
    {
        _bookService = bookService;
    }

    [HttpGet]
    public async Task<ActionResult<List<BookDto>>> GetAllBooks()
    {
        var books = await _bookService.GetAllBooksAsync();
        return Ok(books);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<BookDto>> GetBook(long id)
    {
        var book = await _bookService.GetBookByIdAsync(id);
        if (book == null) return NotFound();
        return Ok(book);
    }

    [HttpGet("featured")]
    public async Task<ActionResult<List<BookDto>>> GetFeaturedBooks()
    {
        var books = await _bookService.GetFeaturedBooksAsync();
        return Ok(books);
    }

    [HttpGet("category/{categoryId}")]
    public async Task<ActionResult<List<BookDto>>> GetBooksByCategory(long categoryId)
    {
        var books = await _bookService.GetBooksByCategoryAsync(categoryId);
        return Ok(books);
    }

    [HttpPost]
    public async Task<ActionResult<BookDto>> CreateBook(CreateBookDto createBookDto)
    {
        var book = await _bookService.CreateBookAsync(createBookDto);
        return CreatedAtAction(nameof(GetBook), new { id = book.Id }, book);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<BookDto>> UpdateBook(long id, UpdateBookDto updateBookDto)
    {
        var book = await _bookService.UpdateBookAsync(id, updateBookDto);
        if (book == null) return NotFound();
        return Ok(book);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteBook(long id)
    {
        var result = await _bookService.DeleteBookAsync(id);
        if (!result) return NotFound();
        return NoContent();
    }
}

