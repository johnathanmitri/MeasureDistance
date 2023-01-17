package com.johnathanmitri.measuredistance;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
//import com.johnathanmitri.measuredistance.databinding.ActivityMainBinding;
import com.johnathanmitri.measuredistance.databinding.ActivityMainBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //set app to be dark mode so all views behave properly
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);  //default value is true if this value does not exist
        if (isFirstRun)
        {
            //show the onboarding screen if this is the first run
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.fragment_view, OnboardingScreen1.class, null).commit();
        }
        else
        {
            //show the main screen if this is not the first run
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.fragment_view, MeasureFragment.class, null).commit();
        }

        setContentView(mainBinding.getRoot());
    }


    // Capture Volume key presses to freeze camera
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    if (event.getRepeatCount() == 0)  //only process this event once
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("freeze_preview"));
                    return true;
                }
            default:
        }
        return super.dispatchKeyEvent(event);
    }

    // If screen is touched, remove focus from the EditText.
    // EditText kept blinking even after another view is interacted with, so this is the fix.
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            //if the view with focus is an EditText
            if ( v instanceof EditText || v instanceof AppCompatEditText)
            {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                //if the click wasn't on that EditText
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                {
                    //remove focus and hide the keyboard
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public void onBackPressed()
    {

    }
}
