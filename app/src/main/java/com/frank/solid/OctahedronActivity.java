package com.frank.solid;

import android.os.Bundle;

public class OctahedronActivity extends SolidActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int[][] thisEdge = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}, {2, 4}, {3, 4}, {1, 3}, {5, 1}, {5, 2}, {5, 3}, {5, 4}};
        Initialization(6, 12, thisEdge);
        edgeLength = Common._screenWidth * 0.75f;
        Common.ObjCenter.reset(Common._screenWidth / 2f, Common._screenHeight / 2f, -edgeLength / 2);

        pX[0] = Common._screenWidth / 2f;
        pY[0] = Common._screenHeight / 2f - edgeLength * 0.7071f;
        pZ[0] = -edgeLength / 2;

        pX[1] = Common._screenWidth / 2f - edgeLength / 2;
        pY[1] = Common._screenHeight / 2f;
        pZ[1] = -edgeLength / 2 - edgeLength / 2;

        pX[2] = Common._screenWidth / 2f + edgeLength / 2;
        pY[2] = Common._screenHeight / 2f;
        pZ[2] = -edgeLength / 2 - edgeLength / 2;

        pX[3] = Common._screenWidth / 2f - edgeLength / 2;
        pY[3] = Common._screenHeight / 2f;
        pZ[3] = -edgeLength / 2 + edgeLength / 2;

        pX[4] = Common._screenWidth / 2f + edgeLength / 2;
        pY[4] = Common._screenHeight / 2f;
        pZ[4] = -edgeLength / 2 + edgeLength / 2;

        pX[5] = Common._screenWidth / 2f;
        pY[5] = Common._screenHeight / 2f + edgeLength * 0.7071f;
        pZ[5] = -edgeLength / 2;

        super.onCreate(savedInstanceState);
    }
}
