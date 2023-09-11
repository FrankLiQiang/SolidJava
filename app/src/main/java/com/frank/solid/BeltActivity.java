package com.frank.solid;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class BeltActivity extends Activity {
    protected float     R, r, R0;
    private   int       parallelNum     = 17;
    private   int       ellipsePointNum = 64;
    private   Ellipse[] ellipseGroup    = new Ellipse[parallelNum];
    private   Ellipse[] oldEllipseGroup = new Ellipse[parallelNum];

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

        createEllipse();
        Common.StartX = 700;
        Common.StartY = 100;
        Common.EndX = 100;
        Common.EndY = 700;
        convert();

        setContentView(_linearLayout);
    }

    private void createEllipse() {
        Common.Eye.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, Common._screenHeight * 2);
        R = Common._screenWidth * 0.4f;
        r = R * 0.3f;
        Common.ObjCenter.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, -R / 2);

        //纬线
        for (int i = 0; i < parallelNum; i++) {
            ellipseGroup[i] = new Ellipse();
            oldEllipseGroup[i] = new Ellipse();

            R0 = R - r * (float)Math.cos(2 * Common.PI / parallelNum * i);
            for (int j = 0; j < ellipsePointNum; j++) {
                ellipseGroup[i].ellipse[j].x =
                        Common.ObjCenter.x - R * (float) Math.cos(2 * Common.PI / ellipsePointNum * j);
                ellipseGroup[i].ellipse[j].y = Common.ObjCenter.y - r + 2 * r / parallelNum * i;
                ellipseGroup[i].ellipse[j].z =
                        Common.ObjCenter.z + R * (float) Math.sin(2 * Common.PI / ellipsePointNum * j);
            }
        }


        for (int i = 0; i < parallelNum; i++) {
            for (int j = 0; j < ellipsePointNum; j++) {
                Common.resetPointF(ellipseGroup[i].ellipse2[j], ellipseGroup[i].ellipse[j]);

                oldEllipseGroup[i].ellipse[j].x = ellipseGroup[i].ellipse[j].x;
                oldEllipseGroup[i].ellipse[j].y = ellipseGroup[i].ellipse[j].y;
                oldEllipseGroup[i].ellipse[j].z = ellipseGroup[i].ellipse[j].z;

                oldEllipseGroup[i].ellipse2[j].x = ellipseGroup[i].ellipse2[j].x;
                oldEllipseGroup[i].ellipse2[j].y = ellipseGroup[i].ellipse2[j].y;
            }
        }
    }

    public void saveOldPoints() {
        for (int i = 0; i < parallelNum; i++) {
            for (int j = 0; j < ellipsePointNum; j++) {
                oldEllipseGroup[i].ellipse[j].reset(ellipseGroup[i].ellipse[j].x, ellipseGroup[i].ellipse[j].y, ellipseGroup[i].ellipse[j].z);
            }
        }
    }

    public void convert() {
        for (int i = 0; i < parallelNum; i++) {
            for (int j = 0; j < ellipsePointNum; j++) {
                Common.getNewPoint(oldEllipseGroup[i].ellipse[j], ellipseGroup[i].ellipse[j]);
                Common.resetPointF(ellipseGroup[i].ellipse2[j], ellipseGroup[i].ellipse[j]);
            }
        }
    }

    public void convert2() {
        for (int i = 0; i < parallelNum; i++) {
            for (int j = 0; j < ellipsePointNum; j++) {
                Common.getNewSizePoint(oldEllipseGroup[i].ellipse[j], ellipseGroup[i].ellipse[j]);
                Common.resetPointF(ellipseGroup[i].ellipse2[j], ellipseGroup[i].ellipse[j]);
            }
        }
    }

    public void drawRing(Canvas canvas, Paint paint) {
        for (int i = 0; i < parallelNum; i++) {
            drawEllipse(i, canvas, paint);
        }
    }
    private void drawEllipse(int index, Canvas canvas, Paint paint) {
        for (int i = 0; i < ellipsePointNum - 1; i++) {
            canvas.drawLine(ellipseGroup[index].ellipse2[i].x, ellipseGroup[index].ellipse2[i].y, ellipseGroup[index].ellipse2[i + 1].x,
                    ellipseGroup[index].ellipse2[i + 1].y, paint);
        }
        canvas.drawLine(ellipseGroup[index].ellipse2[0].x, ellipseGroup[index].ellipse2[0].y, ellipseGroup[index].ellipse2[ellipsePointNum - 1].x,
                ellipseGroup[index].ellipse2[ellipsePointNum - 1].y, paint);
    }

    protected class Ellipse {
        public Common.PointXYZ[] ellipse  = new Common.PointXYZ[ellipsePointNum];
        public Common.PointF[]   ellipse2 = new Common.PointF[ellipsePointNum];

        Ellipse() {
            for (int i = 0; i < ellipsePointNum; i++) {
                ellipse[i] = new Common.PointXYZ();
                ellipse2[i] = new Common.PointF();
            }
        }
    }
}
