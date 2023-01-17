package com.johnathanmitri.measuredistance;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.johnathanmitri.measuredistance.databinding.FragmentMeasureBinding;

import java.text.DecimalFormat;

public class MeasureFragment extends Fragment
{
    private FragmentMeasureBinding binding;

    private Preview preview;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;

    CameraSelector cameraSelector;

    private final String[] units = {"ft", "in", "m", "cm", "yd", "mi", "km"};
    private final double[] conversionFactors = { 1, 12, 0.3048, 30.48, 1.0/3, 1.0/5280, 0.0003048};
    //base unit is ft. ex: ft to inch = ft * 12;

    private int viewportWidth;
    private int viewportHeight;

    Drawable snowflakeFrozen;
    Drawable snowflakeSelector;

    private Bitmap freezeFrame;

    private float verticalCameraFOV;
    private double halfFovTangent;
    private double lastObjectSizePixels;

    private double objectHeightInUnits = 10;

    private boolean isFrozen = false;

    LevelView levelView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentMeasureBinding.inflate(inflater, container, false);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        viewportWidth = displayMetrics.widthPixels;
        viewportHeight = (int)(viewportWidth * (4.0/3.0));

        binding.frameLayout.getLayoutParams().height = viewportHeight;

        //runnable that runs when the layout is done. this way levelView can receive the proper width and height
        binding.getRoot().post(new Runnable()
        {
            @Override
            public void run() {
                    levelView = new LevelView(getContext(), binding.levelViewFrame.getWidth(), binding.levelViewFrame.getHeight());
                    levelView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    binding.levelViewFrame.addView(levelView);
            }
        });
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        objectHeightInUnits = Double.parseDouble(binding.objectHeightInput.getText().toString());

        snowflakeFrozen = ResourcesCompat.getDrawable(getResources(), R.drawable.snowflake_frozen, null);
        snowflakeSelector = ResourcesCompat.getDrawable(getResources(), R.drawable.freeze_button_selector, null);

        // Register volume click receiver from main
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                toggleFreeze();
            }
        }, new IntentFilter("freeze_preview"));

        // Freeze button on click freeze the preview.
        binding.cameraFreezeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view2)
            {
                toggleFreeze();
            }
        });

        // when the object height is changed, we must re calculate the distance
        binding.objectHeightInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() != 0)
                {
                    double parsedNum = Double.parseDouble(s.toString());
                    if (parsedNum > 0)
                    {
                        objectHeightInUnits = parsedNum;
                        calculateDistance();
                    }
                }
            }
        });

        // if we don't have camera permission request. If we do have permission, then set up the camera now
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
            {
                Toast.makeText(getContext(),"Permission is needed to access camera.", Toast.LENGTH_LONG).show();
            }
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
        else
            setUpCamera();

        // when one of the unit spinners is changed, we have to recalculate the distance
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                calculateDistance();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        };
        // set the listeners to the one just defined
        binding.inputUnitsSpinner.setOnItemSelectedListener(listener);
        binding.distanceUnitsSpinner.setOnItemSelectedListener(listener);

        // populate the spinners
        ArrayAdapter ad = new ArrayAdapter(getContext(), R.layout.small_spinner_item, units);
        ArrayAdapter adLarge = new ArrayAdapter(getContext(), R.layout.large_spinner_item, units);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adLarge.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.inputUnitsSpinner.setAdapter(ad);
        binding.distanceUnitsSpinner.setAdapter(adLarge);

        // create and add the Overlay View that the user interacts with
        CameraOverlayView cameraOverlayView = new CameraOverlayView(getContext(), this, viewportWidth, viewportHeight);
        cameraOverlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        binding.frameLayout.addView(cameraOverlayView);
    }

    private void toggleFreeze()
    {
        if (!isFrozen)
        {
            //binding.cameraFreezeButton.setImageResource(R.drawable.snowflake_frozen);
            binding.cameraFreezeButton.setImageDrawable(snowflakeFrozen);
            //stop the camera
            cameraProvider.unbind(preview);

            //Freeze levelView
            levelView.pause();

            //Bitmap to save the last frame before preview was frozen. Without it the last frame is lost when the app is paused
            freezeFrame = binding.viewFinder.getBitmap();
            //Hide the preview
            binding.viewFinder.setVisibility(View.GONE);

            //display the last frame
            binding.freezePreview.setImageBitmap(freezeFrame);
            binding.freezePreview.setVisibility(View.VISIBLE);
        }
        else
        {
            //Selector works with the "state_pressed", but mine is a toggle, not just a click. I kept it so that I could use the Focus state.
            //binding.cameraFreezeButton.setImageResource(R.drawable.freeze_button_selector);
            binding.cameraFreezeButton.setImageDrawable(snowflakeSelector);

            //hide the frozen frame
            binding.freezePreview.setVisibility(View.GONE);
            //show the preview
            binding.viewFinder.setVisibility(View.VISIBLE);
            //restart the camera
            camera = cameraProvider.bindToLifecycle(getActivity(), cameraSelector, preview);

            //Resume levelView
            levelView.resume();
        }
        isFrozen = !isFrozen;
    }


    public void objectResized(int topLinePos, int botLinePos)
    {
        //save the Last Size received from CameraOverlayView, so that we can re-calculate distance if units or real object height are changed.
        lastObjectSizePixels = botLinePos - topLinePos;
        //re-calculate the distance
        calculateDistance();
    }

    /*  -----Calculate Distance Formula
    - theta: Camera FOV
    - h: represents the full height that can be seen with the camera at the given distance
    - s: height (size) of the object
    - d: distance from the object

    Explanation:

    -The ratio of the Full Screen to the Object on screen can be calculated using pixels
       -example ratio:  1920 : 960  =>  Therefore, h = (1920/960) * s.

    -If the object takes up half the screen, then h is twice the size of the object.

    -tan(theta/2) can be considered constant (unless zooming in)

    -(h/2) / d = tan(theta/2)

    -Therefore, d = (h/2)/tan(theta/2), where h = (physical height of object) * (total screen height) / (object height on screen)

    This requires the camera to be perpendicular to the object. If the camera is not perpendicular it will be inaccurate.

     */
    private void calculateDistance()
    {
        // Calculate the maximum size object the camera can see at this distance.
        // This would be the same as the height of the object if that object filled up the entire screen.
        double cameraViewHeightInUnits = objectHeightInUnits * ((double)viewportHeight / lastObjectSizePixels);

        // Distance from the object in the same units as the height
        double distance = (cameraViewHeightInUnits / 2) / halfFovTangent;

        int unit1 = binding.inputUnitsSpinner.getSelectedItemPosition();    //value is currently in this unit
        int unit2 = binding.distanceUnitsSpinner.getSelectedItemPosition();

        if (unit1 != unit2) // divide the value by unit1, and multiply by unit2
            distance = (distance / conversionFactors[unit1]) * conversionFactors[unit2];

        // display the distance on screen rounded to two decimal places
        binding.distanceText.setText(new DecimalFormat("#.##").format(distance));
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted)
                {
                    // if the user gives permission, then we open camera and start calculations
                    setUpCamera();
                    calculateDistance();
                }
                else
                {
                    Toast.makeText(getContext(),"Permission was denied. Camera can't be accessed without permission. ", Toast.LENGTH_LONG).show();
                }
            });

    private void setUpCamera()
    {
        android.hardware.Camera camera = android.hardware.Camera.open();

        if (camera == null)
        {
            Toast.makeText(getContext(),"No back camera available. Please use a device with a back camera.", Toast.LENGTH_LONG).show();
            return;
        }

        verticalCameraFOV = camera.getParameters().getHorizontalViewAngle(); //"horizontal" as if camera were landscape.
        camera.release();

        //Tangent of half the angle. This is equal to Opp/Adj, which is (h/2) / distance
        halfFovTangent = Math.tan(Math.toRadians(verticalCameraFOV/2));

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(new Runnable()
        {
            public void run()
            {
                try
                {
                    cameraProvider = cameraProviderFuture.get();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                bindCameraUseCases();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases()
    {
        int screenAspectRatio = AspectRatio.RATIO_4_3;  //4:3 is the standard camera aspect ratio

        int rotation = Surface.ROTATION_0;

        cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        Preview.Builder previewBuilder = new Preview.Builder();

        previewBuilder.setTargetAspectRatio(screenAspectRatio);
        previewBuilder.setTargetRotation(rotation);

        // Disable Video stabilization and Lens stabilizition. These crop the preview when they are on, which messes up the calculation
        Camera2Interop.Extender camera2InterOp = new Camera2Interop.Extender(previewBuilder);
        camera2InterOp.setCaptureRequestOption(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
        camera2InterOp.setCaptureRequestOption(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);

        preview = previewBuilder.build();

        cameraProvider.unbindAll();

        try
        {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(),"Camera binding failed.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // resume levelView only if its initialized and the preview is not frozen.
        // if preview is frozen then it should remain paused
        if (levelView != null && !isFrozen)
            levelView.resume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        //pause levelView on pause so we dont waste power
        if (levelView != null && !isFrozen)
            levelView.pause();

    }
}