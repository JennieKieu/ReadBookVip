using ReadBookApi.Models.DTOs;

namespace ReadBookApi.Services;

public interface IAdvertisementService
{
    Task<List<AdvertisementDto>> GetAllAdvertisementsAsync();
    Task<AdvertisementDto?> GetAdvertisementByIdAsync(long id);
    Task<List<AdvertisementDto>> GetActiveAdvertisementsAsync();
    Task<AdvertisementDto> CreateAdvertisementAsync(CreateAdvertisementDto createDto);
    Task<AdvertisementDto?> UpdateAdvertisementAsync(long id, UpdateAdvertisementDto updateDto);
    Task<bool> DeleteAdvertisementAsync(long id);
    Task IncrementViewCountAsync(long id);
}

