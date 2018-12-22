package org.reactnative.camera.events;

import android.support.v4.util.Pools;
import android.util.SparseArray;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import org.reactnative.camera.CameraViewManager;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;


public class ModelProcessedEvent extends Event<ModelProcessedEvent> {

  private static final Pools.SynchronizedPool<ModelProcessedEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);


  private double mScaleX;
  private double mScaleY;
  private ByteBuffer mData;
  private ImageDimensions mImageDimensions;

  private ModelProcessedEvent() {}

  public static ModelProcessedEvent obtain(
      int viewTag,
      ByteBuffer data,
      ImageDimensions dimensions,
      double scaleX,
      double scaleY) {
    ModelProcessedEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new ModelProcessedEvent();
    }
    event.init(viewTag, data, dimensions, scaleX, scaleY);
    return event;
  }

  private void init(
      int viewTag,
      ByteBuffer data,
      ImageDimensions dimensions,
      double scaleX,
      double scaleY) {
    super.init(viewTag);
    mData = data;
    mImageDimensions = dimensions;
    mScaleX = scaleX;
    mScaleY = scaleY;
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_MODEL_PROCESSED.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    FloatBuffer fbuf = mData.asFloatBuffer();
    float[] farray = new float[fbuf.remaining()];
    WritableArray dataList = Arguments.fromArray(farray);

    WritableMap event = Arguments.createMap();
    event.putString("type", "textBlock");
    event.putArray("data", dataList);
    event.putInt("target", getViewTag());
    return event;
  }

  private WritableMap rotateTextX(WritableMap text) {
    ReadableMap faceBounds = text.getMap("bounds");

    ReadableMap oldOrigin = faceBounds.getMap("origin");
    WritableMap mirroredOrigin = FaceDetectorUtils.positionMirroredHorizontally(
        oldOrigin, mImageDimensions.getWidth(), mScaleX);

    double translateX = -faceBounds.getMap("size").getDouble("width");
    WritableMap translatedMirroredOrigin = FaceDetectorUtils.positionTranslatedHorizontally(mirroredOrigin, translateX);

    WritableMap newBounds = Arguments.createMap();
    newBounds.merge(faceBounds);
    newBounds.putMap("origin", translatedMirroredOrigin);

    text.putMap("bounds", newBounds);

    ReadableArray oldComponents = text.getArray("components");
    WritableArray newComponents = Arguments.createArray();
    for (int i = 0; i < oldComponents.size(); ++i) {
      WritableMap component = Arguments.createMap();
      component.merge(oldComponents.getMap(i));
      rotateTextX(component);
      newComponents.pushMap(component);
    }
    text.putArray("components", newComponents);

    return text;
  }

}
