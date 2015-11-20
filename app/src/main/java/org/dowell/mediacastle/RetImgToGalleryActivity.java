package org.dowell.mediacastle;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import org.dowell.mediacastle.adapters.SecureImageAdapter;
import org.dowell.mediacastle.task.MoveImageBackTask;
import org.dowell.mediacastle.util.ImageUtil;

public class RetImgToGalleryActivity extends AbstractActivity implements OnItemClickListener, OnClickListener {
    private static final String TAG = "RetImgToGalleryActivity";
    public Uri[] securedImages = null;
    public SecureImageAdapter adapter = null;
    private ListView listView = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasValidSession()) {
            setContentView(R.layout.ret_image_layout);
            prepImages();
            Button b = (Button) findViewById(R.id.done_returning);
            b.setOnClickListener(this);
        } else {
            finish();
        }
    }

    public void onClick(View v) {
        setResult(RESULT_OK);
        finish();
    }

    void prepImages() {
        listView = (ListView) findViewById(R.id.images_for_return);
        securedImages = ImageUtil.getSecuredImageUris();
        adapter = new SecureImageAdapter(this, securedImages, "Back to Gallery");
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
        int i = 0;
        try {
            i++;
            Uri uri = securedImages[position];
            i++;
            new MoveImageBackTask(this).execute(uri);
            i++;
        } catch (Exception e) {
            Log.e(TAG, "return image: " + e.toString());
            showShortToast("Could not return image. Error no.: " + i);
        }
    }
}
