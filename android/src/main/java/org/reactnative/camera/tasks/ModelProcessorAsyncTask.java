package org.reactnative.camera.tasks;

import java.nio.ByteBuffer;

import com.google.android.gms.vision.text.TextRecognizer;
import java.util.concurrent.TimeUnit;

public class ModelProcessorAsyncTask extends android.os.AsyncTask<Void, Void, ByteBuffer> {

  private ModelProcessorAsyncTaskDelegate mDelegate;
  private TextRecognizer mModelProcessor;
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;

  public ModelProcessorAsyncTask(
      ModelProcessorAsyncTaskDelegate delegate,
      TextRecognizer textRecognizer,
      byte[] imageData,
      int width,
      int height,
      int rotation
  ) {
    mDelegate = delegate;
    mModelProcessor = textRecognizer;
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
  }
    
  @Override
  protected ByteBuffer doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mModelProcessor == null || !mModelProcessor.isOperational()) {
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
