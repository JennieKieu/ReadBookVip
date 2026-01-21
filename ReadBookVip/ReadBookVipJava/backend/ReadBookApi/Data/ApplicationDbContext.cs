using Microsoft.EntityFrameworkCore;
using ReadBookApi.Models;

namespace ReadBookApi.Data;

public class ApplicationDbContext : DbContext
{
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
        : base(options)
    {
    }

    public DbSet<Book> Books { get; set; }
    public DbSet<Chapter> Chapters { get; set; }
    public DbSet<BookHistory> BookHistories { get; set; }
    public DbSet<BookFavorite> BookFavorites { get; set; }
    public DbSet<Category> Categories { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Book>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Title).IsRequired().HasMaxLength(500);
            entity.Property(e => e.CategoryName).HasMaxLength(200);
        });

        modelBuilder.Entity<Chapter>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasOne(e => e.Book)
                .WithMany(b => b.Chapters)
                .HasForeignKey(e => e.BookId)
                .OnDelete(DeleteBehavior.Cascade);
            entity.Property(e => e.Content).IsRequired();
        });

        modelBuilder.Entity<BookHistory>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasOne<Book>()
                .WithMany()
                .HasForeignKey(e => e.BookId)
                .OnDelete(DeleteBehavior.Cascade);
            entity.Property(e => e.UserEmail).IsRequired().HasMaxLength(255);
        });

        modelBuilder.Entity<BookFavorite>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasOne<Book>()
                .WithMany()
                .HasForeignKey(e => e.BookId)
                .OnDelete(DeleteBehavior.Cascade);
            entity.Property(e => e.UserEmail).IsRequired().HasMaxLength(255);
            entity.HasIndex(e => new { e.BookId, e.UserEmail }).IsUnique();
        });

        modelBuilder.Entity<Category>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Name).IsRequired().HasMaxLength(200);
            entity.HasMany(e => e.Books)
                .WithOne()
                .HasForeignKey(b => b.CategoryId)
                .OnDelete(DeleteBehavior.SetNull);
        });
    }
}

