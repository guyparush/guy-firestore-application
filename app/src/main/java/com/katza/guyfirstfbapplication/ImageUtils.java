
package com.katza.guyfirstfbapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {

    private static final int MAX_IMAGE_BYTES = 650000;
    private static final int MAX_DIMENSION = 1280;

    // Uri -> Bitmap
    public static Bitmap convertUriToBitmap(
            Context context, Uri uri) throws IOException {

        if (uri == null) {
            return null;
        }

        ImageDecoder.Source source =
                ImageDecoder.createSource(
                        context.getContentResolver(), uri);

        return ImageDecoder.decodeBitmap(
                source,
                new ImageDecoder.OnHeaderDecodedListener() {
                    @Override
                    public void onHeaderDecoded(
                            ImageDecoder decoder,
                            ImageDecoder.ImageInfo info,
                            ImageDecoder.Source source) {

                        int width = info.getSize().getWidth();
                        int height = info.getSize().getHeight();

                        float scale = Math.min(
                                1f,
                                (float) MAX_DIMENSION / Math.max(width, height));

                        int newWidth = Math.max(
                                1, Math.round(width * scale));

                        int newHeight = Math.max(
                                1, Math.round(height * scale));

                        decoder.setTargetSize(newWidth, newHeight);
                        decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                    }
                });
    }

    // Bitmap -> JPEG -> Base64
    public static String convertBitmapToBase64(Bitmap bitmap) {
        byte[] bytes = compressImage(bitmap);

        if (bytes == null) {
            return null;
        }

        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    // Base64 -> Bitmap
    public static Bitmap convertBase64ToBitmap(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.length);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    // ImageView -> Bitmap
    public static Bitmap convertImageViewToBitmap(ImageView imageView) {

        if (imageView == null) {
            return null;
        }

        Drawable drawable = imageView.getDrawable();

        if (drawable == null) {
            return null;
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        if (width <= 0 || height <= 0) {
            return null;
        }

        float scale = Math.min(
                1f,
                (float) MAX_DIMENSION / Math.max(width, height));

        width = Math.max(1, Math.round(width * scale));
        height = Math.max(1, Math.round(height * scale));

        Bitmap bitmap = Bitmap.createBitmap(
                width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Rect oldBounds = new Rect(drawable.getBounds());

        try {
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
        } finally {
            drawable.setBounds(oldBounds);
        }

        return bitmap;
    }

    // כיווץ התמונה לגודל המתאים לשמירה ב-Firestore
    public static byte[] compressImage(Bitmap bitmap) {

        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        Bitmap current = bitmap;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int longest = Math.max(width, height);

        if (longest > MAX_DIMENSION) {
            float scale = (float) MAX_DIMENSION / longest;

            current = Bitmap.createScaledBitmap(
                    bitmap,
                    Math.max(1, Math.round(width * scale)),
                    Math.max(1, Math.round(height * scale)),
                    true);
        }

        // המרת שקיפות לרקע לבן
        if (current.hasAlpha()) {
            Bitmap opaque = Bitmap.createBitmap(
                    current.getWidth(),
                    current.getHeight(),
                    Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(opaque);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(current, 0, 0, null);

            if (current != bitmap) {
                current.recycle();
            }

            current = opaque;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        while (true) {
            for (int quality = 90; quality >= 30; quality -= 10) {
                stream.reset();

                boolean success = current.compress(
                        Bitmap.CompressFormat.JPEG,
                        quality,
                        stream);

                if (!success) {
                    if (current != bitmap) {
                        current.recycle();
                    }
                    return null;
                }

                if (stream.size() <= MAX_IMAGE_BYTES) {
                    byte[] result = stream.toByteArray();

                    if (current != bitmap) {
                        current.recycle();
                    }

                    return result;
                }
            }

            // אם התמונה עדיין גדולה מדי, מקטינים אותה ב-20%
            int newWidth = Math.max(
                    1, (int) (current.getWidth() * 0.8));

            int newHeight = Math.max(
                    1, (int) (current.getHeight() * 0.8));

            if (newWidth < 100 || newHeight < 100
                    || (newWidth == current.getWidth()
                    && newHeight == current.getHeight())) {

                if (current != bitmap) {
                    current.recycle();
                }

                return null;
            }

            Bitmap smaller = Bitmap.createScaledBitmap(
                    current, newWidth, newHeight, true);

            if (current != bitmap) {
                current.recycle();
            }

            current = smaller;

        }


        while (true) {
            for (int quality = 90; quality >= 30; quality -= 10) {
                stream.reset();

                boolean success = current.compress(
                        Bitmap.CompressFormat.JPEG,
                        quality,
                        stream);

                if (!success) {
                    if (current != bitmap) {
                        current.recycle();
                    }
                    return null;
                }

                if (stream.size() <= MAX_IMAGE_BYTES) {
                    byte[] result = stream.toByteArray();

                    if (current != bitmap) {
                        current.recycle();
                    }

                    return result;
                }
            }

            int newWidth = Math.max(
                    1, (int) (current.getWidth() * 0.8));

            int newHeight = Math.max(
                    1, (int) (current.getHeight() * 0.8));

            if (newWidth < 100 || newHeight < 100
                    || (newWidth == current.getWidth()
                    && newHeight == current.getHeight())) {

                if (current != bitmap) {
                    current.recycle();
                }

                return null;
            }

            Bitmap smaller = Bitmap.createScaledBitmap(
                    current, newWidth, newHeight, true);

            if (current != bitmap) {
                current.recycle();
            }

            current = smaller;
        }

    }

}

