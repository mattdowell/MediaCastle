package org.dowell.mediacastle;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import org.dowell.mediacastle.adapters.UnsecureImageAdapter;
import org.dowell.mediacastle.task.ImportImageTask;
import org.dowell.mediacastle.util.ImageInfo;
import org.dowell.mediacastle.util.ImageUtil;
import org.dowell.mediacastle.util.ImportUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports media
 *
 * @author mjdowell
 */
public class ImportMediaActivity extends AbstractActivity implements OnItemClickListener, OnClickListener {

    public static final int PAGE_LIMIT = 50;
    public static final String SECURE_DIR = Environment.getExternalStorageDirectory() + File.separator + "MC";
    private static final int IMPORT_LITE_LIMIT = 8;
    private static final String TAG = "ImportMediaActivity";
    List<ImageInfo> galleryImages = null;
    ListView listview = null;
    UnsecureImageAdapter adapter = null;

    private int myGalleryCount = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleLiteVersionCheck();

        if (handleSharedImage(savedInstanceState)) {

            showShortToast("Image moved to MediaCastle");
            getIntent().removeExtra(EnterPwdActivity.MC_PWD_STATE);
            finish();

        } else if (hasValidSession()) {

            setContentView(R.layout.import_files_list);
            displayGalleryImages();
            initDir();
            Button b = (Button) findViewById(R.id.done_importing);
            b.setOnClickListener(this);

            if (adapter.getCount() < 1) {
                showShortToast("No gallery images. Take some pictures!");
            }

        } else {
            finish();
        }
    }

    public void onClick(View v) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        galleryImages = null;
    }

    /**
     * This method first checks to see if we are the lite version, and if so,
     * have we met our limit on image imports. If we have met our limit, just
     * print a warning msg to the screen and finish()
     */
    private void handleLiteVersionCheck() {
        if (isLiteVersion()) {
            Uri[] count = ImageUtil.getSecuredImageUris();
            if (count.length >= IMPORT_LITE_LIMIT) {
                showLongToast("You have reached the lite version limit of: " + IMPORT_LITE_LIMIT + " images.");
                finish();
            }
        }
    }

    /**
     * Method to handle SHARE menu choice in gallery
     *
     * @param inBundle
     * @return
     */
    private boolean handleSharedImage(Bundle inBundle) {
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                Object obj = bundle.get("android.intent.extra.STREAM");
                if (obj != null) {
                    try {

                        ImageInfo image = getImageInfoList((Uri) obj, true).get(0);

                        ImportUtil.handleImport(getContentResolver(), image);

                        // This tells the Android cache to refresh
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

                    } catch (Exception e) {
                        e.printStackTrace();
                        showShortToast("Could not import the image. Try refreshing your gallery.");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Show all the images in the gallery
     */
    private void displayGalleryImages() {

        // This tells the Android cache to refresh
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        listview = (ListView) findViewById(R.id.images_for_import);
        adapter = new UnsecureImageAdapter(getGalleryThumbnails(), this);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);

    }

    /**
     * Called when the user clicks on a thumbnail in the Gallery. It retrieves
     * the associated image and it should then move the image to the secure
     * directory.
     *
     * @param arg0     is the Adapter used by the Gallery, the calling object
     * @param arg1     is the thumbnail's View
     * @param position is the thumbnail's position in the Gallery
     * @param rowId    is the Adapter's RowId
     */
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
        int i = 0;
        try {
            i++;
            handleImageMove(galleryImages.get(position));
            // showShortToast("Image moved to secure area");
            i++;
            galleryImages.remove(position);
            i++;
            adapter.notifyDataSetInvalidated();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onItemClick: " + e.toString());
            showShortToast("Could not import image. Error no.: " + i);
        }
    }

    /**
     * This method does the dirty work
     *
     * @param inUri
     * @throws Exception
     */
    private void handleImageMove(ImageInfo inII) throws Exception {
        new ImportImageTask(this).execute(inII);
    }

    /**
     * FIRST TIME USE: Init the directory we are importing to.
     */
    private void initDir() {
        File securedDir = new File(SECURE_DIR);
        if (!securedDir.exists()) {
            securedDir.mkdir();
            File noMedia = new File(SECURE_DIR + File.separator + ".nomedia");
            try {
                noMedia.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                throw new IllegalStateException(e.toString());
            }
        }
    }

    /**
     * MINI_KIND: 512 x 384 thumbnail MICRO_KIND: 96 x 96 thumbnail
     * <p/>
     * Loads the gallery
     *
     * @return
     */
    private List<ImageInfo> getGalleryThumbnails() {

        galleryImages = new ArrayList<ImageInfo>();
        galleryImages.addAll(getImageInfoList(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false));
        return galleryImages;
    }

    /**
     * @param inContentUri Can either be EXTERNAL / INTERNAL or a specific image
     * @return
     */
    private List<ImageInfo> getImageInfoList(Uri inContentUri, boolean isUriSingleImage) {

        String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.MINI_THUMB_MAGIC, MediaStore.Images.Media.MIME_TYPE};

        Cursor mCursor = this.managedQuery(inContentUri, projection, null, null, null);

        // Total number of images in gallery
        if (mCursor != null) {
            myGalleryCount = mCursor.getCount();
        } else {
            myGalleryCount = 0;
        }

        if (myGalleryCount < 0) {
            myGalleryCount = 0;
        }

        List<ImageInfo> theReturn = new ArrayList<ImageInfo>(myGalleryCount);
        if (mCursor != null) {
            theReturn = new ArrayList<ImageInfo>(mCursor.getCount());
            for (int i = 0; i < myGalleryCount; i++) {
                if (isUriSingleImage) {
                    ImageInfo ii = buildImageInfoFromRow(mCursor, i, inContentUri, null);
                    theReturn.add(ii);
                } else {
                    ImageInfo ii = buildImageInfoFromRow(mCursor, i, null, inContentUri);
                    theReturn.add(ii);
                }
            }
        }

        return theReturn;
    }

    public static List<String> getCameraImages(Context context) {

        String CAMERA_IMAGE_BUCKET_NAME =
                Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
        String CAMERA_IMAGE_BUCKET_ID = String.valueOf(CAMERA_IMAGE_BUCKET_NAME.toLowerCase().hashCode());

        final String[] projection = {MediaStore.Images.Media.DATA};
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = {CAMERA_IMAGE_BUCKET_ID};
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }


    /**
     * Loads a thumbnail from the give full sized image URL
     *
     * @param url
     * @return
     */
    private Bitmap loadThumbnailImage(String url) {
        // Get original image ID
        int originalImageId = Integer.parseInt(getContentNumberFromUrl(url));

        // Get (or create upon demand) the micro thumbnail for the original
        // image.
        return MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), originalImageId, MediaStore.Images.Thumbnails.MICRO_KIND, null);
    }

    /**
     * @param inFullPath content:/sdfsd/sdfsdf/sdf234243/123
     * @return 123
     */
    private String getContentNumberFromUrl(String inFullPath) {
        return inFullPath.substring(inFullPath.lastIndexOf("/") + 1, inFullPath.length());
    }

    /**
     * Builds either an internal or external image info from the given DB row
     * num.
     *
     * @param c
     * @param rowNum
     * @param inImageUri   content://media/1243 <--actual image
     * @param inContentUri content:/images <--types
     * @return
     */
    private ImageInfo buildImageInfoFromRow(Cursor c, int rowNum, Uri inImageUri, Uri inContentUri) {

        try {
            // Move the cursor
            c.moveToPosition(rowNum);
            if (inImageUri == null) {
                // This gives us an INT. The actual image ID
                int fullSizedImageId = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
                inImageUri = Uri.withAppendedPath(inContentUri, String.valueOf(fullSizedImageId));
            }
            int dataIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int orientationColumnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION);
            int nameColumnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            String unsecuredPath = c.getString(dataIndex);
            int orientation = c.getInt(orientationColumnIndex);
            String name = c.getString(nameColumnIndex);
            Bitmap thumb = loadThumbnailImage(inImageUri.getEncodedPath());
            return buildImageInfo(inImageUri, null, unsecuredPath, name, orientation, thumb);

        } catch (Exception e) {
            Log.e("ImportMediaActivity", e.toString());
            return null;
        }
    }

    /**
     * Builds an ImageInfo class
     *
     * @param fullUri
     * @param thumbUri
     * @param inPath
     * @param inName
     * @param inOrient
     * @param inThumb
     * @return
     */
    private ImageInfo buildImageInfo(Uri fullUri, Uri thumbUri, String inPath, String inName, int inOrient, Bitmap inThumb) {
        ImageInfo ii = new ImageInfo();
        ii.setThumbImage(inThumb);
        ii.setOrientation(inOrient);
        ii.setUnsecuredFullSizedUri(fullUri);
        ii.setUnsecuredThumbUri(thumbUri);
        ii.setUnscFileNameAndPath(inPath);
        ii.setImageName(inName);
        return ii;
    }
}
