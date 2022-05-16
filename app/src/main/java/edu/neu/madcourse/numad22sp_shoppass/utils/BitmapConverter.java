package edu.neu.madcourse.numad22sp_shoppass.utils;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;

public class BitmapConverter {
    public static int NO_PIC_SHOW = R.drawable.example_product_img;

    public static Bitmap getImgBitmap(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;

    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        System.out.println("Drawable to Bitmap");
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Uri drawableToUri(int drawableId, Resources res) {
        Uri uri =  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + res.getResourcePackageName(drawableId) + "/"
                + res.getResourceTypeName(drawableId) + "/"
                + res.getResourceEntryName(drawableId));
        return uri;
    }
}
