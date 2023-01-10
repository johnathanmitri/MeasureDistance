package com.johnathanmitri.measuredistance;

import static android.content.ContentValues.TAG;

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
import android.util.Log;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.johnathanmitri.measuredistance.databinding.FragmentFirstBinding;

import java.text.DecimalFormat;

public class MeasureFragment extends Fragment
{

    private FragmentFirstBinding binding;

    private Preview preview;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;

    private int viewportWidth;
    private int viewportHeight;

    Drawable snowflakeFrozen;
    Drawable snowflakeSelector;

    private float verticalCameraFOV;
    private double halfFovTangent;
    private double lastObjectSizePixels;

    private double objectHeightInUnits = 6;

    private boolean isFrozen = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentFirstBinding.inflate(inflater, container, false);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        viewportWidth = displayMetrics.widthPixels;
        viewportHeight = (int)(viewportWidth * (4.0/3.0));

        binding.frameLayout.getLayoutParams().height = viewportHeight;

        return binding.getRoot();
    }

    private BroadcastReceiver volumeClickReciever = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals("freeze_preview"))
            {
                toggleFreeze();
            }
        }
    };


    public void toggleFreeze()  //public because MainActivity needs to access it for volume button click.
    {
        if (!isFrozen)
        {
            //binding.cameraFreezeButton.setImageResource(R.drawable.snowflake_frozen);
            binding.cameraFreezeButton.setImageDrawable(snowflakeFrozen);
            cameraProvider.unbind(preview);


            Bitmap freezeFrame = binding.viewFinder.getBitmap();
            binding.viewFinder.setVisibility(View.GONE);

            binding.freezePreview.setImageBitmap(freezeFrame);
            binding.freezePreview.setVisibility(View.VISIBLE);
        }
        else
        {
            //Selector works with the "state_pressed", but mine is a toggle, not just a click. I kept it so that I could use the Focus state.
            //binding.cameraFreezeButton.setImageResource(R.drawable.freeze_button_selector);
            binding.cameraFreezeButton.setImageDrawable(snowflakeSelector);

            binding.freezePreview.setVisibility(View.GONE);
            binding.viewFinder.setVisibility(View.VISIBLE);

            camera = cameraProvider.bindToLifecycle(getActivity(), cameraSelector, preview);
        }
        isFrozen = !isFrozen;
    }

    public void objectResized(int topLinePos, int botLinePos) {
        lastObjectSizePixels = botLinePos - topLinePos;
        calculateDistance();
    }
    public void calculateDistance()
    {
        //Calculate the maximum size object the camera can see at this distance. This would be the same as the height of the object if that object filled up the entire screen.
        double cameraViewHeightInUnits = objectHeightInUnits * ((double)viewportHeight / lastObjectSizePixels);

        //double tanValue = Math.tan(Math.toRadians(verticalCameraFOV / 2));  //Tangent of half the angle. This is equal to Opp/Adj, which is (h/2) / distance

        double distance = (cameraViewHeightInUnits / 2) / halfFovTangent;

        String units = "ft";
        binding.distanceText.setText("Distance: " +  new DecimalFormat("#.##").format(distance) + units);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted)
                {
                    setUpCamera();
                }
                else
                {
                    Toast.makeText(getContext(),"Permission was denied. Camera can't be accessed without permission. ", Toast.LENGTH_LONG).show();
                }
            });

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        snowflakeFrozen = ResourcesCompat.getDrawable(getResources(), R.drawable.snowflake_frozen, null);
        snowflakeSelector = ResourcesCompat.getDrawable(getResources(), R.drawable.freeze_button_selector, null);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(volumeClickReciever, new IntentFilter("freeze_preview"));

        binding.cameraFreezeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view2)
            {
                toggleFreeze();
            }
        });

        binding.objectHeightInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s)
            {

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }
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

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
            {
                //TODO: EXPLAIN
               // Snackbar.make(getView(), "Permission is needed to access camera.", 4);
                Toast.makeText(getContext(),"Permission is needed to access camera.", Toast.LENGTH_LONG).show();
            }
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
        else
            setUpCamera();


        CameraOverlayView cameraOverlayView = new CameraOverlayView(getContext(), this, viewportWidth, viewportHeight);
        cameraOverlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        binding.frameLayout.addView(cameraOverlayView);


        /*GLSurfaceView overlayView = new CameraOverlayView(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        binding.frameLayout.addView(overlayView);
*/


    }

    private float[] calculateFOV(CameraManager cManager) {
        try {
            for ( final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics. get(CameraCharacteristics. LENS_FACING );
                if (cOrientation == CameraCharacteristics. LENS_FACING_BACK )
                {
                    float [] maxFocus = characteristics. get(CameraCharacteristics. LENS_INFO_AVAILABLE_FOCAL_LENGTHS );
                    SizeF size = characteristics.get(CameraCharacteristics. SENSOR_INFO_PHYSICAL_SIZE );
                    float w = size. getWidth();
                    float h = size. getHeight();

                    float[] result = new float[2];
                    result[0] = ( float ) ( 2 *Math. atan (w/(maxFocus[ 0 ]* 2 )));
                    result[1] = ( float ) ( 2 *Math. atan (h/(maxFocus[ 0 ]* 2 )));
                    return result;
                }
            }
        } catch (CameraAccessException e) { e.
                printStackTrace();

        }
        return null;
    }

    private void setUpCamera()
    {
        android.hardware.Camera camera = android.hardware.Camera.open();
        verticalCameraFOV = camera.getParameters().getHorizontalViewAngle(); //"horizontal" if camera were landscape. app only runs portrait.
        camera.release();

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
        int screenAspectRatio = AspectRatio.RATIO_4_3;  //every camera shoots a maximum of 4:3 aspect ratio i hope

        int rotation = Surface.ROTATION_0;

        cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        Preview.Builder previewBuilder = new Preview.Builder();

        previewBuilder.setTargetAspectRatio(screenAspectRatio);
        previewBuilder.setTargetRotation(rotation);

        Camera2Interop.Extender camera2InterOp = new Camera2Interop.Extender(previewBuilder);
        //@androidx.camera.camera2.interop.ExperimentalCamera2Interop

        camera2InterOp.setCaptureRequestOption(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);

        // @androidx.camera.camera2.interop.ExperimentalCamera2Interop
        camera2InterOp.setCaptureRequestOption(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);

        preview = previewBuilder.build();

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll();

        /*if (camera != null)
        {
           removeCameraStateObservers(camera.cameraInfo);
        }*/

        try
        {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

//TODO: FIX THIS ?
            //observeCameraState(camera?.cameraInfo!!)
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
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

        //levelView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        //levelView.onPause();

    }


}