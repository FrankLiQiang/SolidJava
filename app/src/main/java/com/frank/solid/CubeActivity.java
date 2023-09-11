package com.frank.solid;

import android.os.Bundle;

public class CubeActivity extends SolidActivity {

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

//        float diff0 = 0.289f * edgeLength;
//        float diff1 = 0.866f * edgeLength;
//        float diff3 = 0.816f * edgeLength;
//        float diff4 = 0.408f * edgeLength;
//        float diff5 = 0.707f * edgeLength;
//        pX[0] = Common.ObjCenter.x;
//        pX[2] = pX[0];
//        pX[4] = pX[0];
//        pX[6] = pX[0];
//        pX[3] = Common.ObjCenter.x - diff5;
//        pX[7] = Common.ObjCenter.x - diff5;
//        pX[1] = Common.ObjCenter.x + diff5;
//        pX[5] = Common.ObjCenter.x + diff5;
//
//        pY[0] = Common.ObjCenter.y - diff1;
//        pY[6] = Common.ObjCenter.y +  diff1;
//        pY[2] = Common.ObjCenter.y + diff0;
//        pY[5] = Common.ObjCenter.y + diff0;
//        pY[7] = Common.ObjCenter.y + diff0;
//        pY[1] = Common.ObjCenter.y - diff0;
//        pY[3] = Common.ObjCenter.y - diff0;
//        pY[4] = Common.ObjCenter.y - diff0;
//
//        pZ[0] = Common.ObjCenter.z;
//        pZ[6] = Common.ObjCenter.z;
//        pZ[2] = Common.ObjCenter.z + diff3;
//        pZ[4] = Common.ObjCenter.z - diff3;
//        pZ[1] = Common.ObjCenter.z + diff4;
//        pZ[3] = Common.ObjCenter.z + diff4;
//        pZ[5] = Common.ObjCenter.z - diff4;
//        pZ[7] = Common.ObjCenter.z - diff4;
        super.onCreate(savedInstanceState);
    }
}
