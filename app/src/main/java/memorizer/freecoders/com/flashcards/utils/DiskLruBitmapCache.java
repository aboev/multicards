package memorizer.freecoders.com.flashcards.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import memorizer.freecoders.com.flashcards.BuildConfig;

/**
 * Created by Alex on 2014-11-29.
 */

public class DiskLruBitmapCache implements ImageLoader.ImageCache {
    private static String LOG_TAG = "DiskLruBitmapCache";

    private com.jakewharton.disklrucache.DiskLruCache mDiskCache;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private int mCompressQuality = 70;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private static final String TAG = "DiskLruImageCache";
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    public DiskLruBitmapCache(Context context, String uniqueName, int diskCacheSize,
                              Bitmap.CompressFormat compressFormat, int quality) throws IOException {
        final File diskCacheDir = getDiskCacheDir(context, uniqueName );
        mDiskCache = com.jakewharton.disklrucache.DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
        mCompressFormat = compressFormat;
        mCompressQuality = quality;
    }

    private boolean writeBitmapToFile( Bitmap bitmap, com.jakewharton.disklrucache.DiskLruCache.Editor editor )
            throws IOException, FileNotFoundException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
            return bitmap.compress( mCompressFormat, mCompressQuality, out );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        String cachePath = context.getCacheDir().getPath();; // you still need a default value if not mounted

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable()) {
            if (getExternalCacheDir(context) != null) {
                cachePath = getExternalCacheDir(context).getPath();
            }
        }

        return new File(cachePath + File.separator + uniqueName);
    }

    @Override
    public void putBitmap( String url, Bitmap data ) {
        String key = makeSHA1Hash(url);
        com.jakewharton.disklrucache.DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( key );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {
                mDiskCache.flush();
                editor.commit();
                if ( BuildConfig.DEBUG ) {
                    Log.d( LOG_TAG, "image put on disk cache " + url +
                            " at " + key);
                }
            } else {
                editor.abort();
                if ( BuildConfig.DEBUG ) {
                    Log.d( LOG_TAG, "ERROR on: image put on disk cache " + url +
                            " at " + key);
                }
            }
        } catch (IOException e) {
            if ( BuildConfig.DEBUG ) {
                Log.d( LOG_TAG, "ERROR on: image put on disk cache " + url +
                        " at " + key);
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    @Override
    public Bitmap getBitmap( String url ) {
        String key = makeSHA1Hash(url);
        Bitmap bitmap = null;
        com.jakewharton.disklrucache.DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn =
                        new BufferedInputStream( in, IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        if ( BuildConfig.DEBUG ) {
            Log.d( LOG_TAG, bitmap == null ? "" : "image read from disk for "
                    + url + " from " + key);
        }

        return bitmap;

    }

    public boolean containsKey( String key ) {

        boolean contained = false;
        com.jakewharton.disklrucache.DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if ( BuildConfig.DEBUG ) {
            Log.d( LOG_TAG, "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }

    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public final static String makeSHA1Hash(String input)
    {
        try {
            MessageDigest md = null;
            md = MessageDigest.getInstance("SHA1");
            md.reset();
            byte[] buffer = input.getBytes();
            md.update(buffer);
            byte[] digest = md.digest();

            String hexStr = "";
            for (int i = 0; i < digest.length; i++) {
                hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
            return hexStr;
        } catch (NoSuchAlgorithmException e) {
            Log.d(LOG_TAG, "Hash exception: " + e.getLocalizedMessage());
            return "";
        }
    }

}