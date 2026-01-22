using Microsoft.EntityFrameworkCore;
using ReadBookApi.Data;
using ReadBookApi.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Add CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAndroidApp", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// Add DbContext
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection") 
    ?? "Server=localhost;Database=ReadBookDB;Trusted_Connection=True;TrustServerCertificate=True;";

builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseSqlServer(connectionString));

// Add Services
builder.Services.AddScoped<IBookService, BookService>();
builder.Services.AddScoped<ICategoryService, CategoryService>();
builder.Services.AddScoped<IAdvertisementService, AdvertisementService>();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAndroidApp");

app.UseAuthorization();

app.MapControllers();

// Configure URLs - listen on all interfaces for Android emulator access
app.Urls.Add("http://0.0.0.0:5000");
app.Urls.Add("https://0.0.0.0:5001");

Console.WriteLine("Backend API is running...");
Console.WriteLine("HTTP: http://localhost:5000");
Console.WriteLine("HTTPS: https://localhost:5001");
Console.WriteLine("Swagger: http://localhost:5000/swagger");

app.Run();

