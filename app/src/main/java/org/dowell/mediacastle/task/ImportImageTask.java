package org.dowell.mediacastle.task;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import org.dowell.mediacastle.ImportMediaActivity;
import org.dowell.mediacastle.util.ImageInfo;
import org.dowell.mediacastle.util.ImportUtil;


public class ImportImageTask extends AsyncTask<ImageInfo, Integer, Integer> {

    ImportMediaActivity activity = null;
    ProgressDialog dialog = null;


    public ImportImageTask(ImportMediaActivity activity) {
        super();
        this.activity = activity;
        dialog = ProgressDialog.show(activity, null, "Securing in MediaCastle...", true, false);
    }

    @Override
    protected Integer doInBackground(ImageInfo... params) {
        try {
            if (params != null) {
                ImageInfo inII = params[0];

                ImportUtil.handleImport(activity.getContentResolver(), inII);

                // This tells the Android cache to refresh
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    protected void onPostExecute(Integer result) {
        dialog.dismiss();
        if (result.intValue() == 0) {
            activity.showShortToast("Import Successful");
        } else if (result.intValue() == 1) {
            activity.showShortToast("Image could not be imported");
        }
    }

}
