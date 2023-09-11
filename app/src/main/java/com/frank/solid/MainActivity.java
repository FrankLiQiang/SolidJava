package com.frank.solid;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static Uri _uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        // 设置页面全屏 刘海屏 显示
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        window.setAttributes(lp);
        final View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        hideNavigationBar();
        // 设置页面全屏 刘海屏 显示
        setContentView(R.layout.activity_main);
        findViewById(R.id.s4).setOnClickListener(this);
        findViewById(R.id.s6).setOnClickListener(this);
        findViewById(R.id.s60).setOnClickListener(this);
        findViewById(R.id.s8).setOnClickListener(this);
        findViewById(R.id.s12).setOnClickListener(this);
        findViewById(R.id.s120).setOnClickListener(this);
        findViewById(R.id.s20).setOnClickListener(this);
        findViewById(R.id.earth).setOnClickListener(this);
        findViewById(R.id.ball).setOnClickListener(this);
        findViewById(R.id.pCube).setOnClickListener(this);
        findViewById(R.id.pCube6).setOnClickListener(this);
        findViewById(R.id.ColorCube).setOnClickListener(this);
        findViewById(R.id.ring0).setOnClickListener(this);
        findViewById(R.id.pRing).setOnClickListener(this);
        findViewById(R.id.mobiusRing).setOnClickListener(this);
        findViewById(R.id.forBackup).setOnClickListener(this);
    }

    private void hideNavigationBar() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        System.exit(0);
    }

//    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ball)
                .setItems(R.array.rotateNum, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
        return builder.create();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forBackup: {
                _uri = null;
                Intent intent = new Intent(this, BallActivity.class);
                startActivity(intent);

                break;
            }
            case R.id.ring0: {
                Intent intent = new Intent(this, RingActivity.class);
                startActivity(intent);

                 break;
            }
            case R.id.mobiusRing: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.rotateNum)
                        .setItems(R.array.rotateNum, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, MobiusActivity.class);
                                intent.putExtra("rotateNum", which);
                                startActivity(intent);
                            }
                        });
                builder.create().show();
                break;
            }
            case R.id.s4: {
                Intent intent = new Intent(this, TetrahedronActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.s6: {
                Intent intent = new Intent(this, CubeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.s60: {
                Intent intent = new Intent(this, Cube0Activity.class);
                startActivity(intent);
                break;
            }
            case R.id.s8: {
                Intent intent = new Intent(this, OctahedronActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.s12: {
                Intent intent = new Intent(this, DodecahedronActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.s120: {
                Intent intent = new Intent(this, Dodecahedron0Activity.class);
                startActivity(intent);
                break;
            }
            case R.id.s20: {
                Intent intent = new Intent(this, IcosahedronActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.earth: {
                Intent intent = new Intent(this, EarthActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.ball: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent, 1);
                } catch (Exception ex) {
                }
                break;
            }
            case R.id.pRing: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent, 3);
                } catch (Exception ex) {
                }
                break;
            }
            case R.id.ColorCube: {
                Intent intent = new Intent(this, ColorCubeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.pCube: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent, 2);
                } catch (Exception ex) {
                }
                break;
            }
            case R.id.pCube6: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent, 6);
                } catch (Exception ex) {
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        _uri = data.getData();
        if (_uri == null || _uri.getPath() == null) return;

        if (requestCode == 1) {
            Intent intent = new Intent(this, BallActivity.class);
            startActivity(intent);
        }
        if (requestCode == 2) {
            Intent intent = new Intent(this, PictCubeActivity.class);
            startActivity(intent);
        }
        if (requestCode == 3) {
            Intent intent = new Intent(this, PictureRingActivity.class);
            startActivity(intent);
        }
    }
}

