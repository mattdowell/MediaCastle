package org.dowell.mediacastle.util;

import android.graphics.Bitmap;
import android.net.Uri;

import org.dowell.mediacastle.ImportMediaActivity;

import java.io.File;

/**
 * @author mjdowell
 */
public class ImageInfo {

    public static final String THUMB_EXTENSION = ".thumb";

    private String unsecuredFileNameAndPath = null;
    private String imageName = null;

    private int maxHeight = 0;
    private int maxWidth = 0;

    private boolean handleRotation = true;
    private int orientation = 0;

    private Uri securedFullSizeUri = null;
    private Uri unsecuredFullSizedUri = null;
    private Uri unsecuredThumbUri = null;

    private Bitmap thumbImage = null;
    private Bitmap fullSizedImage = null;

    public ImageInfo() {

    }

    /**
     * What should we use?
     *
     * @param inFullImageUri
     * @return
     */
    @Deprecated
    public static String getSecuredThumbnailPath(Uri inFullImageUri) {
        return inFullImageUri.getEncodedPath() + THUMB_EXTENSION;
    }

    public String getThumbnailName() {
        return unsecuredFileNameAndPath + THUMB_EXTENSION;
    }

    public String getSecureThumbnailPathAndName() {
        return getSecuredImagePathAndName() + THUMB_EXTENSION;
    }

    public String getSecuredImagePathAndName() {
        return ImportMediaActivity.SECURE_DIR + File.separator + getFilenameOnly();
    }

    private String getFilenameOnly() {
        return unsecuredFileNameAndPath.substring(unsecuredFileNameAndPath.lastIndexOf("/") + 1, unsecuredFileNameAndPath.length());
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Uri getSecuredFullSizeUri() {
        return securedFullSizeUri;
    }

    public void setSecuredFullSizeUri(Uri securedFullSizeUri) {
        this.securedFullSizeUri = securedFullSizeUri;
    }

    public Uri getUnsecuredFullSizedUri() {
        return unsecuredFullSizedUri;
    }

    public void setUnsecuredFullSizedUri(Uri unsecuredFullSizedUri) {
        this.unsecuredFullSizedUri = unsecuredFullSizedUri;
    }

    public Uri getUnsecuredThumbUri() {
        return unsecuredThumbUri;
    }

    public void setUnsecuredThumbUri(Uri unsecuredThumbUri) {
        this.unsecuredThumbUri = unsecuredThumbUri;
    }

    public String getUnscFileNameAndPath() {
        return unsecuredFileNameAndPath;
    }

    public void setUnscFileNameAndPath(String fileName) {
        this.unsecuredFileNameAndPath = fileName;
    }


    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public boolean isHandleRotation() {
        return handleRotation;
    }

    public void setHandleRotation(boolean handleRotation) {
        this.handleRotation = handleRotation;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public Bitmap getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(Bitmap thumbImage) {
        this.thumbImage = thumbImage;
    }

    public Bitmap getFullSizedImage() {
        return fullSizedImage;
    }

    public void setFullSizedImage(Bitmap fullSizedImage) {
        this.fullSizedImage = fullSizedImage;
    }

}
