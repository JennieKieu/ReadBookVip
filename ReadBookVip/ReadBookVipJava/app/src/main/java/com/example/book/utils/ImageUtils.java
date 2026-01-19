package com.example.book.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageUtils {
    
    /**
     * Load image from either base64 or URL
     * @param imageView Target ImageView
     * @param imageString Base64 string (starting with "data:image") or URL
     */
    public static void loadImage(ImageView imageView, String imageString) {
        if (imageString == null || imageString.isEmpty()) {
            return;
        }
        
        try {
            if (imageString.startsWith("data:image")) {
                // Base64 image
                String base64Image = imageString.split(",")[1];
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(bitmap);
            } else {
                // URL image (Firebase Storage or other URL)
                Glide.with(imageView.getContext())
                        .load(imageString)
                        .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Check if string is base64 image
     */
    public static boolean isBase64Image(String imageString) {
        return imageString != null && imageString.startsWith("data:image");
    }
}

