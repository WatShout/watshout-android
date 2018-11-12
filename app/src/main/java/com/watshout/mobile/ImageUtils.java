package com.watshout.mobile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

    // Standard profile picture size: 256x256

    public static Bitmap reshapeAsProfilePicture(Bitmap srcBmp,Activity activity){

        final int STANDARD_PFP_DIM = 256;
        int width = srcBmp.getWidth();
        int height = srcBmp.getHeight();

        double scaleFactor = (double)width/STANDARD_PFP_DIM;
        Log.i("ReshapePfp","Scale factor: "+scaleFactor);

        Bitmap out = null;

        // Crop out non-central parts of image
        if (srcBmp.getWidth() > srcBmp.getHeight()){

            out = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else if (srcBmp.getWidth() < srcBmp.getHeight()){

            out = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        } else Log.i("ReshapePfp","Image already square.");

        // Resize cropped image to 256x256
        out = Bitmap.createScaledBitmap(out, 256, 256, true);

        // Compress image with PNG format
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        out.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        out = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

        return out;

    }

}
