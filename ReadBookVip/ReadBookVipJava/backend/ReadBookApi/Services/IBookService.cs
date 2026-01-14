using ReadBookApi.Models;
using ReadBookApi.Models.DTOs;

namespace ReadBookApi.Services;

public interface IBookService
{
    Task<List<BookDto>> GetAllBooksAsync();
    Task<BookDto?> GetBookByIdAsync(long id);
    Task<List<BookDto>> GetFeaturedBooksAsync();
    Task<List<BookDto>> GetBooksByCategoryAsync(long categoryId);
    Task<BookDto> CreateBookAsync(CreateBookDto createBookDto);
    Task<BookDto?> UpdateBookAsync(long id, UpdateBookDto updateBookDto);
    Task<bool> DeleteBookAsync(long id);
}

