package com.frank.solid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;

public class Cube0Activity extends SolidActivity {

    protected static int FaceCount = 6;
    private int[][] face = {{0, 3, 2, 1}, {7, 4, 5, 6}, {0, 4, 7, 3}, {2, 6, 5, 1},
            {3, 7, 6, 2}, {1, 5, 4, 0}};

    private int[][] faceE = {{0, 1, 2, 3}, {4, 5, 6, 7}, {8, 11, 7, 3}, {9, 10, 5, 1},
            {10, 11, 6, 2}, {8, 9, 4, 0}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int[][] thisEdge = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        Initialization(8, 12, thisEdge);
        edgeLength = Common._screenWidth / 2f;
        Common.ObjCenter.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, -edgeLength / 2);

        pX[0] = Common._screenWidth / 4f;
        pX[3] = pX[0];
        pX[4] = pX[0];
        pX[7] = pX[0];

        pX[1] = pX[0] + edgeLength;
        pX[2] = pX[1];
        pX[5] = pX[1];
        pX[6] = pX[1];

        pY[0] = Common._screenHeight / 2f - edgeLength / 2;
        pY[1] = pY[0];
        pY[2] = pY[0];
        pY[3] = pY[0];

        pY[4] = pY[0] + edgeLength;
        pY[5] = pY[4];
        pY[6] = pY[4];
        pY[7] = pY[4];

        pZ[2] = 0;
        pZ[3] = pZ[2];
        pZ[6] = pZ[2];
        pZ[7] = pZ[2];

        pZ[0] = pZ[2] - edgeLength;
        pZ[1] = pZ[0];
        pZ[4] = pZ[0];
        pZ[5] = pZ[0];

        super.onCreate(savedInstanceState);

        Common.setEYE(Common.Eye.x, Common.Eye.y, Common.Eye.z);
    }

    public void drawSolid(Canvas canvas, Paint paint) {
        for (int i = 0; i < FaceCount; i++) {
            if (Common.isVisible(P[face[i][1]].x, P[face[i][1]].y, P[face[i][1]].z,
                    P[face[i][2]].x, P[face[i][2]].y, P[face[i][2]].z,
                    P[face[i][3]].x, P[face[i][3]].y, P[face[i][3]].z) > 0) {
                for (int j = 0; j < 4; j++) {
                    drawEdge(canvas, paint, faceE[i][j]);
                }
            }
        }

//        paint.setTextSize(60);
//        for (int i = 0; i < PointCount; i++) {
//            canvas.drawText(i + "", p[i].x, p[i].y, paint);
//        }
    }
}
