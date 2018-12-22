package org.reactnative.camera.tasks;

import org.tensorflow.lite.Interpreter;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.TextureView;

import java.util.concurrent.TimeUnit;
import android.util.Log;

public class ModelProcessorAsyncTask extends android.os.AsyncTask<Void, Void, ByteBuffer> {

  private static final int DIM_IMG_SIZE_X = 224;
  private static final int DIM_IMG_SIZE_Y = 224;
  private static final int DIM_PIXEL_SIZE = 3;
  private ModelProcessorAsyncTaskDelegate mDelegate;
  private Interpreter mModelProcessor;
  private TextureView mRenderView;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  static private int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
  static private ByteBuffer mImageData = ByteBuffer.allocateDirect(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);

  public ModelProcessorAsyncTask(
      ModelProcessorAsyncTaskDelegate delegate,
      Interpreter modelProcessor,
      TextureView renderView,
      int width,
      int height,
      int rotation
  ) {
    mDelegate = delegate;
    mModelProcessor = modelProcessor;
    mRenderView = renderView;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
  }

  private void convertBitmapToByteBuffer(Bitmap bitmap) {
    if (mImageData == null) {
      return;
    }
    mImageData.rewind();
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    // Convert the image to floating point.
    int pixel = 0;
    for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
      for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
        final int val = intValues[pixel++];
        mImageData.put((byte) ((val >> 16) & 0xFF));
        mImageData.put((byte) ((val >> 8) & 0xFF));
        mImageData.put((byte) (val & 0xFF));
      }
    }
  }
    
  @Override
  protected ByteBuffer doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mModelProcessor == null) {
      return null;
    }
    try {
        TimeUnit.SECONDS.sleep(3);
    } catch(Exception e) {}
    ByteBuffer output = ByteBuffer.allocate(1024);
    Bitmap renderBitmap = mRenderView.getBitmap(DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y);
    convertBitmapToByteBuffer(renderBitmap);
    try {
        mModelProcessor.run(mImageData, output);
    } catch (Exception e) {
        Log.e("tfmodel", "exception", e);
    }
    return output;
  }

  @Override
  protected void onPostExecute(ByteBuffer data) {
    super.onPostExecute(data);

    if (data != null) {
      mDelegate.onModelProcessed(data, mWidth, mHeight, mRotation);
    }
    mDelegate.onModelProcessorTaskCompleted();
  }
}
