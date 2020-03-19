package com.xiaoe.shop.sdk.apicloud.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

public class APICloudUtils {
    private static final String RES_ROOT_DIR = "widget/";

    private APICloudUtils() {
    }

    public static void error(UZModuleContext moduleContext, String error) {
        try {
            JSONObject errorObj = new JSONObject();
            if (error != null) {
                errorObj.put("error", error);
            }
            moduleContext.error(null, errorObj, true);
        } catch (JSONException ignored) {
        }
    }

    public static void success(UZModuleContext moduleContext) {
        success(moduleContext, null);
    }

    public static void success(UZModuleContext moduleContext, Object result) {
        try {
            JSONObject resultObj = new JSONObject();
            if (result != null) {
                resultObj.put("result", result);
            }
            moduleContext.success(resultObj, true);
        } catch (JSONException e) {
            error(moduleContext, e.getMessage());
        }
    }

    public static void sendResult(UZModuleContext moduleContext, JSONObject result) {
        moduleContext.success(result, false);
    }

    public static Bitmap getImageFromAssets(Context context, String imagePath, int width, int height) {
        try {
            final String path = RES_ROOT_DIR + imagePath;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(path));
            return scaleBitmap(bitmap, width, height);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap scaleBitmap(Bitmap origin, int targetWidth, int targetHeight) {
        final int width = origin.getWidth();
        final int height = origin.getHeight();
        final float scaleWidth = targetWidth / width;
        final float scaleHeight = targetHeight / height;
        final Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(origin, 0, 0, width, height, matrix, true);
    }
}
