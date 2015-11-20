package org.dowell.mediacastle.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import org.dowell.mediacastle.ImportMediaActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

public class ImageUtil {

    public static final int THUMB_WIDTH = 95;
    public static final int THUMB_HEIGHT = 95;
    private static final int COPY_BYTES_BUFFER_BYTES = 1024 * 4;
    private static final byte[] TEMP_STORAGE = new byte[COPY_BYTES_BUFFER_BYTES];

    public static Bitmap loadFullBitmap(String filename) {
        return BitmapFactory.decodeFile(filename);
    }

    public static Bitmap loadFullBitmap(InputStream inFile) {
        return BitmapFactory.decodeStream(inFile);
    }

    /**
     * @param image
     * @return
     * @throws Exception
     */
    public static Metadata readMetadata(byte[] image) throws Exception {
        return JpegMetadataReader.readMetadata(new ByteArrayInputStream(image));
    }

    /**
     * Gets the exif orientation using a 3rd party lib.
     *
     * @param inImage
     * @return
     * @throws Exception
     */
    public static int getOrientation(byte[] inImage) throws Exception {
        try {
            Metadata meta = readMetadata(inImage);
            Directory directory = meta.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            return directory.getInt(ExifSubIFDDirectory.TAG_ORIENTATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // default no rotation
    }

    public static Bitmap loadEncryptedThumbnail(Uri inUri) {
        try {
            InputStream fis = new FileInputStream(inUri.getPath());
            ByteArrayOutputStream out = DesEncrypter.getInstance().decrypt(fis);
            Bitmap bm = ImageUtil.loadResized(out.toByteArray(), 85, 85);
            out.close();
            fis.close();
            return bm;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The given URI should be of the FULL SIZED IMAGE
     *
     * @param inUri
     * @return
     */
    public static Bitmap loadObfuscatedThumbnail(Uri inUri) {
        try {
            byte[] fis = getImage(ImageInfo.getSecuredThumbnailPath(inUri));
            byte[] out = ObfscateUtil.deObfuscate(fis);
            return ImageUtil.load(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads a thumbnail of the given full-sized image Uri
     *
     * @param inContResolv
     * @param inUri
     * @return
     */
    public static Bitmap loadThumbnailFromLargeBitmap(ContentResolver inContResolv, Uri inUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outHeight = THUMB_HEIGHT;
            options.outWidth = THUMB_WIDTH;
            options.inSampleSize = 50;
            options.inPurgeable = true;
            options.inTargetDensity = 0;
            options.inTempStorage = TEMP_STORAGE;
            options.inDensity = DisplayMetrics.DENSITY_LOW;
            return BitmapFactory.decodeStream(inContResolv.openInputStream(inUri), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads a thumbnail with a gived small image
     *
     * @param inContResolv
     * @param inUri
     * @return
     */
    public static Bitmap loadThumbnailWithoutMods(ContentResolver inContResolv, Uri inUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outHeight = THUMB_HEIGHT;
            options.outWidth = THUMB_WIDTH;
            options.inDensity = DisplayMetrics.DENSITY_LOW;
            options.inPurgeable = true;
            options.inTargetDensity = 0;
            options.inSampleSize = 4;
            return BitmapFactory.decodeStream(inContResolv.openInputStream(inUri), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Turn a byte[] in to a Bitmap
     *
     * @param image
     * @return
     */
    public static Bitmap load(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    /**
     * Loads the byte[] as an Image using the given width and height
     *
     * @param image
     * @param width
     * @param height
     * @return
     */
    public static Bitmap loadResized(byte[] image, int width, int height) {

        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDensity = DisplayMetrics.DENSITY_LOW;
        BitmapFactory.decodeByteArray(image, 0, image.length, options);
        if (options.outHeight > 0 && options.outWidth > 0) {

            // Real options
            options.inJustDecodeBounds = false;
            options.inSampleSize = 2;
            options.inPurgeable = true;

            // Now see how much we need to scale it down.
            int widthFactor = (options.outWidth + width - 1) / width;
            int heightFactor = (options.outHeight + height - 1) / height;

            widthFactor = Math.max(widthFactor, heightFactor);
            widthFactor = Math.max(widthFactor, 1);

            // Now turn it into a power of two.
            if (widthFactor > 1) {
                if ((widthFactor & (widthFactor - 1)) != 0) {
                    while ((widthFactor & (widthFactor - 1)) != 0) {
                        widthFactor &= widthFactor - 1;
                    }

                    widthFactor <<= 1;
                }
            }

            options.inSampleSize = widthFactor;

            if (options.inSampleSize > 80) {
                options.inDensity = DisplayMetrics.DENSITY_HIGH;
            }
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        }
        return bitmap;
    }

    public static InputStream getStreamFromPath(String inFilename) throws FileNotFoundException {
        return new FileInputStream(new File(inFilename));
    }

    /**
     * Gets all the files in the secure directory
     *
     * @return
     */
    public static Uri[] getSecuredImageUris() {
        File images = new File(ImportMediaActivity.SECURE_DIR);

        // Get all the files in the directory
        File[] imagelist = images.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg"));
            }
        });

        Uri[] mUrls;
        if (imagelist == null) {
            return new Uri[0];
        } else {
            // Sort the files by last modified date
            Arrays.sort(imagelist, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                }
            });
        }

        // Convert the files to URI's
        mUrls = new Uri[(imagelist.length)];
        for (int i = 0; i < imagelist.length; i++) {
            mUrls[i] = Uri.parse(imagelist[i].getAbsolutePath());
        }
        return mUrls;
    }

    /**
     * Scans the data directory and returs all images
     *
     * @return
     */
    public static Uri[] getUnSecuredImageUris() {
        File images = Environment.getExternalStorageDirectory();
        File[] imagelist = images.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg"));
            }
        });
        Uri[] mUrls;

        if (imagelist == null) {
            return new Uri[0];
        }

        mUrls = new Uri[(imagelist.length)];
        for (int i = 0; i < imagelist.length; i++) {
            mUrls[i] = Uri.parse(imagelist[i].getAbsolutePath());
        }
        return mUrls;
    }

    public static byte[] getImage(String inFileName) throws Exception {

        File file = new File(inFileName);
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buf = new byte[COPY_BYTES_BUFFER_BYTES];

        for (int readNum; (readNum = fis.read(buf)) != -1; ) {
            bos.write(buf, 0, readNum);
        }

        return bos.toByteArray();
    }

    public static byte[] inputStreamToByteArray(InputStream inStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[COPY_BYTES_BUFFER_BYTES];
        while ((nRead = inStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    /**
     * Save the given byte[] image to the given path
     *
     * @param in
     * @param newFilename
     * @throws Exception
     */
    public static void saveImage(byte[] in, String newFilename) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < in.length; i++) {
            out.write(in[i]);
        }

        out.writeTo(new FileOutputStream(new File(newFilename)));

    }

    /**
     * Create a obfuscated thumbnail byte[] from the given path to JPG file
     *
     * @param unsecuredPath
     * @return
     * @throws Exception
     */
    public static byte[] createObfsc8Thumbnail(String unsecuredPath) throws Exception {

        // Turn to byte[]
        byte[] largeByte = inputStreamToByteArray(new FileInputStream(new File(unsecuredPath)));

        // Shrink
        Bitmap smallImage = loadResized(largeByte, THUMB_WIDTH, THUMB_HEIGHT);

        // Back to byte[]
        ByteArrayOutputStream smallByte = new ByteArrayOutputStream();
        smallImage.compress(CompressFormat.JPEG, 80, smallByte);

        // ..then Obfuscate.
        return ObfscateUtil.obfuscate(smallByte.toByteArray());
    }

    /**
     * Uses the decryption util to decrpt and load the full sized bitmap
     *
     * @param inFilename
     * @return
     */
    public static Bitmap loadFullSizedEncryptedImage(String inFilename) throws Exception {
        ByteArrayOutputStream bout = DesEncrypter.getInstance().decrypt(new FileInputStream(inFilename));

        byte[] image = bout.toByteArray();

        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    /**
     * Given an EXIF rotation number, return the int rotation in degrees
     * http://sylvana.net/jpegcrop/exif_orientation.html
     *
     * @param inOrientation
     * @return
     */
    public static int getRotation(int inOrientation) {

        switch (inOrientation) {
            case 1:
                return 0;
            case 2:
                return 0;
            case 3:
                return 180;
            case 4:
                return 0;
            case 5:
                return 0;
            case 6:
                return 90;
            case 7:
                return 0;
            case 8:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * Creates an obfuscated thumbnail using the ImageUtil
     *
     * @param inImageInfo
     * @throws Exception
     */
    public static void createObfuscatedThumb(ImageInfo inImageInfo) throws Exception {
        byte[] thumb = ImageUtil.createObfsc8Thumbnail(inImageInfo.getUnscFileNameAndPath());
        ImageUtil.saveImage(thumb, inImageInfo.getSecureThumbnailPathAndName());
    }

    /**
     * @param inImageInfo
     * @throws Exception
     */
    public static void createEncryptedImage(ImageInfo inImageInfo) throws Exception {
        FileInputStream source = new FileInputStream(new File(inImageInfo.getUnscFileNameAndPath()));
        FileOutputStream dest = new FileOutputStream(new File(inImageInfo.getSecuredImagePathAndName()));
        DesEncrypter.getInstance().encrypt(source, dest);

        dest.close();
        source.close();
    }

    /**
     * Using the ContentResolver, remove the image we just imported. If the
     * image cannot be deleting from the CR, then try to manually delete it
     * from the filesystem.
     *
     * @param inFile
     * @throws Exception
     */
    public static void deleteUnsecuredFile(ContentResolver inCr, ImageInfo inFile) throws Exception {

        try {

            // Try to delete using the ContentResolver
            inCr.delete(inFile.getUnsecuredFullSizedUri(), null, null);

        } catch (Exception e) {
            e.printStackTrace();

            // delete it manually
            File f = new File(inFile.getUnscFileNameAndPath());
            f.delete();
        }

    }

}
