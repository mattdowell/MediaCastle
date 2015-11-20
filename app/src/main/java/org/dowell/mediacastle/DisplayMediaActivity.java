package org.dowell.mediacastle;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.dowell.mediacastle.adapters.SecureImageAdapter;
import org.dowell.mediacastle.task.DecryptTask;
import org.dowell.mediacastle.util.ImageInfo;
import org.dowell.mediacastle.util.ImageUtil;

import java.util.Timer;
import java.util.TimerTask;

public class DisplayMediaActivity extends AbstractActivity implements OnItemClickListener {

    private static final String CURRENT_IMAGE = "CURRENT_IMAGE";
    // private SimpleGestureFilter detector;
    private static final int ABOUT_DIALOG = 0;
    private static final int NEW_USER_DIALOG = 1;
    public ImageView closeButton;
    public ImageView nextButton;
    public ImageView previousButton;
    private Uri[] securedImages = null;
    private Uri currentImage = null;
    private int currentPosition = 0;
    private SecureImageAdapter adapter = null;
    private GridView gridview = null;
    private State myState = null;
    private boolean controlsAreShowing = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This is for any progress dialogs we might use
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // This is to handle any "Shared" images from the gallery
        if (!handleCurrentImage(savedInstanceState)) {

            if (hasValidSession()) {

                initViewAndImages();

            } else {

                startDisplayEnterPwd();
            }
        }
    }

    ;

    /**
     * This method takes the unencrypted bitmap and ads it to the layout
     *
     * @param inBitmap
     */
    public void displayUnencryptedFile(Bitmap inBitmap) {

        setContentView(R.layout.display_unencrypted_file_layout);
        setUpImageClickListeners();
        ImageView theImageHolder = (ImageView) findViewById(R.id.securedImageToDisplay);
        theImageHolder.setScaleType(ImageView.ScaleType.FIT_CENTER);
        theImageHolder.setImageBitmap(inBitmap);

        theImageHolder.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (controlsAreShowing) {
                    fadeControls();
                } else {
                    bringBackControls();
                }
                return false;
            }
        });
    }

    /**
     * Sets up the navigation icons and their touch actions
     */
    private void setUpImageClickListeners() {
        closeButton = (ImageView) findViewById(R.id.closeButton);
        nextButton = (ImageView) findViewById(R.id.nextImageButton);
        previousButton = (ImageView) findViewById(R.id.previousImageButton);

        closeButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                setContentView(gridview);
                return false;
            }
        });

        nextButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                gotoNextImage();
                return false;
            }
        });

        previousButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                gotoPreviousImage();
                return false;
            }
        });

        Timer myTimer = new Timer();

        // in 7 seconds, fade the controls, then wait a long time to do it again
        // (redundant)
        myTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        fadeControls();
                    }
                });
            }
        }, 20000);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (myState != null) {
            if (myState.equals(State.DISPLAYING_IMAGE)) {
                displayImage(currentImage, true);
            }
        }
    }

    /**
     * It's possible the screen orientation has changed, so if there is an image
     * that we are currently looking at, note it so we can re-display it in a
     * different mode (Portrait / Landscape)
     */
    @Override
    public void onSaveInstanceState(Bundle inBund) {
        super.onSaveInstanceState(inBund);
        inBund.putParcelable(CURRENT_IMAGE, currentImage);
    }

    /**
     * If we were viewing a current image and the orientation change, redisplay
     * it.
     *
     * @param inBund
     * @return
     */
    private boolean handleCurrentImage(Bundle inBund) {
        if (inBund != null) {
            currentImage = inBund.getParcelable(CURRENT_IMAGE);
            if (currentImage != null) {
                displayImage(currentImage, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPostResume() {
        super.onPostResume();

        if (!hasValidSession()) {
            startDisplayEnterPwd();
        }
    }

    /**
     *
     */
    private void initViewAndImages() {

        setContentView(R.layout.disp_secured_files);
        gridview = (GridView) findViewById(R.id.securemediadisplay);
        securedImages = ImageUtil.getSecuredImageUris();
        adapter = new SecureImageAdapter(getApplicationContext(), securedImages, "");
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(this);
        gridview.setPadding(1, 1, 1, 1);

        // If they don't have any images imported yet.
        if (securedImages == null || securedImages.length < 1) {
            showDialog(NEW_USER_DIALOG);
        }

        // detector = new SimpleGestureFilter(this, this);

        myState = State.GALLERY;
    }

    private void cleanup() {
        getIntent().removeExtra(EnterPwdActivity.MC_PWD_STATE);
        if (securedImages != null) {
            securedImages = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.secured_media_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.import_pics:
                startDisplayImportMediaActivity();
                return true;
            case R.id.delete_images:
                startDeleteImagesActivity();
                return true;
            case R.id.move_img_bk:
                startReturnImagesActivity();
                return true;
            case R.id.change_pwd:
                startDisplayCreatePwd();
                return true;
            case R.id.logout:
                logout();
                return true;
            case R.id.about:
                showDialog(ABOUT_DIALOG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @return
     */
    private Dialog buildAboutDialog() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.about_layout);
        dialog.setTitle("About MediaCastle");

        TextView text = (TextView) dialog.findViewById(R.id.aboutText);
        text.setText("MediaCastle Copyright 2011 by Matt Dowell\n\nMediaCastle encrpyts your images using DES encryption. To backup or port your images, just copy the MC directory on your phone to the new location. If you have any questions, comments or problems with this application, please email: mdowell@gmail.com");

        text.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                dialog.dismiss();
                return false;
            }
        });

        return dialog;
    }

    /**
     * @return
     */
    private Dialog buildNewUserDialog() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.about_layout);
        dialog.setTitle("Welcome");

        TextView text = (TextView) dialog.findViewById(R.id.aboutText);
        text.setText("Welcome MediaCastle user!\n\nTo see all your options, cleck the menu button.\n\nTo move pictures from your gallery, to the castle, choose Import.\n");

        text.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                dialog.dismiss();
                return false;
            }
        });

        return dialog;
    }

    /**
     * Shows the diaglog with the given custom ID
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case ABOUT_DIALOG:
                dialog = buildAboutDialog();
                break;
            case NEW_USER_DIALOG:
                dialog = buildNewUserDialog();
                break;
            default:
        }
        return dialog;
    }

    private void logout() {
        cleanup();
        clearSession();
        System.exit(0);
    }

    /**
     * Called when the user clicks on a thumbnail in the Gallery. It retrieves
     * the associated image and starts an ACTION_VIEW activity, which brings up
     * a slide show.
     *
     * @param adapterView is the Adapter used by the Gallery, the calling object
     * @param view        is the thumbnail's View
     * @param position    is the thumbnail's position in the Gallery
     * @param rowId       is the Adapter's RowId
     */
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
        currentPosition = position;
        displayImage(securedImages[position], true);
    }

    /**
     * Main method, takes a Uri and dispalys it properly
     *
     * @param inUri
     */
    private void displayImage(final Uri inUri, final boolean handleRotation) {

        if (inUri != null) {
            currentImage = inUri;

            ImageInfo ii = new ImageInfo();
            ii.setSecuredFullSizeUri(inUri);
            ii.setMaxHeight(getWindowManager().getDefaultDisplay().getHeight());
            ii.setMaxWidth(getWindowManager().getDefaultDisplay().getWidth());
            ii.setHandleRotation(handleRotation);

            // Decrypts the images and sets it on the view
            new DecryptTask(this).execute(ii);

            myState = State.DISPLAYING_IMAGE;
        }
    }

    /**
     * This method manages the lifecycle of all the child activities. It removes
     * them from the stack so a user can't see the images without logging in,
     * and the back button does not give weird results.
     */
    @Override
    protected void onActivityResult(int inRequestCode, int inResultCode, Intent data) {
        // super.showLongToast(getApplicationContext(), "Result: " +
        // inRequestCode + ":" + inResultCode + ":" + data);
        switch (inRequestCode) {
            case ENT_PWD_ACT:
                finishActivity(ENT_PWD_ACT);
                initViewAndImages();
            case NEW_PWD_ACT:
                finishActivity(NEW_PWD_ACT);
                initViewAndImages();
            case DEL_IMAGE_ACT:
                finishActivity(DEL_IMAGE_ACT);
                initViewAndImages();
            case IMP_MED_ACT:
                finishActivity(IMP_MED_ACT);
                initViewAndImages();
            case RET_IMAGE_ACT:
                finishActivity(RET_IMAGE_ACT);
                initViewAndImages();
        }
    }

    public void onDoubleTap() {
        setContentView(gridview);
    }

    private void gotoNextImage() {
        if (securedImages != null && securedImages.length > 0) {
            if (currentPosition == securedImages.length - 1) {
                currentPosition = 0;
            } else {
                currentPosition++;
            }
            displayImage(securedImages[currentPosition], true);
        }
    }

    private void gotoPreviousImage() {
        if (securedImages != null && securedImages.length > 0) {
            if (currentPosition == 0) {
                currentPosition = securedImages.length - 1;
            } else {
                currentPosition--;
            }
            displayImage(securedImages[currentPosition], true);
        }
    }

    public void fadeControls() {
        closeButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        previousButton.setVisibility(View.INVISIBLE);
        controlsAreShowing = false;
    }

    public void bringBackControls() {
        closeButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        previousButton.setVisibility(View.VISIBLE);
        controlsAreShowing = true;
    }

    public static enum State {
        DISPLAYING_IMAGE, GALLERY
    }
}
