package com.frank.solid;

import android.os.Bundle;

public class DodecahedronActivity extends SolidActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int[][] thisEdge = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 0}, {2, 5}, {5, 6}, {1, 7}, {5, 8}, {7, 8}, {0, 9}, {9, 10}, {7, 10}, {4, 11}, {11, 12}, {9,
                12}, {13, 3}, {13, 14}, {14, 11}, {6, 13}, {15, 16}, {16, 17}, {17, 18}, {18, 19}, {15, 19}, {6, 15}, {8, 19}, {10, 18}, {12, 17}, {14, 16}};
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
    }

    private void getNewPoint1(Common.PointXYZ pOld, Common.PointXYZ pNew, double angleA) {
        pNew.reset(pOld.x - Common.ObjCenter.x, pOld.y - Common.ObjCenter.y, pOld.z - Common.ObjCenter.z);
        double x = pNew.x * Math.cos(angleA) - pNew.z * Math.sin(angleA);
        double z = pNew.z * Math.cos(angleA) + pNew.x * Math.sin(angleA);
        pNew.reset((float) x, pNew.y, (float) z);
        pNew.reset(pNew.x + Common.ObjCenter.x, pNew.y + Common.ObjCenter.y, pNew.z + Common.ObjCenter.z);
    }
}
