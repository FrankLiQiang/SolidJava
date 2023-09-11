package com.frank.solid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class PictureRingActivity extends Activity  implements View.OnClickListener {
    protected static float R, r, Old_r, r0;

    static {
        System.loadLibrary("native-lib");
    }

    private ImageView _ivChosen;
    private Bitmap originalBMP, newBMP;
    private Common.PointXYZ Arctic      = new Common.PointXYZ();
    private Common.PointXYZ Meridian    = new Common.PointXYZ();
    private Common.PointF   ArcticF     = new Common.PointF();
    private Common.PointF   MeridianF   = new Common.PointF();
    private Common.PointF   Meridian0F  = new Common.PointF();
    private Common.PointXYZ OldArctic   = new Common.PointXYZ();
    private Common.PointXYZ OldMeridian = new Common.PointXYZ();
    private byte[]          bmpByteArray, newBmpByteArray;
    private LinearLayout _linearLayout;
    private MyHandler saveHandler = new MyHandler(this);

    public native int transforms(byte[] in, byte[] out, float ex, float ey, float ez, float r);

    public native void Initialization(float z, float cx, float cy, float cz);

    public native void Initialization2(int width0, int height0, int width, int height, float R, float r, float r0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(com.frank.solid.R.layout.choose_img);
        try {
            originalBMP = Common.getBitmapFromUri(MainActivity._uri, getContentResolver());
        } catch (Exception e) {
            e.toString();
        }
        new ReadyThread().start();

        _ivChosen = findViewById(com.frank.solid.R.id.chosen);
        _ivChosen.setImageBitmap(originalBMP);
        _ivChosen.setOnClickListener(this);
    }

    public void saveOldPoints() {
        OldArctic.reset(Arctic.x, Arctic.y, Arctic.z);
        OldMeridian.reset(Meridian.x, Meridian.y, Meridian.z);
        OldArctic.reset(Arctic.x, Arctic.y, Arctic.z);
        Old_r = r;
    }

    public void convert() {
        Common.getNewPoint(OldArctic, Arctic);
        Common.getNewPoint(OldMeridian, Meridian);
        Common.resetPointF(ArcticF, Arctic);
        Common.resetPointF(MeridianF, Meridian);
    }

    public void convert2() {
        Common.getNewSizePoint(OldArctic, Arctic, false);
        r = Old_r * Common.distance;
        Common.getNewSizePoint(OldMeridian, Meridian, false);
    }

    private void bmp2byte() {
        int bytes = originalBMP.getByteCount();

        ByteBuffer buf = ByteBuffer.allocate(bytes);
        originalBMP.copyPixelsToBuffer(buf);

        bmpByteArray = buf.array();

        bytes = newBMP.getByteCount();
        buf = ByteBuffer.allocate(bytes);
        newBMP.copyPixelsToBuffer(buf);

        newBmpByteArray = buf.array();
    }

    public void drawPicRing(Canvas canvas, Paint paint) {
        try {
            int test = transforms(bmpByteArray, newBmpByteArray, Common.Eye.x, Common.Eye.y, Common.Eye.z, r);
            newBMP.copyPixelsFromBuffer(ByteBuffer.wrap(newBmpByteArray));
            canvas.drawBitmap(newBMP, 0, 0, paint);
        } catch (Exception e) {
            String a = e.toString();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == com.frank.solid.R.id.chosen) {
            setContentView(_linearLayout);
        }
    }

    public class ReadyThread extends Thread {
        @Override
        public void run() {
            Common.getScreenSize(getWindowManager());
            Common.DepthZ = 0;      //Common._screenWidth / 2f;

            _linearLayout = new LinearLayout(PictureRingActivity.this);
            Common.LayoutSettingView layoutSettingView = new Common.LayoutSettingView(PictureRingActivity.this);
            _linearLayout.setOrientation(LinearLayout.VERTICAL);
            LayoutParams mParams = new LayoutParams(Common._screenWidth, Common._screenHeight);
            layoutSettingView.setLayoutParams(mParams);
            _linearLayout.addView(layoutSettingView, mParams);

            Common.Eye.reset(0, 0, Common._screenHeight);
            R = Common._screenHeight / 4f;
            r0 = R / 4.0f;
            Common.ObjCenter.reset(0, 0, -R);

            Initialization(Common.DepthZ, Common.ObjCenter.x, Common.ObjCenter.y, Common.ObjCenter.z);

            Arctic.reset(Common.ObjCenter.x, Common.ObjCenter.y - R, Common.ObjCenter.z);
            Meridian.reset(Common.ObjCenter.x - R, Common.ObjCenter.y, Common.ObjCenter.z);
            Common.resetPointF(ArcticF, Arctic);
            Common.resetPointF(MeridianF, Meridian);

            Common.PointXYZ Meridian0 = new Common.PointXYZ();
            Meridian0.reset(Common.ObjCenter.x - R - r0, Common.ObjCenter.y, Common.ObjCenter.z);
            Common.resetPointF(Meridian0F, Meridian0);
            r = Common.ObjCenter.x - Meridian0F.x;

            Old_r = r;
            newBMP = Bitmap.createBitmap(Common._screenWidth, Common._screenHeight, Bitmap.Config.ARGB_8888);
            Initialization2(originalBMP.getWidth(), originalBMP.getHeight(), Common._screenWidth, Common._screenHeight, R, r, r0);

            bmp2byte();
            saveHandler.sendEmptyMessage(0);
        }
    }

    private void showResult(Message msg) {
        setContentView(_linearLayout);
    }

    static class MyHandler extends Handler {
        private final WeakReference<PictureRingActivity> _outer;

        public MyHandler(PictureRingActivity activity) {
            _outer = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PictureRingActivity outer = _outer.get();
            if (outer != null) {
                outer.showResult(msg);
            }
        }
    }
}
