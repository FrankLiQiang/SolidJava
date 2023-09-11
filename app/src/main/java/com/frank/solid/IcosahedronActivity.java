package com.frank.solid;

import android.os.Bundle;

public class IcosahedronActivity extends SolidActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        float m = 0.5257f, n = 0.85065f;
        int[][] thisEdge = {{2, 9}, {2, 10}, {9, 10}, {5, 0}, {5, 11}, {0, 11}, {8, 11}, {8, 0}, {3, 8}, {3, 11}, {7, 8}, {7, 3}, {2, 3}, {2, 7}, {1, 5}, {1,
                0}, {4, 0}, {4, 8}, {4, 9}, {4, 7}, {9, 7}, {1, 9}, {1, 4}, {6, 5}, {6, 11}, {1, 10}, {5, 10}, {6, 10}, {2, 6}, {6, 3}};
        Initialization(12, 30, thisEdge);

        edgeLength = Common._screenWidth / 2f;
        Common.ObjCenter.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, -edgeLength / 2);

        //{(±m,0,±n), (0,±n,±m), (±n,±m,0)}
        pX[0] = m;
        pY[0] = 0;
        pZ[0] = n;

        pX[1] = -m;
        pY[1] = 0;
        pZ[1] = n;

        pX[2] = -m;
        pY[2] = 0;
        pZ[2] = -n;

        pX[3] = m;
        pY[3] = 0;
        pZ[3] = -n;

        pX[4] = 0;
        pY[4] = n;
        pZ[4] = m;

        pX[5] = 0;
        pY[5] = -n;
        pZ[5] = m;

        pX[6] = 0;
        pY[6] = -n;
        pZ[6] = -m;

        pX[7] = 0;
        pY[7] = n;
        pZ[7] = -m;

        pX[8] = n;
        pY[8] = m;
        pZ[8] = 0;

        pX[9] = -n;
        pY[9] = m;
        pZ[9] = 0;

        pX[10] = -n;
        pY[10] = -m;
        pZ[10] = 0;

        pX[11] = n;
        pY[11] = -m;
        pZ[11] = 0;

        for (int i = 0; i < PointCount; i++) {
            pX[i] = pX[i] * edgeLength + Common.ObjCenter.x;
            pY[i] = pY[i] * edgeLength + Common.ObjCenter.y;
            pZ[i] = pZ[i] * edgeLength + Common.ObjCenter.z;
        }
        super.onCreate(savedInstanceState);
    }
}
