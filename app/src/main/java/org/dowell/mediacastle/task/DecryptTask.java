package org.dowell.mediacastle.task;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;

import org.dowell.mediacastle.DisplayMediaActivity;
import org.dowell.mediacastle.util.DesEncrypter;
import org.dowell.mediacastle.util.ImageInfo;
import org.dowell.mediacastle.util.ImageUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

/**
 * We can decrypt our image's in the background which gives us the flexibility
 * to play with the view.
 *
 * @author mjdowell
 * @see http://developer.android.com/reference/android/os/AsyncTask.html
 */
public class DecryptTask extends AsyncTask<ImageInfo, Integer, Bitmap> {

    DisplayMediaActivity activity = null;
    ProgressDialog dialog = null;
    int width = 0;
    int height = 0;

    public DecryptTask(DisplayMediaActivity inActivity) {
        super();
        activity = inActivity;
        dialog = ProgressDialog.show(activity, null, "Decrypting...", true, false);
        width = activity.getWindowManager().getDefaultDisplay().getWidth();
        height = activity.getWindowManager().getDefaultDisplay().getHeight();
    }

    @Override
    protected Bitmap doInBackground(ImageInfo... inInfos) {

        Bitmap theReturn = null;

        try {
            ImageInfo ii = inInfos[0];

            ByteArrayOutputStream bout = DesEncrypter.getInstance().decrypt(
                    new FileInputStream(ii.getSecuredFullSizeUri().getEncodedPath()));
            byte[] imageArray = bout.toByteArray();

            // Cleanup
            bout.close();
            bout = null;

            int rotation = ImageUtil.getRotation(ImageUtil.getOrientation(imageArray));

            theReturn = ImageUtil.loadResized(imageArray, width, height);

            // Cleanup the array since it is no longer needed
            imageArray = null;

            if (rotation > 0 && ii.isHandleRotation()) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                theReturn = Bitmap.createBitmap(theReturn, 0, 0, theReturn.getWidth(), theReturn.getHeight(), matrix, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return theReturn;
    }

    protected void onPostExecute(Bitmap result) {
        dialog.dismiss();
        activity.displayUnencryptedFile(result);
    }

}
