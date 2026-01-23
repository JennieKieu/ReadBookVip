package com.example.book.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeDeserializer implements JsonDeserializer<Long> {
    
    private static final String[] DATE_FORMATS = new String[]{
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",  // Support microseconds (7 digits)
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",   // Support microseconds (6 digits)
        "yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z'",   // Support microseconds (5 digits)
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",     // Support milliseconds (3 digits)
        "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",      // Support centiseconds (2 digits)
        "yyyy-MM-dd'T'HH:mm:ss.S'Z'",       // Support deciseconds (1 digit)
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    };

    @Override
    public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // Handle null or missing value
        if (json == null || json.isJsonNull()) {
            return 0L;
        }

        // If it's already a number, return it directly
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return json.getAsLong();
        }

        // Try to get as string
        String dateString;
        try {
            dateString = json.getAsString();
        } catch (Exception e) {
            android.util.Log.w("DateTimeDeserializer", "Cannot get string from json: " + json);
            return System.currentTimeMillis();
        }

        if (dateString == null || dateString.isEmpty()) {
            return 0L;
        }

        // Try to parse as long first (if backend sends timestamp directly)
        try {
            return Long.parseLong(dateString);
        } catch (NumberFormatException e) {
            // Not a number, try to parse as date string
        }

        // Handle microseconds format (7 digits after dot) - SimpleDateFormat doesn't support microseconds
        // Convert to milliseconds format by truncating to 3 digits
        if (dateString.contains(".") && dateString.contains("Z")) {
            try {
                // Extract the part before microseconds
                int dotIndex = dateString.indexOf('.');
                int zIndex = dateString.indexOf('Z');
                if (dotIndex > 0 && zIndex > dotIndex) {
                    String beforeDot = dateString.substring(0, dotIndex + 1);
                    String afterDot = dateString.substring(dotIndex + 1, zIndex);
                    // Truncate to 3 digits (milliseconds)
                    if (afterDot.length() > 3) {
                        afterDot = afterDot.substring(0, 3);
                    } else {
                        // Pad with zeros if less than 3 digits
                        while (afterDot.length() < 3) {
                            afterDot += "0";
                        }
                    }
                    dateString = beforeDot + afterDot + "Z";
                }
            } catch (Exception e) {
                // If conversion fails, try original string
            }
        }
        
        // Try different date formats
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(dateString);
                return date != null ? date.getTime() : 0L;
            } catch (ParseException e) {
                // Try next format
            }
        }

        // If all parsing fails, return 0 or current time
        android.util.Log.w("DateTimeDeserializer", "Failed to parse date: " + dateString);
        return System.currentTimeMillis();
    }
}

