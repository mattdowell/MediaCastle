package org.dowell.mediacastle.util;

import android.content.ContentResolver;

/**
 * This class is a delegate used to handle the Importing
 * of pictures from multiple sources. Usually the gallery
 * but can be from the "share" menu on gallery, or from
 * the asynchronous task via the ImportActivity
 *
 * @author Matt
 */
public class ImportUtil {


    /**
     * Handles all filesystem tasks with regards to MOVING an image to
     * the secured area.
     *
     * @param inImageInfo
     */
    public static void handleImport(ContentResolver inCr, ImageInfo inImageInfo) throws Exception {
        writeFileToSecureArea(inImageInfo);
        deleteUnsecuredFile(inCr, inImageInfo);
    }

    /**
     * Uses the ImageUtil to handle the writing of an Image to the secure area
     *
     * @param inImageInfo
     * @throws Exception
     */
    private static void writeFileToSecureArea(ImageInfo inImageInfo) throws Exception {
        ImageUtil.createObfuscatedThumb(inImageInfo);
        ImageUtil.createEncryptedImage(inImageInfo);
    }

    /**
     * @param inActivity  Activty os
     * @param inImageInfo
     * @throws Exception
     */
    private static void deleteUnsecuredFile(ContentResolver inCr, ImageInfo inImageInfo) throws Exception {
        ImageUtil.deleteUnsecuredFile(inCr, inImageInfo);

    }
}
