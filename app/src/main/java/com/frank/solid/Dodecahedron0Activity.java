package com.frank.solid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;

public class Dodecahedron0Activity extends SolidActivity {

    protected static int FaceCount = 12;
    private int[][] face = {{0, 1, 2, 3, 4}
                            ,{19, 18, 17, 16, 15}
                            ,{6, 13, 3, 2, 5}
                            , {8, 5, 2, 1, 7}
                            ,{7, 1, 0, 9, 10}
                            , {9, 0, 4, 11, 12}
                            , {11, 4, 3, 13, 14}
                            ,{16, 14, 13, 6, 15}
                            ,{15, 6, 5, 8, 19}
                            ,{19, 8, 7, 10, 18}
                            , {18, 10, 9, 12, 17}
                            , {17, 12, 11, 14, 16}
                            };

    private int[][] faceE = {{0, 1, 2, 3, 4}
                            ,{20, 21, 22, 23, 24}
                            ,{19, 16, 2, 5, 6}
                            ,{8, 5, 1, 7, 9}
                            ,{7, 0, 10, 11, 12}
                            ,{10, 4, 13, 14, 15}
                            ,{13, 3, 16, 17, 18}
                            ,{29, 17, 19, 25, 20}
                            ,{25, 6, 8, 26, 24}
                            ,{26, 9, 12, 27, 23}
                            ,{27, 11, 15, 28, 22}
                            ,{28, 14, 18, 29, 21}
                            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int[][] thisEdge = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 0}
                ,{2, 5}, {5, 6}, {1, 7}, {5, 8}, {7, 8}
                ,{0, 9}, {9, 10}, {7, 10}, {4, 11}, {11, 12}
                ,{9,12}, {13, 3}, {13, 14}, {14, 11}, {6, 13}
                ,{15, 16}, {16, 17}, {17, 18}, {18, 19}, {15, 19}
                ,{6, 15}, {8, 19}, {10, 18}, {12, 17}, {14, 16}};
        Initialization(20, 30, thisEdge);

        edgeLength = Common._screenWidth / 4f;
        Common.ObjCenter.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, -edgeLength / 2);

        for (int i = 0; i < 5; i++) {
            pX[i] = Common._screenWidth / 2f + edgeLength * (float) Math.cos(2 * 3.1416 / 5 * (double) i);
            pY[i] = Common._screenHeight / 2f;
            pZ[i] = -edgeLength / 2 + edgeLength * (float) Math.sin(2 * 3.1416 / 5 * (double) i);
        }
        int k = 5;
        float px2 = pX[2];
        Common.PointXYZ newP = new Common.PointXYZ();
        Common.PointXYZ old = new Common.PointXYZ();
        for (int j = 0; j < 5; j++) {
            float xOffset = px2 - edgeLength * (float) Math.cos(2 * 3.1416 / 10);
            for (int i = 0; i < 2; i++) {
                pX[k] = xOffset + edgeLength * (float) Math.cos(6 * 3.1416 / 10 + 2 * 3.1416 / 5 * (double) i);
                pY[k] = Common._screenHeight / 2f;
                pZ[k] = -edgeLength / 2 + edgeLength * (float) Math.sin(6 * 3.1416 / 10 + 2 * 3.1416 / 5 * (double) i);
                k++;
            }

            xOffset = xOffset + edgeLength * (float) Math.cos(2 * 3.1416 / 10);
            double angleA = Math.toRadians(63.4349);
            for (int i = k - 2; i < k; i++) {
                pY[i] = Common._screenHeight / 2f - Math.abs(pX[i] - xOffset) * (float) Math.sin(angleA);
                pX[i] = xOffset + (pX[i] - xOffset) * (float) Math.cos(angleA);
            }
            angleA = 2 * 3.1416 / 5;
            for (int i = 0; i < k; i++) {
                old.reset(pX[i], pY[i], pZ[i]);
                getNewPoint1(old, newP, angleA);
                pX[i] = newP.x;
                pY[i] = newP.y;
                pZ[i] = newP.z;
            }
        }
        float h = edgeLength * 2.227f * 2 * (float) Math.sin(2 * 3.1416 / 10);
        for (int i = 0; i < 5; i++) {
            pX[k] = Common._screenWidth / 2f + edgeLength * (float) Math.cos(3.1416 + 2 * 3.1416 / 5 * (double) i);
            pY[k] = Common._screenHeight / 2f - h;
            pZ[k] = -edgeLength / 2 + edgeLength * (float) Math.sin(3.1416 + 2 * 3.1416 / 5 * (double) i);
            k++;
        }
        for (int i = 0; i < PointCount; i++) {
            pY[i] = pY[i] + h / 2;
        }

        super.onCreate(savedInstanceState);
        Common.setEYE(Common.Eye.x, Common.Eye.y, Common.Eye.z);
    }

    private void getNewPoint1(Common.PointXYZ pOld, Common.PointXYZ pNew, double angleA) {
        pNew.reset(pOld.x - Common.ObjCenter.x, pOld.y - Common.ObjCenter.y, pOld.z - Common.ObjCenter.z);
        double x = pNew.x * Math.cos(angleA) - pNew.z * Math.sin(angleA);
        double z = pNew.z * Math.cos(angleA) + pNew.x * Math.sin(angleA);
        pNew.reset((float) x, pNew.y, (float) z);
        pNew.reset(pNew.x + Common.ObjCenter.x, pNew.y + Common.ObjCenter.y, pNew.z + Common.ObjCenter.z);
    }

    public void drawSolid(Canvas canvas, Paint paint) {
        for (int i = 0; i < FaceCount; i++) {
            if (Common.isVisible(P[face[i][1]].x, P[face[i][1]].y, P[face[i][1]].z,
                    P[face[i][2]].x, P[face[i][2]].y, P[face[i][2]].z,
                    P[face[i][3]].x, P[face[i][3]].y, P[face[i][3]].z) > 0) {
                for (int j = 0; j < 5; j++) {
                    drawEdge(canvas, paint, faceE[i][j]);
                }
            }
        }

//        paint.setTextSize(50);
//        for (int i = 0; i < PointCount; i++) {
//            canvas.drawText(i + "", p[i].x, p[i].y, paint);
//        }
    }
}
