package com.example.quanlynhansu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/** Sao chép ảnh vào files/avatars, không phụ thuộc quyền URI sau khi khởi động lại. */
public final class PhotoHelper {
    private PhotoHelper() { }

    // Dùng API native từ minSdk 24 để giữ project Java/XML không có thư viện ngoài.
    public static String importPhoto(Context context, Uri uri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) throw new IOException("Không mở được ảnh");
            BitmapFactory.decodeStream(input, null, options);
        }
        if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException("Ảnh không hợp lệ");
        options.inSampleSize = 1;
        while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > 1024) {
            options.inSampleSize *= 2;
        }
        options.inJustDecodeBounds = false;
        Bitmap bitmap;
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(input, null, options);
        }
        if (bitmap == null) throw new IOException("Không giải mã được ảnh");

        // Sửa hướng ảnh chụp điện thoại nếu có EXIF; ảnh không có EXIF vẫn dùng được.
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input != null) orientation = new ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException ignored) { }
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setScale(1, -1); break;
            case ExifInterface.ORIENTATION_TRANSPOSE: matrix.setRotate(90); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
            case ExifInterface.ORIENTATION_TRANSVERSE: matrix.setRotate(-90); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(-90); break;
            default: break;
        }
        if (!matrix.isIdentity()) {
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) bitmap.recycle();
            bitmap = rotated;
        }

        File directory = new File(context.getFilesDir(), "avatars");
        if (!directory.exists() && !directory.mkdirs()) {
            bitmap.recycle();
            throw new IOException("Không tạo được thư mục ảnh");
        }
        File file = new File(directory, UUID.randomUUID() + ".jpg");
        try (FileOutputStream output = new FileOutputStream(file)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                throw new IOException("Không lưu được ảnh");
            }
        } catch (IOException error) {
            if (file.exists()) file.delete();
            throw error;
        } finally {
            bitmap.recycle();
        }
        return file.getAbsolutePath();
    }

    public static void display(ImageView view, String path) {
        view.setImageResource(R.drawable.ic_people);
        if (path == null || path.isEmpty()) return;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null) view.setImageBitmap(bitmap);
    }

    public static void delete(Context context, String path) {
        if (path == null || path.isEmpty()) return;
        File file = new File(path);
        File directory = new File(context.getFilesDir(), "avatars");
        // Chỉ xóa ảnh do chính ứng dụng tạo, không xóa ảnh thư viện của người dùng.
        if (directory.equals(file.getParentFile())) file.delete();
    }
}
