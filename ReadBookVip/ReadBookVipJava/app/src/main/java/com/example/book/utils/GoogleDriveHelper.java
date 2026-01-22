package com.example.book.utils;

public class GoogleDriveHelper {
    
    /**
     * Convert Google Drive shareable link to direct download/playable link
     * 
     * Input formats:
     * - https://drive.google.com/file/d/FILE_ID/view?usp=sharing
     * - https://drive.google.com/open?id=FILE_ID
     * - https://drive.google.com/uc?id=FILE_ID
     * 
     * Output format:
     * - https://drive.google.com/uc?export=download&id=FILE_ID
     */
    public static String convertToPlayableUrl(String googleDriveUrl) {
        if (googleDriveUrl == null || googleDriveUrl.trim().isEmpty()) {
            return googleDriveUrl;
        }
        
        String url = googleDriveUrl.trim();
        
        // If already in correct format, return as is
        if (url.contains("uc?export=download") || url.contains("uc?id=")) {
            return url;
        }
        
        // Extract file ID from various Google Drive URL formats
        String fileId = extractFileId(url);
        
        if (fileId != null && !fileId.isEmpty()) {
            // Convert to direct download link for video playback
            return "https://drive.google.com/uc?export=download&id=" + fileId;
        }
        
        // If extraction failed, return original URL
        return url;
    }
    
    private static String extractFileId(String url) {
        // Pattern 1: /file/d/FILE_ID/
        int startIndex = url.indexOf("/file/d/");
        if (startIndex != -1) {
            startIndex += 8; // Length of "/file/d/"
            int endIndex = url.indexOf("/", startIndex);
            if (endIndex == -1) {
                endIndex = url.indexOf("?", startIndex);
            }
            if (endIndex == -1) {
                endIndex = url.length();
            }
            return url.substring(startIndex, endIndex);
        }
        
        // Pattern 2: ?id=FILE_ID
        startIndex = url.indexOf("?id=");
        if (startIndex != -1) {
            startIndex += 4; // Length of "?id="
            int endIndex = url.indexOf("&", startIndex);
            if (endIndex == -1) {
                endIndex = url.length();
            }
            return url.substring(startIndex, endIndex);
        }
        
        // Pattern 3: &id=FILE_ID
        startIndex = url.indexOf("&id=");
        if (startIndex != -1) {
            startIndex += 4; // Length of "&id="
            int endIndex = url.indexOf("&", startIndex);
            if (endIndex == -1) {
                endIndex = url.length();
            }
            return url.substring(startIndex, endIndex);
        }
        
        return null;
    }
}

