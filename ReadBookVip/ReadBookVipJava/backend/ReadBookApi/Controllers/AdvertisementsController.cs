using Microsoft.AspNetCore.Mvc;
using ReadBookApi.Models.DTOs;
using ReadBookApi.Services;

namespace ReadBookApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AdvertisementsController : ControllerBase
{
    private readonly IAdvertisementService _advertisementService;

    public AdvertisementsController(IAdvertisementService advertisementService)
    {
        _advertisementService = advertisementService;
    }

    [HttpGet]
    public async Task<ActionResult<List<AdvertisementDto>>> GetAllAdvertisements()
    {
        var advertisements = await _advertisementService.GetAllAdvertisementsAsync();
        return Ok(advertisements);
    }

    [HttpGet("active")]
    public async Task<ActionResult<List<AdvertisementDto>>> GetActiveAdvertisements()
    {
        var advertisements = await _advertisementService.GetActiveAdvertisementsAsync();
        return Ok(advertisements);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<AdvertisementDto>> GetAdvertisement(long id)
    {
        var advertisement = await _advertisementService.GetAdvertisementByIdAsync(id);
        if (advertisement == null) return NotFound();
        return Ok(advertisement);
    }

    [HttpPost]
    public async Task<ActionResult<AdvertisementDto>> CreateAdvertisement(CreateAdvertisementDto createDto)
    {
        var advertisement = await _advertisementService.CreateAdvertisementAsync(createDto);
        return CreatedAtAction(nameof(GetAdvertisement), new { id = advertisement.Id }, advertisement);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<AdvertisementDto>> UpdateAdvertisement(long id, UpdateAdvertisementDto updateDto)
    {
        var advertisement = await _advertisementService.UpdateAdvertisementAsync(id, updateDto);
        if (advertisement == null) return NotFound();
        return Ok(advertisement);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteAdvertisement(long id)
    {
        var result = await _advertisementService.DeleteAdvertisementAsync(id);
        if (!result) return NotFound();
        return NoContent();
    }

    [HttpPost("{id}/increment-view")]
    public async Task<IActionResult> IncrementViewCount(long id)
    {
        await _advertisementService.IncrementViewCountAsync(id);
        return Ok();
    }
}

