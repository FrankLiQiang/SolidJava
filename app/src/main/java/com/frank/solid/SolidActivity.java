package com.frank.solid;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class SolidActivity extends Activity {
    protected static int               PointCount = 1;
    protected static int               EdgeCount  = 1;
    protected        float             edgeLength;
    protected        float[]           pX;
    protected        float[]           pY;
    protected        float[]           pZ;
    protected        int[][]           edge;
    protected        Common.PointXYZ[] P;
    protected        Common.PointXYZ[] oldP;
    protected        Common.PointF[]   p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        Common.getScreenSize(getWindowManager());

        Common.DepthZ = Common._screenWidth / 2f;
        LinearLayout _linearLayout = new LinearLayout(this);
        Common.LayoutSettingView layoutSettingView = new Common.LayoutSettingView(this);
        _linearLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams mParams = new LayoutParams(Common._screenWidth, Common._screenHeight);
        layoutSettingView.setLayoutParams(mParams);
        _linearLayout.addView(layoutSettingView, mParams);

        setContentView(_linearLayout);
        for (int i = 0; i < PointCount; i++) {
            P[i] = new Common.PointXYZ(pX[i], pY[i], pZ[i]);
            oldP[i] = new Common.PointXYZ(pX[i], pY[i], pZ[i]);
            p[i] = new Common.PointF();
            Common.resetPointF(p[i], P[i]);
        }
        Common.StartX = 300;
        Common.StartY = 100;
        Common.EndX = 100;
        Common.EndY = 300;
        convert();
    }

    protected void Initialization(int pointCount, int edgeCount, int[][] newEdge) {
        Common.getScreenSize(getWindowManager());

        Common.Eye.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, Common._screenHeight * 2);
        PointCount = pointCount;
        EdgeCount = edgeCount;
        pX = new float[PointCount];
        pY = new float[PointCount];
        pZ = new float[PointCount];
        edge = newEdge;
        P = new Common.PointXYZ[PointCount];
        oldP = new Common.PointXYZ[PointCount];
        p = new Common.PointF[PointCount];
    }

    public void saveOldPoints() {
        for (int i = 0; i < PointCount; i++) {
            oldP[i].reset(P[i].x, P[i].y, P[i].z);
        }
    }

    public void convert() {
        for (int i = 0; i < PointCount; i++) {
            Common.getNewPoint(oldP[i], P[i]);
            Common.resetPointF(p[i], P[i]);
        }
    }

    public void convert2() {
        for (int i = 0; i < PointCount; i++) {
            Common.getNewSizePoint(oldP[i], P[i]);
            Common.resetPointF(p[i], P[i]);
        }
    }

    public void drawSolid(Canvas canvas, Paint paint) {
        for (int i = 0; i < EdgeCount; i++) {
            drawEdge(canvas, paint, i);
        }
        paint.setTextSize(60);
        for (int i = 0; i < PointCount; i++) {
            canvas.drawText(i + "", p[i].x + 20, p[i].y - 40, paint);
        }
    }

    protected void drawEdge(Canvas canvas, Paint paint, int index) {
        canvas.drawLine(p[edge[index][0]].x, p[edge[index][0]].y, p[edge[index][1]].x, p[edge[index][1]].y, paint);
    }
}
