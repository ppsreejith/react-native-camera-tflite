package org.reactnative.camera.tasks;

import android.util.SparseArray;

import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;
import java.util.concurrent.TimeUnit;


public class ModelProcessorAsyncTask extends android.os.AsyncTask<Void, Void, SparseArray<TextBlock>> {

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
  protected SparseArray<TextBlock> doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mModelProcessor == null || !mModelProcessor.isOperational()) {
      return null;
    }

    RNFrame frame = RNFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
    try {
        TimeUnit.SECONDS.sleep(3);
    } catch(Exception e) {}
    return mModelProcessor.detect(frame.getFrame());
  }

  @Override
  protected void onPostExecute(SparseArray<TextBlock> textBlocks) {
    super.onPostExecute(textBlocks);

    if (textBlocks != null) {
      mDelegate.onModelProcessed(textBlocks, mWidth, mHeight, mRotation);
    }
    mDelegate.onModelProcessorTaskCompleted();
  }
}
