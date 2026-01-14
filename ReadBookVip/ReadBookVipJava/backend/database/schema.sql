-- SQL Server Database Schema for Books and Chapters
-- Migration from Firebase to SQL Server

-- Create Books table
CREATE TABLE Books (
    Id BIGINT PRIMARY KEY IDENTITY(1,1),
    Title NVARCHAR(500) NOT NULL,
    Image NVARCHAR(MAX),
    Banner NVARCHAR(MAX),
    CategoryId BIGINT,
    CategoryName NVARCHAR(200),
    Featured BIT DEFAULT 0,
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE()
);

-- Create Chapters table
CREATE TABLE Chapters (
    Id BIGINT PRIMARY KEY IDENTITY(1,1),
    BookId BIGINT NOT NULL,
    ChapterNumber INT NOT NULL,
    Title NVARCHAR(500),
    Content NVARCHAR(MAX) NOT NULL, -- HTML content
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (BookId) REFERENCES Books(Id) ON DELETE CASCADE
);

-- Create BookHistory table (replaces Firebase history)
CREATE TABLE BookHistory (
    Id BIGINT PRIMARY KEY IDENTITY(1,1),
    BookId BIGINT NOT NULL,
    UserEmail NVARCHAR(255) NOT NULL,
    ChapterId BIGINT, -- Chapter đang đọc
    ChapterNumber INT DEFAULT 1,
    LastReadAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (BookId) REFERENCES Books(Id) ON DELETE CASCADE
);

-- Create BookFavorites table (replaces Firebase favorite)
CREATE TABLE BookFavorites (
    Id BIGINT PRIMARY KEY IDENTITY(1,1),
    BookId BIGINT NOT NULL,
    UserEmail NVARCHAR(255) NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (BookId) REFERENCES Books(Id) ON DELETE CASCADE,
    UNIQUE(BookId, UserEmail)
);

-- Create indexes for better performance
CREATE INDEX IX_Books_CategoryId ON Books(CategoryId);
CREATE INDEX IX_Books_Featured ON Books(Featured);
CREATE INDEX IX_Chapters_BookId ON Chapters(BookId);
CREATE INDEX IX_Chapters_BookId_ChapterNumber ON Chapters(BookId, ChapterNumber);
CREATE INDEX IX_BookHistory_BookId_UserEmail ON BookHistory(BookId, UserEmail);
CREATE INDEX IX_BookFavorites_BookId_UserEmail ON BookFavorites(BookId, UserEmail);

