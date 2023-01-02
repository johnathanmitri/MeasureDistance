package com.johnathanmitri.measuredistance;

import static android.content.ContentValues.TAG;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.johnathanmitri.measuredistance.databinding.FragmentFirstBinding;

import java.util.concurrent.ExecutionException;

public class FirstFragment extends Fragment
{

    private FragmentFirstBinding binding;

    private Preview preview;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        /*binding.buttonFirst.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });*/

        android.hardware.Camera camera = android.hardware.Camera.open();
        float[] fov = new float[2];
        fov[0] = camera.getParameters()
                .getHorizontalViewAngle();
        fov[1] = camera.getParameters()
                .getVerticalViewAngle();
        camera.release();

        Toast.makeText(getContext(), "Horizontal FOV: " + fov[0] + "  Vert FOV: " + fov[1], 5).show();

      //  Snackbar.make(view, "Horizontal FOV: " + fov[0] + "  Vert FOV: " + fov[1], Snackbar.LENGTH_LONG).setAction("Action", null).show();


        setUpCamera();


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

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

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

        if (camera != null) {
            // Must remove observers from the previous camera instance

//TODO: FIX THIS!!!
    //        removeCameraStateObservers(camera!!.cameraInfo)
        }

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview);

            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

//TODO: FIX THIS!!!
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

}