package org.reactnative.camera.tasks;

import org.tensorflow.lite.Interpreter;
import java.nio.ByteBuffer;

import java.util.concurrent.TimeUnit;

public class ModelProcessorAsyncTask extends android.os.AsyncTask<Void, Void, ByteBuffer> {

  private ModelProcessorAsyncTaskDelegate mDelegate;
  private Interpreter mModelProcessor;
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;

  public ModelProcessorAsyncTask(
      ModelProcessorAsyncTaskDelegate delegate,
      Interpreter mModelProcessor,
      byte[] imageData,
      int width,
      int height,
      int rotation
  ) {
    mDelegate = delegate;
    mModelProcessor = mModelProcessor;
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
  }
    
  @Override
  protected ByteBuffer doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mModelProcessor == null) {
      return null;
    }
    try {
        TimeUnit.SECONDS.sleep(3);
    } catch(Exception e) {}
    ByteBuffer buf = ByteBuffer.allocate(2048);
    return buf;
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
