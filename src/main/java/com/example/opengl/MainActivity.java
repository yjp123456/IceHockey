package com.example.opengl;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private AirHockeyRender airHockeyRender;
    private boolean renderSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);
        airHockeyRender = new AirHockeyRender(this);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        if (supportEs2) {
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setRenderer(airHockeyRender);
            renderSet = true;
        } else
            Toast.makeText(this, "not support egl 2.0", Toast.LENGTH_LONG);

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent != null) {
                    //把坐标限制在[-1,1]
                    final float normalizedX = (motionEvent.getX() / (float) view.getWidth()) * 2 - 1;
                    final float normalizedY = -((motionEvent.getY() / (float) view.getHeight()) * 2 - 1);
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                airHockeyRender.handleTouchPress(normalizedX, normalizedY);
                            }
                        });
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                airHockeyRender.handleTouchDrag(normalizedX, normalizedY);
                            }
                        });
                    }
                    return true;
                } else
                    return false;
            }
        });
        setContentView(glSurfaceView);
        /*new Thread() {
            public void run() {
                int i = 1;
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                String providerClassName;

                while ((providerClassName = Security.getProperty("security.provider." + i++)) != null) {
                    try {
                        Class providerClass = Class.forName(providerClassName.trim(), true, cl);
                        Provider p = (Provider) providerClass.newInstance();
                        for (Provider.Service service : p.getServices()) {
                            String type = service.getType();
                            if (type.contains("CertPathValidator")) {
                                Provider provider = Security.getProvider("BC");
                                Provider.Service myService = provider.getService("CertPathValidator", "PKIX");
                                Object s = myService.newInstance(null);
                                Log.d("test", type);
                            }

                        }
                        Log.d("test", "test");
                    } catch (ClassNotFoundException ignored) {
                    } catch (IllegalAccessException ignored) {
                    } catch (InstantiationException ignored) {
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();*/


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (renderSet)
            glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (renderSet)
            glSurfaceView.onResume();
    }
}
