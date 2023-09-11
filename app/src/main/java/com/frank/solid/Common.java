package com.frank.solid;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.FileDescriptor;
import java.io.IOException;

public class Common {

    static {
        System.loadLibrary("native-lib");
    }

    public static native void setEYE(float eyeX, float eyeY, float eyeZ);

    public static native int isVisible(float xE, float yE, float zE,
                                       float xF, float yF, float zF,
                                       float xG, float yG, float zG);

    public static double   PI  = 3.14159;
    public static int      _screenWidth;
    public static int      _screenHeight;
    public static PointXYZ Eye = new PointXYZ();
    public static float    firstDistance, DepthZ, StartX, StartY, EndX, EndY;
    public static boolean  isSingle  = true;
    public static PointXYZ ObjCenter = new PointXYZ();
    public static double   square;
    public static float distance;
    public static double angleY = 0;

    public static void getScreenSize(WindowManager windowManager) {
        Point outSize = new Point();
        windowManager.getDefaultDisplay().getRealSize(outSize);
        _screenWidth = outSize.x;
        _screenHeight = outSize.y;
    }

    public static void resetPointF(PointF thisP, PointXYZ P) {
        thisP.x = (P.x - Eye.x) / (Eye.z - P.z) * (Eye.z - DepthZ) + Eye.x;
        thisP.y = (P.y - Eye.y) / (Eye.z - P.z) * (Eye.z - DepthZ) + Eye.y;
    }

    public static double getDistance(float x1, float y1, float x2, float y2) {
        square = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
        return Math.sqrt(square);
    }

    public static void getNewSizePoint(PointXYZ pOld, PointXYZ pNew) {
        getNewSizePoint(pOld, pNew, true);
    }

    public static void getNewSizePoint(PointXYZ pOld, PointXYZ pNew, boolean isChange) {
        pNew.reset(pOld.x - ObjCenter.x, pOld.y - ObjCenter.y, pOld.z - ObjCenter.z);

        distance = (float) Math.sqrt((ObjCenter.x - EndX) * (ObjCenter.x - EndX) + (ObjCenter.y - EndY) * (ObjCenter.y - EndY));
        distance = distance / firstDistance;
        if(isChange) {
            pNew.reset(pNew.x * distance, pNew.y * distance, pNew.z * distance);
        }

        getDistance(StartX, StartY, EndX, EndY);
        double a2 = square;
        double b = getDistance(ObjCenter.x, ObjCenter.y, StartX, StartY);
        double b2 = square;
        double c = getDistance(ObjCenter.x, ObjCenter.y, EndX, EndY);
        double c2 = square;

        double Acos = (b2 + c2 - a2) / (2 * b * c);
        double angleA;
        if (Math.abs(Acos) > 1) {
            angleA = 0;
        } else {
            angleA = Math.acos(Acos);
        }
        int dir = getDir(ObjCenter.x, ObjCenter.y, StartX, StartY, EndX, EndY);
        angleA *= dir;
        double x = pNew.x * Math.cos(angleA) - pNew.y * Math.sin(angleA);
        double y = pNew.y * Math.cos(angleA) + pNew.x * Math.sin(angleA);
        pNew.reset((float) x, (float) y, pNew.z);
        pNew.reset(pNew.x + ObjCenter.x, pNew.y + ObjCenter.y, pNew.z + ObjCenter.z);
    }

    public static int getDir(float x1, float y1, float x2, float y2, float x3, float y3) {
        return (x1 * y2 + x2 * y3 + x3 * y1 - x1 * y3 - x2 * y1 - x3 * y2) > 0 ? 1 : -1;
    }

    public static void getNewPoint(PointXYZ pOld, PointXYZ pNew) {
        pNew.reset(pOld.x - ObjCenter.x, pOld.y - ObjCenter.y, pOld.z - ObjCenter.z);

        double a2 = (StartX - EndX) * (StartX - EndX);
        double b = getDistance(ObjCenter.x, ObjCenter.z, StartX, DepthZ);
        double b2 = square;
        double c = getDistance(ObjCenter.x, ObjCenter.z, EndX, DepthZ);
        double c2 = square;

        double Acos = (b2 + c2 - a2) / (2 * b * c);
        double angleA;
        if (Math.abs(Acos) > 1) {
            angleA = 0;
        } else {
            angleA = Math.acos(Acos) * 2;
        }
        if (EndX > StartX) angleA *= -1;
        angleY = angleA;
        double x = pNew.x * Math.cos(angleA) - pNew.z * Math.sin(angleA);
        double z = pNew.z * Math.cos(angleA) + pNew.x * Math.sin(angleA);
        pNew.reset((float) x, pNew.y, (float) z);

        //---------
        a2 = (StartY - EndY) * (StartY - EndY);
        b = getDistance(ObjCenter.y, ObjCenter.z, StartY, DepthZ);
        b2 = square;
        c = getDistance(ObjCenter.y, ObjCenter.z, EndY, DepthZ);
        c2 = square;

        Acos = (b2 + c2 - a2) / (2 * b * c);
        if (Math.abs(Acos) > 1) {
            angleA = 0;
        } else {
            angleA = Math.acos(Acos) * 2;
        }
        if (EndY > StartY) angleA *= -1;
        double y = pNew.y * Math.cos(angleA) - pNew.z * Math.sin(angleA);
        z = pNew.z * Math.cos(angleA) + pNew.y * Math.sin(angleA);

        pNew.reset(pNew.x, (float) y, (float) z);

        pNew.reset(pNew.x + ObjCenter.x, pNew.y + ObjCenter.y, pNew.z + ObjCenter.z);
    }

    public static class PointXYZ {
        public float x, y, z;

        public PointXYZ() {
        }

        public PointXYZ(float X, float Y, float Z) {
            x = X;
            y = Y;
            z = Z;
        }

        void reset(float X, float Y, float Z) {
            x = X;
            y = Y;
            z = Z;
        }
    }

    public static class PointF {
        public float x, y;

        PointF() {
        }
    }

    static public short getExifOrientation(Uri uri, ContentResolver resolver) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(fileDescriptor);
        } catch (IOException var4) {
            String a = var4.toString();
            a += "";
        }
        short degree = 0;

        if (exif != null) {
            int orientation = exif.getAttributeInt("Orientation", -1);
            if (orientation != -1) {
                switch (orientation) {
                    case 3:
                        degree = 180;
                    case 4:
                    case 5:
                    case 7:
                    default:
                        break;
                    case 6:
                        degree = 90;
                        break;
                    case 8:
                        degree = 270;
                }
            }
        }
        parcelFileDescriptor.close();

        return degree;
    }

    static public Bitmap getBitmapFromUri(Uri uri, ContentResolver resolver) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();

        int degrees = getExifOrientation(uri, resolver);
        if (degrees != 0) {
            Matrix m = new Matrix();
            m.setRotate(degrees);
            try {
                Bitmap b2 = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);
                if (image != b2) {
                    image.recycle();
                    image = b2;
                }
            } catch (OutOfMemoryError var5) {
            }
        }
        return image;
    }

    public static class LayoutSettingView extends View {
        private Paint _paint = new Paint();

        Context cc;
        public LayoutSettingView(Context c) {
            super(c);
            cc = c;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    Common.StartX = event.getX();
                    Common.StartY = event.getY();
                    if(cc instanceof EarthActivity) {
                        ((EarthActivity)cc).saveOldPoints();
                    }
                    if(cc instanceof RingActivity) {
                        ((RingActivity)cc).saveOldPoints();
                    }
                    if(cc instanceof MobiusActivity) {
                        ((MobiusActivity)cc).saveOldPoints();
                    }
                    if(cc instanceof BeltActivity) {
                        ((BeltActivity)cc).saveOldPoints();
                    }
                    if(cc instanceof SolidActivity) {
                        ((SolidActivity)cc).saveOldPoints();
                    }
                    if(cc instanceof BallActivity) {
                        ((BallActivity)cc).saveOldPoints();
                    }
                    if(cc instanceof PictureRingActivity) {
                        ((PictureRingActivity)cc).saveOldPoints();
                    }

                    Common.isSingle = event.getPointerCount() == 1;
                    Common.firstDistance =
                            (float) Math.sqrt((Common.ObjCenter.x - Common.StartX) * (Common.ObjCenter.x - Common.StartX) + (Common.ObjCenter.y - Common.StartY) * (Common.ObjCenter.y - Common.StartY));
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    Common.EndX = event.getX();
                    Common.EndY = event.getY();
                    if (event.getPointerCount() == 2) {
                        Common.isSingle = false;
                        if(cc instanceof EarthActivity) {
                            ((EarthActivity)cc).convert2();
                        }
                        if(cc instanceof RingActivity) {
                            ((RingActivity)cc).convert2();
                        }
                        if(cc instanceof MobiusActivity) {
                            ((MobiusActivity)cc).convert2();
                        }
                        if(cc instanceof BeltActivity) {
                            ((BeltActivity)cc).convert2();
                        }
                        if(cc instanceof SolidActivity) {
                            ((SolidActivity)cc).convert2();
                        }
                        if(cc instanceof BallActivity) {
                            ((BallActivity)cc).convert2();
                        }
                        if(cc instanceof PictureRingActivity) {
                            ((PictureRingActivity)cc).convert2();
                        }
                        invalidate();
                    } else if (event.getPointerCount() == 1 && Common.isSingle) {
                        if(cc instanceof RingActivity) {
                            ((RingActivity)cc).convert();
                        }
                        if(cc instanceof MobiusActivity) {
                            ((MobiusActivity)cc).convert();
                        }
                        if(cc instanceof BeltActivity) {
                            ((BeltActivity)cc).convert();
                        }
                        if(cc instanceof EarthActivity) {
                            ((EarthActivity)cc).convert();
                        }
                        if(cc instanceof SolidActivity) {
                            ((SolidActivity)cc).convert();
                        }
                        if(cc instanceof BallActivity) {
                            ((BallActivity)cc).convert();
                        }
                        if(cc instanceof PictureRingActivity) {
                            ((PictureRingActivity)cc).convert();
                        }
                        invalidate();
                    }
                    break;
                }
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = _paint;
            canvas.drawColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);

            if(cc instanceof SolidActivity) {
                ((SolidActivity)cc).drawSolid(canvas, paint);
            } else if(cc instanceof EarthActivity) {
                ((EarthActivity)cc).drawEarth(canvas, paint);
            } else if(cc instanceof BallActivity) {
                ((BallActivity)cc).drawPicBall(canvas, paint);
            } else if(cc instanceof PictureRingActivity) {
                ((PictureRingActivity)cc).drawPicRing(canvas, paint);
            } else if(cc instanceof RingActivity) {
                ((RingActivity)cc).drawRing(canvas, paint);
            } else if(cc instanceof MobiusActivity) {
                ((MobiusActivity)cc).drawRing(canvas, paint);
            } else if(cc instanceof BeltActivity) {
                ((BeltActivity)cc).drawRing(canvas, paint);
            }
        }
    }
}
