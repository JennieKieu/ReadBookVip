using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Models;
using ReadBookApi.Models.DTOs;

namespace ReadBookApi.Services;

public class AdvertisementService : IAdvertisementService
{
    private readonly ApplicationDbContext _context;

    public AdvertisementService(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<List<AdvertisementDto>> GetAllAdvertisementsAsync()
    {
        var advertisements = await _context.Advertisements
            .OrderByDescending(a => a.CreatedAt)
            .ToListAsync();

        return advertisements.Select(a => new AdvertisementDto
        {
            Id = a.Id,
            Title = a.Title,
            VideoUrl = a.VideoUrl,
            Url = a.Url,
            ThumbnailUrl = a.ThumbnailUrl,
            IsActive = a.IsActive,
            CreatedAt = a.CreatedAt,
            UpdatedAt = a.UpdatedAt,
            ViewCount = a.ViewCount
        }).ToList();
    }

    public async Task<AdvertisementDto?> GetAdvertisementByIdAsync(long id)
    {
        var advertisement = await _context.Advertisements.FindAsync(id);
        if (advertisement == null) return null;

        return new AdvertisementDto
        {
            Id = advertisement.Id,
            Title = advertisement.Title,
            VideoUrl = advertisement.VideoUrl,
            Url = advertisement.Url,
            ThumbnailUrl = advertisement.ThumbnailUrl,
            IsActive = advertisement.IsActive,
            CreatedAt = advertisement.CreatedAt,
            UpdatedAt = advertisement.UpdatedAt,
            ViewCount = advertisement.ViewCount
        };
    }

    public async Task<List<AdvertisementDto>> GetActiveAdvertisementsAsync()
    {
        var advertisements = await _context.Advertisements
            .Where(a => a.IsActive)
            .OrderByDescending(a => a.CreatedAt)
            .ToListAsync();

        return advertisements.Select(a => new AdvertisementDto
        {
            Id = a.Id,
            Title = a.Title,
            VideoUrl = a.VideoUrl,
            Url = a.Url,
            ThumbnailUrl = a.ThumbnailUrl,
            IsActive = a.IsActive,
            CreatedAt = a.CreatedAt,
            UpdatedAt = a.UpdatedAt,
            ViewCount = a.ViewCount
        }).ToList();
    }

    public async Task<AdvertisementDto> CreateAdvertisementAsync(CreateAdvertisementDto createDto)
    {
        var advertisement = new Advertisement
        {
            Title = createDto.Title,
            VideoUrl = createDto.VideoUrl,
            Url = createDto.Url,
            ThumbnailUrl = createDto.ThumbnailUrl,
            IsActive = createDto.IsActive,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow,
            ViewCount = 0
        };

        _context.Advertisements.Add(advertisement);
        await _context.SaveChangesAsync();

        return new AdvertisementDto
        {
            Id = advertisement.Id,
            Title = advertisement.Title,
            VideoUrl = advertisement.VideoUrl,
            Url = advertisement.Url,
            ThumbnailUrl = advertisement.ThumbnailUrl,
            IsActive = advertisement.IsActive,
            CreatedAt = advertisement.CreatedAt,
            UpdatedAt = advertisement.UpdatedAt,
            ViewCount = advertisement.ViewCount
        };
    }

    public async Task<AdvertisementDto?> UpdateAdvertisementAsync(long id, UpdateAdvertisementDto updateDto)
    {
        var advertisement = await _context.Advertisements.FindAsync(id);
        if (advertisement == null) return null;

        advertisement.Title = updateDto.Title;
        advertisement.VideoUrl = updateDto.VideoUrl;
        advertisement.Url = updateDto.Url;
        advertisement.ThumbnailUrl = updateDto.ThumbnailUrl;
        advertisement.IsActive = updateDto.IsActive;
        advertisement.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        return new AdvertisementDto
        {
            Id = advertisement.Id,
            Title = advertisement.Title,
            VideoUrl = advertisement.VideoUrl,
            Url = advertisement.Url,
            ThumbnailUrl = advertisement.ThumbnailUrl,
            IsActive = advertisement.IsActive,
            CreatedAt = advertisement.CreatedAt,
            UpdatedAt = advertisement.UpdatedAt,
            ViewCount = advertisement.ViewCount
        };
    }

    public async Task<bool> DeleteAdvertisementAsync(long id)
    {
        var advertisement = await _context.Advertisements.FindAsync(id);
        if (advertisement == null) return false;

        _context.Advertisements.Remove(advertisement);
        await _context.SaveChangesAsync();
        return true;
    }

    public async Task IncrementViewCountAsync(long id)
    {
        var advertisement = await _context.Advertisements.FindAsync(id);
        if (advertisement != null)
        {
            advertisement.ViewCount++;
            await _context.SaveChangesAsync();
        }
    }
}

