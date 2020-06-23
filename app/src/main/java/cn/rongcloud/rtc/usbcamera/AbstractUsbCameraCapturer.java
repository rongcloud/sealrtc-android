package cn.rongcloud.rtc.usbcamera;

import static java.lang.Math.abs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.opengl.GLES11Ext;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.core.EglBase;
import cn.rongcloud.rtc.core.GlUtil;
import cn.rongcloud.rtc.engine.view.RongRTCVideoViewManager;
import cn.rongcloud.rtc.utils.FinLog;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Created by wangw on 2019/4/29. */
public abstract class AbstractUsbCameraCapturer
        implements USBMonitor.OnDeviceConnectListener // , SurfaceTextureHelper.OnTextureFrameAvailableListener
{

    public static final String TAG = "AbstractUsbCameraCapturer";
    public static final int STATE_IDLE = 0;
    public static final int STATE_START = 1;
    public static final int STATE_STOP = -1;

    protected Context mContext;
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private Handler mUiHandler = new Handler(Looper.getMainLooper());
    private AtomicInteger mState = new AtomicInteger(STATE_IDLE);
    private Handler mWorkerHandler;
    private long mWorkerThreadID = -1;
    private EglBase mEglBase;
    private SurfaceTexture mSurfaceTexture;
    protected int mReqWidth;
    protected int mReqHeight;
    private int mSelectWidth;
    private int mSelectHeight;

    public AbstractUsbCameraCapturer(Context context, RCRTCVideoResolution videoResolution) {
        mContext = context;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mWorkerHandler = new Handler(handlerThread.getLooper());
        Thread thread = mWorkerHandler.getLooper().getThread();
        mWorkerThreadID = thread.getId();
        mSelectWidth = mReqWidth = videoResolution.getWidth();
        mSelectHeight = mReqHeight = videoResolution.getHeight();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                onInit();
            }
        });
    }

    private void onInit() {
        mUSBMonitor = new USBMonitor(mContext, this);
        mUSBMonitor.register();
    }

    protected final synchronized void queueEvent(final Runnable task) {
        queueEvent(task, 0);
    }

    protected final synchronized void queueEvent(final Runnable task, final long delayMillis) {
        if ((task == null) || (mWorkerHandler == null)) return;
        try {
            mWorkerHandler.removeCallbacks(task);
            if (delayMillis > 0) {
                mWorkerHandler.postDelayed(task, delayMillis);
            } else if (mWorkerThreadID == Thread.currentThread().getId()) {
                task.run();
            } else {
                mWorkerHandler.post(task);
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    protected final synchronized void removeEvent(final Runnable task) {
        if (task == null) return;
        try {
            mWorkerHandler.removeCallbacks(task);
        } catch (final Exception e) {
            // ignore
        }
    }

    @Override
    public void onAttach(UsbDevice usbDevice) {
        log("[onAttach]", usbDevice.toString());
        showToast("USB-onAttach");
        if (mState.get() == STATE_START) openCamera();
    }

    @Override
    public void onDettach(UsbDevice usbDevice) {
        log("[onDettach]", usbDevice.toString());
        showToast("USB-onDettach");
        releaseCamera();
    }

    @Override
    public void onConnect(final UsbDevice usbDevice, final USBMonitor.UsbControlBlock usbControlBlock, boolean b) {
        log("[onConnect]", usbDevice.toString());
        showToast("USB-onConnect");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mState.get() == STATE_START) {
                    onInitCamera(usbDevice, usbControlBlock);
                }
            }
        });
    }

    /**
     * 初始化摄像头
     *
     * @param usbDevice
     * @param usbControlBlock
     */
    private void onInitCamera(UsbDevice usbDevice, USBMonitor.UsbControlBlock usbControlBlock) {
        releaseCamera();
        final UVCCamera camera = new UVCCamera();
        camera.open(usbControlBlock);
        camera.setStatusCallback(new IStatusCallback() {
            @Override
            public void onStatus(final int statusClass,
                final int event, final int selector, final int statusAttribute, final ByteBuffer data) {
                log("onStatus", "statusClass=" + statusClass + "; " + "event=" + event + "; " +
                    "selector=" + selector + "; " + "statusAttribute=" + statusAttribute + "; ");
            }
        });
        camera.setButtonCallback(new IButtonCallback() {
            @Override
            public void onButton(final int button, final int state) {
                log("onButton", "(button=" + button + "; " + "state=" + state + ")");
            }
        });
        Size size = getClosestSupportedSize(camera.getSupportedSizeList(), mReqWidth, mReqHeight);
        if (size != null) {
            mSelectWidth = size.width;
            mSelectHeight = size.height;
        }
        try {
            log("onInitCamera", "select: " + mSelectWidth +
                "x" + mSelectHeight + " , req: " + mReqWidth + "x" + mReqHeight);
            camera.setPreviewSize(mSelectWidth, mSelectHeight, 1, 60, UVCCamera.FRAME_FORMAT_YUYV, 1.0f);
        } catch (final IllegalArgumentException e) {
            // fallback to YUV mode
            try {
                camera.setPreviewSize(mSelectWidth, mSelectHeight, 1, 60, UVCCamera.FRAME_FORMAT_YUYV, 1.0f);
            } catch (final IllegalArgumentException e1) {
                camera.destroy();
                return;
            }
        }
        //        mUVCCamera.setFrameCallback(new IFrameCallback() {
        //            @Override
        //            public void onFrame(ByteBuffer byteBuffer) {
        //                log("onFrame","length="+byteBuffer.remaining());
        //                byte[] bytes = new byte[byteBuffer.remaining()];
        //                byteBuffer.get(bytes);
        //                AbstractUsbCameraCapturer.this.onFrame(bytes);
        //            }
        //        },UVCCamera.PIXEL_FORMAT_NV21);
        try {
            createTexture();
            camera.setPreviewTexture(mSurfaceTexture);
            mUVCCamera = camera;
            if (mState.get() == STATE_START) onStartPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTexture() throws Exception {
        try {
            mEglBase = EglBase.create(EglBase.create(RongRTCVideoViewManager.getInstance().getBaseContext()).getEglBaseContext(), EglBase.CONFIG_PIXEL_BUFFER);
            // Both these statements have been observed to fail on rare occasions, see
            // BUG=RongRTC:5682.
            mEglBase.createDummyPbufferSurface();
            mEglBase.makeCurrent();
        } catch (Exception e) {
            // Clean up before rethrowing the exception.
            mEglBase.release();
            throw e;
        }
        final int oesTextureId = GlUtil.generateTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        mSurfaceTexture = new SurfaceTexture(oesTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                try {
                    synchronized (EglBase.lock) {
                        mSurfaceTexture.updateTexImage();
                    }
                } catch (Exception e) {
                    FinLog.e(TAG, "updateTexImage Failed: " + e.getMessage());
                    e.printStackTrace();
                }

                final float[] transformMatrix = new float[16];
                surfaceTexture.getTransformMatrix(transformMatrix);
                final long timestampNs = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) ?
                    surfaceTexture.getTimestamp() :
                    TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
                onTextureFrameAvailable(oesTextureId, transformMatrix, timestampNs);
            }
        });
    }

    protected void openCamera() {
        List<UsbDevice> deviceList = mUSBMonitor.getDeviceList();
        if (deviceList == null || deviceList.isEmpty()) {
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) {
                        showToast("未检测到USB摄像头");
                    } else {
                        AlertDialog dialog =
                            new AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setMessage("未检测到USB摄像头，请从以下几个步骤排查原因:" +
                                    "\n\n  1. OTG功能是否已开启" +
                                    "\n  2. USB摄像头设备是否已连接" +
                                    "\n\n如果以上步骤都已检查通过还是看不到视频画面则有可能您的手机不支持OTG功能!")
                                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                        dialog.show();
                    }
                }
            });
            return;
        }
        ArrayList<UsbDevice> myDevices = new ArrayList<>();
        for (UsbDevice usbDevice : deviceList) {
            if (usbDevice.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                myDevices.add(usbDevice);
            }
        }
        if (myDevices.size() != 0) {
            UsbDevice cameraDevice = null;
            if (myDevices.size() > 1) {
                for (UsbDevice usbDevice : myDevices) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                            && usbDevice.getProductName().contains("Camera")) {
                        cameraDevice = usbDevice;
                        break;
                    }
                }
            } else {
                cameraDevice = myDevices.get(0);
            }
            if (cameraDevice != null) {
                log("openCamera", "Find camera device: " + cameraDevice.getDeviceName());
                mUSBMonitor.requestPermission(cameraDevice);
            } else {
                showToast("未找到可用的USB摄像头");
                FinLog.e(TAG, "未找到可用的USB摄像头");
            }
        } else {
            showToast("未找到可用的USB摄像头");
            FinLog.e(TAG, "未找到可用的USB摄像头");
        }
    }

    /** 开始预览 */
    protected void onStartPreview() {
        if (mUVCCamera == null) {
            openCamera();
        } else {
            mUVCCamera.startPreview();
        }
    }

    protected void onStopPreview() {
        if (mUVCCamera != null) {
            mUVCCamera.stopPreview();
        }
    }

    @Override
    public void onDisconnect(UsbDevice usbDevice, USBMonitor.UsbControlBlock usbControlBlock) {
        log("[onDisconnect]", usbDevice.toString());
        showToast("USB-onDisconnect");
        queueEvent(
                new Runnable() {
                    @Override
                    public void run() {
                        releaseCamera();
                    }
                });
    }

    @Override
    public void onCancel(UsbDevice usbDevice) {
        log("[onCancel]", usbDevice.toString());
    }

    protected void showToast(final String msg) {
        postUIThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void postUIThread(Runnable runnable) {
        mUiHandler.post(runnable);
    }

    protected void log(String funName, String msg) {
        FinLog.d(TAG, "[" + funName + "] ==>" + msg);
    }

    /** 释放所有资源 */
    public void onRelease() {
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        releaseCamera();
        if (mWorkerHandler != null) {
            try {
                mWorkerHandler.getLooper().quit();
            } catch (final Exception e) {
                //
            }
            mWorkerHandler = null;
        }
    }

    /** 释放Camera */
    protected void releaseCamera() {

        try {
            if (mUVCCamera != null) {
                mUVCCamera.setStatusCallback(null);
                mUVCCamera.setButtonCallback(null);
                mUVCCamera.close();
                mUVCCamera.destroy();
            }
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            if (mEglBase != null) mEglBase.release();
        } catch (final Exception e) {
            //
        }
        mUVCCamera = null;
    }

    protected void setState(int state) {
        mState.set(state);
    }

    public int getState() {
        return mState.get();
    }

    //    @Override
    public final void onTextureFrameAvailable(
            int oesTextureId, float[] transformMatrix, long timestampNs) {
        onTextureFrameAvailable(
                oesTextureId, mSelectWidth, mSelectHeight, transformMatrix, timestampNs);
    }

    protected abstract void onTextureFrameAvailable(
            int oesTextureId, int width, int height, float[] transformMatrix, long timestampNs);

    /**
     * Camera 数据回调
     *
     * @param bytes
     */
    protected final void onFrame(byte[] bytes) {
        onFrame(bytes, mSelectWidth, mSelectHeight);
    }

    protected abstract void onFrame(byte[] bytes, int selectWidth, int selectHeight);

    public Size getClosestSupportedSize(
            List<Size> supportedSizes, final int requestedWidth, final int requestedHeight) {
        Size size =
                Collections.min(
                        supportedSizes,
                        new ClosestComparator<Size>() {
                            @Override
                            int diff(Size size) {
                                // First time, try to select a Size whose width and length are not
                                // shorter than the requested.
                                // If no Size is suitable, then select the biggest Size which is
                                // closest with the requested.
                                if (size.width - requestedWidth >= 0
                                        && size.height - requestedHeight >= 0)
                                    return size.width
                                            - requestedWidth
                                            + size.height
                                            - requestedHeight;
                                else
                                    return (abs(requestedWidth - size.width)
                                            + abs(requestedHeight - size.height));
                            }
                        });
        FinLog.d(TAG, "request Size: " + requestedWidth + "x" + requestedHeight);
        FinLog.d(TAG, "Found closest capture Size: " + size.toString());
        return size;
    }

    private abstract class ClosestComparator<T> implements Comparator<T> {
        // Difference between supported and requested parameter.
        abstract int diff(T supportedParameter);

        @Override
        public int compare(T t1, T t2) {
            return diff(t1) - diff(t2);
        }
    }
}
