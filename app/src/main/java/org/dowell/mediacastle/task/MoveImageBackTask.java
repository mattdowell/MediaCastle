package org.dowell.mediacastle.task;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import org.dowell.mediacastle.RetImgToGalleryActivity;
import org.dowell.mediacastle.util.ImageUtil;

import java.io.File;

public class MoveImageBackTask extends AsyncTask<Uri, Integer, Integer> {

    private RetImgToGalleryActivity activity = null;
    private ProgressDialog dialog = null;

    public MoveImageBackTask(RetImgToGalleryActivity inActivity) {
        super();
        activity = inActivity;
        dialog = ProgressDialog.show(activity, null, "Moving back...", true, false);
    }

    @Override
    protected Integer doInBackground(Uri... params) {

        Uri uri = params[0];

        try {
            moveImageToGallery(uri);
            deleteSecuredFile(uri.getPath());
            return Integer.valueOf(0);

        } catch (Exception e) {
            e.printStackTrace();
            return Integer.valueOf(1);
        }

    }

    protected void onPostExecute(Integer result) {
        Uri[] newSetOfImages = ImageUtil.getSecuredImageUris();
        activity.adapter.setSecureImageUris(newSetOfImages);
        activity.adapter.notifyDataSetChanged();
        activity.securedImages = newSetOfImages;
        dialog.dismiss();
    }

    private void moveImageToGallery(Uri inUri) throws Exception {
        MediaStore.Images.Media.insertImage(activity.getContentResolver(), ImageUtil.loadFullSizedEncryptedImage(inUri.getPath()), null, null);
    }

    void deleteSecuredFile(String inPath) {
        File f = new File(inPath);
        f.delete();
    }

}
