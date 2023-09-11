package com.frank.solid;

import android.os.Bundle;

public class TetrahedronActivity extends SolidActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int[][] thisEdge = {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}};
        Initialization(4, 6, thisEdge);
        edgeLength = Common._screenWidth / 2f;
        Common.ObjCenter.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, -edgeLength / 2);

        pX[0] = Common._screenWidth / 2f + edgeLength / 2;
        pY[0] = Common._screenHeight / 2f - edgeLength / 2;
        pZ[0] = -edgeLength;

        pX[1] = Common._screenWidth / 2f - edgeLength / 2;
        pY[1] = pY[0];
        pZ[1] = 0;

        pX[2] = pX[1];
        pY[2] = Common._screenHeight / 2f + edgeLength / 2;
        pZ[2] = pZ[0];

        pX[3] = pX[0];
        pY[3] = pY[2];
        pZ[3] = 0;

        super.onCreate(savedInstanceState);
    }
}
