package com.frank.solid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;

import java.nio.ByteBuffer;

public class PictCubeActivity extends SolidActivity {

    protected static int FaceCount = 6;
    private Bitmap newBMP;
    private Bitmap originalBMP;
    private byte[] bmpByteArray, newBmpByteArray;

    private int[][] face = {{0, 3, 2, 1}, {7, 4, 5, 6}, {0, 4, 7, 3}, {2, 6, 5, 1},
            {3, 7, 6, 2}, {1, 5, 4, 0}};

    static {
        System.loadLibrary("native-lib");
    }

    public native void Initialization2(int width, int height, int width00, int height00);

    public native void transforms(int count, int index1, int index2, int index3,
                                  byte[] in, byte[] out
            , float A1x, float A1y
            , float B1x, float B1y
            , float C1x, float C1y
            , float D1x, float D1y
            , float A2x, float A2y
            , float B2x, float B2y
            , float C2x, float C2y
            , float D2x, float D2y
            , float A3x, float A3y
            , float B3x, float B3y
            , float C3x, float C3y
            , float D3x, float D3y
    );

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

        new ReadyThread().start();
        super.onCreate(savedInstanceState);
    }

    private void bmp2byte() {

        int bytes;
        ByteBuffer buf;

        bytes = originalBMP.getByteCount();
        buf = ByteBuffer.allocate(bytes);
        originalBMP.copyPixelsToBuffer(buf);
        bmpByteArray = buf.array();

        bytes = newBMP.getByteCount();
        buf = ByteBuffer.allocate(bytes);
        newBMP.copyPixelsToBuffer(buf);
        newBmpByteArray = buf.array();
    }

    public class ReadyThread extends Thread {
        @Override
        public void run() {
            Common.setEYE(Common.Eye.x, Common.Eye.y, Common.Eye.z);
            try {
                originalBMP = Common.getBitmapFromUri(MainActivity._uri, getContentResolver());
            } catch (Exception e) {
                e.toString();
            }
            newBMP = Bitmap.createBitmap(Common._screenWidth, Common._screenHeight, Bitmap.Config.ARGB_8888);

            bmp2byte();

            Initialization2(Common._screenWidth, Common._screenHeight,
                    originalBMP.getWidth(), originalBMP.getHeight());
        }
    }

    public void drawSolid(Canvas canvas, Paint paint) {
        if (newBmpByteArray == null) return;

        int[] index = {0, 0, 0};
        int[] bmpIndex = {0, 0, 0};
        int count = 0;
        for (int i = 0; i < FaceCount; i++) {
            if (Common.isVisible(P[face[i][1]].x, P[face[i][1]].y, P[face[i][1]].z,
                    P[face[i][2]].x, P[face[i][2]].y, P[face[i][2]].z,
                    P[face[i][3]].x, P[face[i][3]].y, P[face[i][3]].z) > 0) {
                index[count] = i;
                bmpIndex[count++] = i;
            }
        }
        try {
            transforms(count, index[0], index[1], index[2]
                    , bmpByteArray
                    , newBmpByteArray
                    , p[face[index[0]][0]].x, p[face[index[0]][0]].y
                    , p[face[index[0]][1]].x, p[face[index[0]][1]].y
                    , p[face[index[0]][2]].x, p[face[index[0]][2]].y
                    , p[face[index[0]][3]].x, p[face[index[0]][3]].y

                    , p[face[index[1]][0]].x, p[face[index[1]][0]].y
                    , p[face[index[1]][1]].x, p[face[index[1]][1]].y
                    , p[face[index[1]][2]].x, p[face[index[1]][2]].y
                    , p[face[index[1]][3]].x, p[face[index[1]][3]].y

                    , p[face[index[2]][0]].x, p[face[index[2]][0]].y
                    , p[face[index[2]][1]].x, p[face[index[2]][1]].y
                    , p[face[index[2]][2]].x, p[face[index[2]][2]].y
                    , p[face[index[2]][3]].x, p[face[index[2]][3]].y
            );

            newBMP.copyPixelsFromBuffer(ByteBuffer.wrap(newBmpByteArray));
            canvas.drawBitmap(newBMP, 0, 0, paint);

            //            paint.setTextSize(100);
            //            canvas.drawText("" + (int)(alpha * 1000)+ ", " + (int)(Common.angleY * 1000), 20, 200, paint);
        } catch (Exception e) {
            String a = e.toString();
        }
    }

}
