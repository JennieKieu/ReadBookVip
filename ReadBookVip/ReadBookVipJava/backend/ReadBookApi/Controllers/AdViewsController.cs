using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Models;

namespace ReadBookApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AdViewsController : ControllerBase
{
    private readonly ApplicationDbContext _context;

    public AdViewsController(ApplicationDbContext context)
    {
        _context = context;
    }

    // POST: api/AdViews
    // Record a new ad view
    [HttpPost]
    public async Task<ActionResult<AdView>> CreateAdView([FromBody] CreateAdViewDto createDto)
    {
        // Get advertisement title
        var advertisement = await _context.Advertisements
            .FirstOrDefaultAsync(a => a.Id == createDto.AdvertisementId);
        
        if (advertisement == null)
        {
            return BadRequest("Advertisement not found");
        }

        var adView = new AdView
        {
            AdvertisementId = createDto.AdvertisementId,
            AdvertisementTitle = advertisement.Title,
            UserEmail = createDto.UserEmail,
            ViewedAt = DateTime.UtcNow,
            Duration = createDto.Duration,
            Completed = createDto.Completed
        };

        _context.AdViews.Add(adView);
        
        // Also increment view count in Advertisement table
        advertisement.ViewCount++;
        
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetAdView), new { id = adView.Id }, adView);
    }

    // GET: api/AdViews/{id}
    [HttpGet("{id}")]
    public async Task<ActionResult<AdView>> GetAdView(long id)
    {
        var adView = await _context.AdViews.FindAsync(id);
        if (adView == null) return NotFound();
        return Ok(adView);
    }

    // GET: api/AdViews/statistics
    // Get statistics for all advertisements
    [HttpGet("statistics")]
    public async Task<ActionResult<AdStatisticsDto>> GetStatistics()
    {
        var now = DateTime.UtcNow;
        var todayStart = new DateTime(now.Year, now.Month, now.Day, 0, 0, 0, DateTimeKind.Utc);
        var weekStart = todayStart.AddDays(-(int)now.DayOfWeek);
        var monthStart = new DateTime(now.Year, now.Month, 1, 0, 0, 0, DateTimeKind.Utc);

        var allViews = await _context.AdViews.ToListAsync();
        
        var statistics = new AdStatisticsDto
        {
            TotalViews = allViews.Count,
            ViewsToday = allViews.Count(v => v.ViewedAt >= todayStart),
            ViewsWeek = allViews.Count(v => v.ViewedAt >= weekStart),
            ViewsMonth = allViews.Count(v => v.ViewedAt >= monthStart),
            TotalCompleted = allViews.Count(v => v.Completed),
            CompletedToday = allViews.Count(v => v.Completed && v.ViewedAt >= todayStart),
            CompletedWeek = allViews.Count(v => v.Completed && v.ViewedAt >= weekStart),
            CompletedMonth = allViews.Count(v => v.Completed && v.ViewedAt >= monthStart)
        };

        // Get view counts per advertisement
        var viewCountsByAd = allViews
            .GroupBy(v => v.AdvertisementId)
            .Select(g => new AdViewCountDto
            {
                AdvertisementId = g.Key,
                TotalViews = g.Count(),
                CompletedViews = g.Count(v => v.Completed),
                ViewsToday = g.Count(v => v.ViewedAt >= todayStart),
                ViewsWeek = g.Count(v => v.ViewedAt >= weekStart),
                ViewsMonth = g.Count(v => v.ViewedAt >= monthStart)
            })
            .OrderByDescending(v => v.TotalViews)
            .ToList();

        statistics.ViewCountsByAdvertisement = viewCountsByAd;

        // Get top advertisement
        if (viewCountsByAd.Any())
        {
            var topAd = viewCountsByAd.First();
            var topAdvertisement = await _context.Advertisements.FindAsync(topAd.AdvertisementId);
            if (topAdvertisement != null)
            {
                statistics.TopAdvertisementId = topAd.AdvertisementId;
                statistics.TopAdvertisementTitle = topAdvertisement.Title;
                statistics.TopAdvertisementViews = topAd.TotalViews;
            }
        }

        return Ok(statistics);
    }
}

// DTOs
public class CreateAdViewDto
{
    public long AdvertisementId { get; set; }
    public string UserEmail { get; set; } = string.Empty;
    public int Duration { get; set; } = 0;
    public bool Completed { get; set; } = false;
}

public class AdStatisticsDto
{
    public int TotalViews { get; set; }
    public int ViewsToday { get; set; }
    public int ViewsWeek { get; set; }
    public int ViewsMonth { get; set; }
    public int TotalCompleted { get; set; }
    public int CompletedToday { get; set; }
    public int CompletedWeek { get; set; }
    public int CompletedMonth { get; set; }
    public long? TopAdvertisementId { get; set; }
    public string? TopAdvertisementTitle { get; set; }
    public int TopAdvertisementViews { get; set; }
    public List<AdViewCountDto> ViewCountsByAdvertisement { get; set; } = new();
}

public class AdViewCountDto
{
    public long AdvertisementId { get; set; }
    public int TotalViews { get; set; }
    public int CompletedViews { get; set; }
    public int ViewsToday { get; set; }
    public int ViewsWeek { get; set; }
    public int ViewsMonth { get; set; }
}

