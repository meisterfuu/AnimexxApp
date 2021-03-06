package de.meisterfuu.animexx.utils.imageloader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import de.meisterfuu.animexx.utils.views.RoundedImageView;

public class ImageDownloaderCustom {

    private String folder;
    Map<String, Bitmap> imageCache;
    boolean clickable = false;

    public ImageDownloaderCustom(String folder) {
        this.folder = folder;
        imageCache = new HashMap<String, Bitmap>();
    }

    public boolean exists(final ImageSaveObject url, Context c) {
        // Caching code right here
        String filename = url.getName();
        final File f = new File(getCacheDirectory(c), filename);

        // Is the bitmap in our memory cache?
        Bitmap bitmap = null;

        bitmap = imageCache.get(f.getPath());

        if (bitmap == null) {

            bitmap = BitmapFactory.decodeFile(f.getPath());

            if (bitmap != null) {
                imageCache.put(f.getPath(), bitmap);
            }
        }

        if (bitmap == null) {
            // No
            return false;
        } else {
            // Yes
            return true;
        }
    }


    public void download(final ImageSaveObject url, final ImageView imageView) {
        download(url, imageView, false);
    }


    // download function
    public void download(final ImageSaveObject url, final ImageView imageView, boolean clickable) {
        if (cancelPotentialDownload(url, imageView)) {
            this.clickable = clickable;
            // Caching code right here
            String filename = url.getName();
            final File f = new File(getCacheDirectory(imageView.getContext()), filename);

            // Is the bitmap in our memory cache?
            Bitmap bitmap = null;

            bitmap = imageCache.get(f.getPath());

            if (bitmap == null) {

                BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
                bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                bitmap = BitmapFactory.decodeFile(f.getPath(), bitmapFatoryOptions);

                if (bitmap != null) {
                    imageCache.put(f.getPath(), bitmap);
                }

            }
            // No? download it

            if (bitmap == null) {
                BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
                DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
                if (imageView instanceof RoundedImageView) {
                    ((RoundedImageView) imageView).setImageDrawable(downloadedDrawable, true);
                } else {
                    imageView.setImageDrawable(downloadedDrawable);
                }

                task.execute(url);
            } else {
                // Yes? set the image
                if(url.isUsePalette()){
                    final Bitmap bm = bitmap;
                    Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {

                        private final float[] mMultiplyBlendMatrixValues = {
                                0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0,
                                0, 0, 0, 1, 0
                        };
                        private final float[] mAlphaMatrixValues = {
                                0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0,
                                0, 0, 0, 1, 0
                        };

                        private final ColorMatrix mWhitenessColorMatrix = new ColorMatrix();
                        private final ColorMatrix mMultiplyBlendMatrix = new ColorMatrix();

                        /**
                         * Simulates alpha blending an image with {@param color}.
                         */
                        private ColorMatrix alphaMatrix(float alpha, int color) {
                            mAlphaMatrixValues[0] = Color.red(color) * alpha / 255;
                            mAlphaMatrixValues[6] = Color.green(color) * alpha / 255;
                            mAlphaMatrixValues[12] = Color.blue(color) * alpha / 255;
                            mAlphaMatrixValues[4] = 255 * (1 - alpha);
                            mAlphaMatrixValues[9] = 255 * (1 - alpha);
                            mAlphaMatrixValues[14] = 255 * (1 - alpha);
                            mWhitenessColorMatrix.set(mAlphaMatrixValues);
                            return mWhitenessColorMatrix;
                        }

                        private float multiplyBlend(int color, float alpha) {
                            return color * alpha / 255.0f + (1 - alpha);
                        }

                        /**
                         * Simulates multiply blending an image with a single {@param color}.
                         *
                         * Multiply blending is [Sa * Da, Sc * Dc]. See {@link android.graphics.PorterDuff}.
                         */
                        private ColorMatrix multiplyBlendMatrix(int color, float alpha) {
                            mMultiplyBlendMatrixValues[0] = multiplyBlend(Color.red(color), alpha);
                            mMultiplyBlendMatrixValues[6] = multiplyBlend(Color.green(color), alpha);
                            mMultiplyBlendMatrixValues[12] = multiplyBlend(Color.blue(color), alpha);
                            mMultiplyBlendMatrix.set(mMultiplyBlendMatrixValues);
                            return mMultiplyBlendMatrix;
                        }

                        @Override
                        public void onGenerated(Palette palette) {
                            url.setPalette(palette);
                            final int mHeaderTintColor = palette.getVibrantSwatch().getRgb();
                            final float colorAlpha = 1 - 0.2f;
                            final float colorAlpha2 = 1 - 0.6f;


                            ColorMatrix mColorMatrix = new ColorMatrix();
                            mColorMatrix.setSaturation(1);
                            mColorMatrix.postConcat(alphaMatrix(colorAlpha2, Color.WHITE));
                            mColorMatrix.postConcat(multiplyBlendMatrix(mHeaderTintColor, colorAlpha));

                            imageView.setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
                            //imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);


                            imageView.setImageBitmap(bm);
                        }
                    });
                } else {
                    imageView.setImageBitmap(bitmap);
                }
                if (clickable) imageView.setOnClickListener(new OnClickListener() {

                    public void onClick(View arg0) {
                        Intent i2 = new Intent();
                        i2.setAction(android.content.Intent.ACTION_VIEW);
                        i2.setDataAndType(Uri.fromFile(f), "image/*");
                        imageView.getContext().startActivity(i2);
                    }

                });

            }
        }
    }





    // cancel a download (internal only)
    private static boolean cancelPotentialDownload(ImageSaveObject url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
        try {
            if (bitmapDownloaderTask != null) {
                String bitmapUrl = bitmapDownloaderTask.url.getUrl();
                if ((bitmapUrl == null) || (!bitmapUrl.equals(url.getUrl()))) {
                    bitmapDownloaderTask.cancel(true);
                } else {
                    // The same URL is already being downloaded.
                    return false;
                }
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }


    // gets an existing download if one exists for the imageview
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }


    // our caching functions
    // Find the dir to save cached images
    private File getCacheDirectory(Context context) {
        File sdDir = context.getExternalCacheDir();
        File cacheDir;
        if (sdDir != null) {
            cacheDir = new File(sdDir, folder);
        } else
            cacheDir = context.getCacheDir();

        if (!cacheDir.exists()) cacheDir.mkdirs();
        return cacheDir;
    }


    private void writeFile(final Bitmap bmp, final File f) {
        new Thread(new Runnable() {

            public void run() {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(f);
                    bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) out.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }).start();

    }

    // /////////////////////

    // download asynctask
    public class BitmapDownloaderTask extends AsyncTask<ImageSaveObject, Void, Bitmap> {

        private ImageSaveObject url;
        private final WeakReference<ImageView> imageViewReference;


        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }


        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(ImageSaveObject... params) {
            // params comes from the execute() call: params[0] is the url.
            url = params[0];
            Bitmap b = downloadBitmap(params[0].getUrl());
            return b;
        }


        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                final ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                //Change bitmap only if this process is still associated with it
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(bitmap);
                    // cache the image

                    String filename = url.getName();
                    final File f = new File(getCacheDirectory(imageView.getContext()), filename);
                    writeFile(bitmap, f);
                    imageCache.put(f.getPath(), bitmap);


                    if (clickable) imageView.setOnClickListener(new OnClickListener() {

                        public void onClick(View arg0) {
                            Intent i2 = new Intent();
                            i2.setAction(android.content.Intent.ACTION_VIEW);
                            i2.setDataAndType(Uri.fromFile(f), "image/*");
                            imageView.getContext().startActivity(i2);
                        }

                    });
                }
            }
        }

    }

    static class DownloadedDrawable extends ColorDrawable {

        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;


        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            super(Color.TRANSPARENT);
            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }


        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }


    }


    // the actual download code
    static Bitmap downloadBitmap(String url) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpClient client = new DefaultHttpClient(params);
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    final Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            Log.w("ImageDownloader", "Error while retrieving bitmap from " + url + e.toString());
        } finally {
            if (client != null) {
                // client.close();
            }
        }
        return null;
    }


    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int bytes = read();
                    if (bytes < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }


}
