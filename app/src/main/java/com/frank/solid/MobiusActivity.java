package com.frank.solid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MobiusActivity extends Activity {
    protected float     R, r, r0, angle,x0;
    private   int       parallelNum     = 17;
    private   int       ellipsePointNum = 64;
    private   Common.PointXYZ[] lineGroup    = new Common.PointXYZ[ellipsePointNum * 2];
    private   Common.PointXYZ[] oldLineGroup = new Common.PointXYZ[ellipsePointNum * 2];
    private   Common.PointF[] line2Group    = new Common.PointF[ellipsePointNum * 2];
    private   Ellipse[] ellipseGroup    = new Ellipse[parallelNum];
    private   Ellipse[] oldEllipseGroup = new Ellipse[parallelNum];
    private int rotateNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        rotateNum = intent.getIntExtra("rotateNum", 1);
        Common.getScreenSize(getWindowManager());
        Common.DepthZ = Common._screenWidth / 2f;

        LinearLayout _linearLayout = new LinearLayout(this);
        Common.LayoutSettingView layoutSettingView = new Common.LayoutSettingView(this);
        _linearLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams mParams = new LayoutParams(Common._screenWidth, Common._screenHeight);
        layoutSettingView.setLayoutParams(mParams);
        _linearLayout.addView(layoutSettingView, mParams);

        createEllipse();
        setContentView(_linearLayout);
    }

    private void createEllipse() {
        Common.Eye.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, Common._screenHeight * 2);
        R = Common._screenWidth * 0.4f;
        r = R * 0.3f;
        Common.ObjCenter.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, -R / 2);

        //纬线
        int lineNum = 0;
        for (int i = 0; i < parallelNum; i++) {
            ellipseGroup[i] = new Ellipse();
            oldEllipseGroup[i] = new Ellipse();

            for (int j = 0; j < ellipsePointNum; j++) {
                r0 = r - 2 * r / (parallelNum -1) * i;
                angle = (float)(2 * Common.PI / ellipsePointNum * j);
                x0 = R - r0 * (float) Math.sin(angle / 2 * rotateNum);
                ellipseGroup[i].ellipse[j].x = Common.ObjCenter.x - x0 * (float) Math.sin(angle);
                ellipseGroup[i].ellipse[j].y = Common.ObjCenter.y - r0 * (float) Math.cos(angle / 2 * rotateNum);
                ellipseGroup[i].ellipse[j].z = Common.ObjCenter.z + x0 * (float) Math.cos(angle);

                if(i == 0 || i == parallelNum - 1) {
                    lineGroup[lineNum] = new Common.PointXYZ();
                    oldLineGroup[lineNum] = new Common.PointXYZ();
                    lineGroup[lineNum++].reset(ellipseGroup[i].ellipse[j].x,
                                                ellipseGroup[i].ellipse[j].y,
                                                ellipseGroup[i].ellipse[j].z);
                }
            }
        }


        for (int i = 0; i < parallelNum; i++) {
            for (int j = 0; j < ellipsePointNum; j++) {
                Common.resetPointF(ellipseGroup[i].ellipse2[j], ellipseGroup[i].ellipse[j]);

                oldEllipseGroup[i].ellipse[j].x = ellipseGroup[i].ellipse[j].x;
                oldEllipseGroup[i].ellipse[j].y = ellipseGroup[i].ellipse[j].y;
                oldEllipseGroup[i].ellipse[j].z = ellipseGroup[i].ellipse[j].z;

            }
        }
        for (int i = 0; i < ellipsePointNum * 2; i++) {
            line2Group[i] = new Common.PointF();
            Common.resetPointF(line2Group[i], lineGroup[i]);
            oldLineGroup[i].x = lineGroup[i].x;
            oldLineGroup[i].y = lineGroup[i].y;
            oldLineGroup[i].z = lineGroup[i].z;
        }

    }

    public void saveOldPoints() {
        for (int i = 0; i < parallelNum; i++) {
            for (int j = 0; j < ellipsePointNum; j++) {
                oldEllipseGroup[i].ellipse[j].reset(ellipseGroup[i].ellipse[j].x, ellipseGroup[i].ellipse[j].y, ellipseGroup[i].ellipse[j].z);
            }
        }
        for (int i = 0; i < ellipsePointNum * 2; i++) {
            oldLineGroup[i].reset(lineGroup[i].x, lineGroup[i].y, lineGroup[i].z);
        }
    }

    public void convert() {
        for (int i = 0; i < parallelNum; i++) {
            for (int j = 0; j < ellipsePointNum; j++) {
                Common.getNewPoint(oldEllipseGroup[i].ellipse[j], ellipseGroup[i].ellipse[j]);
                Common.resetPointF(ellipseGroup[i].ellipse2[j], ellipseGroup[i].ellipse[j]);
            }
        }
        for (int i = 0; i < ellipsePointNum * 2; i++) {
            Common.getNewPoint(oldLineGroup[i], lineGroup[i]);
            Common.resetPointF(line2Group[i], lineGroup[i]);
        }
    }

    public void convert2() {
        for (int i = 0; i < parallelNum; i++) {
            for (int j = 0; j < ellipsePointNum; j++) {
                Common.getNewSizePoint(oldEllipseGroup[i].ellipse[j], ellipseGroup[i].ellipse[j]);
                Common.resetPointF(ellipseGroup[i].ellipse2[j], ellipseGroup[i].ellipse[j]);
            }
        }
        for (int i = 0; i < ellipsePointNum * 2; i++) {
            Common.getNewSizePoint(oldLineGroup[i], lineGroup[i]);
            Common.resetPointF(line2Group[i], lineGroup[i]);
        }
    }

    public void drawRing(Canvas canvas, Paint paint) {
        for (int i = 0; i < parallelNum; i++) {
            drawEllipse(i, canvas, paint);
        }
        for (int i = 0; i < ellipsePointNum; i++) {
            canvas.drawLine(line2Group[i].x, line2Group[i].y,
                    line2Group[ellipsePointNum + i].x,
                    line2Group[ellipsePointNum + i].y, paint);
        }
    }
    private void drawEllipse(int index, Canvas canvas, Paint paint) {
        for (int i = 0; i < ellipsePointNum - 1; i++) {
            canvas.drawLine(ellipseGroup[index].ellipse2[i].x, ellipseGroup[index].ellipse2[i].y, ellipseGroup[index].ellipse2[i + 1].x,
                    ellipseGroup[index].ellipse2[i + 1].y, paint);
        }

        int theIndex = index;
        if((float)rotateNum / 2 != rotateNum / 2) {
            theIndex = parallelNum - index - 1;
        }
        canvas.drawLine(ellipseGroup[index].ellipse2[0].x,
                ellipseGroup[index].ellipse2[0].y,
                ellipseGroup[theIndex].ellipse2[ellipsePointNum - 1].x,
                ellipseGroup[theIndex].ellipse2[ellipsePointNum - 1].y, paint);
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
