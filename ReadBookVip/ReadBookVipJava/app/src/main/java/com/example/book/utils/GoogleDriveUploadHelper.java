package com.example.book.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;

public class GoogleDriveUploadHelper {
    
    private static final String TAG = "GoogleDriveUpload";
    private static final String APPLICATION_NAME = "ReadBook App";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    
    // Scopes required for Google Drive
    private static final java.util.Collection<String> SCOPES = Collections.singleton(DriveScopes.DRIVE_FILE);
    
    /**
     * Upload video to Google Drive and return shareable link
     * 
     * @param context Application context
     * @param videoUri URI of the video file to upload
     * @param accountName Google account email (null to use default account)
     * @param callback Callback to receive result
     */
    public static void uploadVideoToDrive(Context context, Uri videoUri, String accountName, 
                                         UploadCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // Initialize Google Account Credential
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                            context, SCOPES);
                    
                    if (accountName != null && !accountName.isEmpty()) {
                        credential.setSelectedAccountName(accountName);
                    }
                    
                    // Build Drive service
                    Drive driveService = new Drive.Builder(
                            HTTP_TRANSPORT,
                            JSON_FACTORY,
                            credential)
                            .setApplicationName(APPLICATION_NAME)
                            .build();
                    
                    // Read video file from URI
                    java.io.File tempFile = createTempFileFromUri(context, videoUri);
                    if (tempFile == null) {
                        return null;
                    }
                    
                    // Create file metadata
                    File fileMetadata = new File();
                    fileMetadata.setName(tempFile.getName());
                    fileMetadata.setMimeType("video/mp4");
                    
                    // Upload file
                    FileContent mediaContent = new FileContent("video/mp4", tempFile);
                    File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                            .setFields("id, webViewLink, webContentLink")
                            .execute();
                    
                    // Make file publicly accessible
                    Permission permission = new Permission();
                    permission.setType("anyone");
                    permission.setRole("reader");
                    driveService.permissions().create(uploadedFile.getId(), permission).execute();
                    
                    // Get shareable link
                    String shareableLink = uploadedFile.getWebViewLink();
                    if (shareableLink == null || shareableLink.isEmpty()) {
                        shareableLink = "https://drive.google.com/file/d/" + uploadedFile.getId() + "/view";
                    }
                    
                    // Clean up temp file
                    tempFile.delete();
                    
                    return shareableLink;
                    
                } catch (SecurityException e) {
                    Log.e(TAG, "SecurityException: Reading not allowed due to timeout or cancellation. " + 
                          "URI permission may have expired. Please select the video again.", e);
                    return "ERROR: Reading not allowed. Please select the video again.";
                } catch (java.io.IOException e) {
                    Log.e(TAG, "IOException: Error reading file or uploading to Drive", e);
                    return "ERROR: " + e.getMessage();
                } catch (Exception e) {
                    Log.e(TAG, "Error uploading to Google Drive", e);
                    return "ERROR: " + e.getMessage();
                }
            }
            
            @Override
            protected void onPostExecute(String result) {
                if (callback != null) {
                    if (result != null && !result.isEmpty() && !result.startsWith("ERROR:")) {
                        callback.onSuccess(result);
                    } else {
                        String errorMsg = result != null && result.startsWith("ERROR:") 
                            ? result.substring(6).trim() 
                            : "Failed to upload video to Google Drive";
                        callback.onError(errorMsg);
                    }
                }
            }
        }.execute();
    }
    
    /**
     * Create temporary file from URI
     */
    private static java.io.File createTempFileFromUri(Context context, Uri uri) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            // Try to open input stream with retry mechanism
            int retryCount = 0;
            while (retryCount < 3) {
                try {
                    inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        break;
                    }
                } catch (SecurityException e) {
                    Log.w(TAG, "SecurityException on attempt " + (retryCount + 1) + ": " + e.getMessage());
                    if (retryCount == 2) {
                        throw e;
                    }
                    try {
                        Thread.sleep(500); // Wait 500ms before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
                retryCount++;
            }
            
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream after retries");
                return null;
            }
            
            java.io.File tempFile = new java.io.File(context.getCacheDir(), "temp_video_" + System.currentTimeMillis() + ".mp4");
            outputStream = new FileOutputStream(tempFile);
            
            // Use larger buffer for better performance with large video files
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // Log progress for large files
                if (totalBytesRead % (10 * 1024 * 1024) == 0) { // Every 10MB
                    Log.d(TAG, "Reading file: " + (totalBytesRead / (1024 * 1024)) + " MB");
                }
            }
            
            Log.d(TAG, "File copied successfully: " + tempFile.getAbsolutePath() + " (" + (totalBytesRead / (1024 * 1024)) + " MB)");
            return tempFile;
            
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: Reading not allowed. URI permission may have expired.", e);
            return null;
        } catch (java.io.IOException e) {
            Log.e(TAG, "IOException: Error reading file - " + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error creating temp file: " + e.getMessage(), e);
            return null;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (java.io.IOException e) {
                Log.e(TAG, "Error closing streams", e);
            }
        }
    }
    
    public interface UploadCallback {
        void onSuccess(String shareableLink);
        void onError(String errorMessage);
    }
}

