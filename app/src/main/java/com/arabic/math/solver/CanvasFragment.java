package com.arabic.math.solver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.RangeSlider;

import java.io.File;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CanvasFragment extends Fragment {
    private DrawView paint;
    private RangeSlider rangeSlider;
    private View rootView;

    PermissionHandler<Map<String, Boolean>> multiPermissionsCallback = new PermissionHandler<>();
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), multiPermissionsCallback);

    Callback<Classification> classificationCallback;
    private final ActivityResultLauncher<Intent> startForResultFromGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            try {
                if (result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    File img_file = new File(Imguru.getPathFromUri(requireContext(), selectedImageUri));
                    uploadFile(img_file);
                }
            } catch (Exception exception) {
                Log.d("External storage error:", exception.getLocalizedMessage());
            }
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_canvas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View mRootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        this.rootView = mRootView;
        paint = rootView.findViewById(R.id.draw_view);
        rangeSlider = rootView.findViewById(R.id.rangebar);
        TextView pred_textview = rootView.findViewById(R.id.pred_textview);

        setOnClickMethods();
        classificationCallback = new Callback<Classification>() {
            @Override
            public void onResponse(@NonNull Call<Classification> call,
                                   @NonNull Response<Classification> response) {
                String pred_result = "Equation : " + response.body().getEquation() + "\nMapping : " + response.body().getMapping() + "\nSolution : " + response.body().getSolution();
                pred_textview.setText(pred_result);
            }

            @Override
            public void onFailure(@NonNull Call<Classification> call, @NonNull Throwable t) {
                Log.e("Upload error:", t.getMessage());
                Toast.makeText(requireContext(),"Upload error:"+ t.getMessage(),Toast.LENGTH_LONG).show();
                pred_textview.setText(R.string.pred_textview_str);
            }
        };
        //pass the height and width of the custom view to the init method of the DrawView object
        ViewTreeObserver vto = paint.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                paint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = paint.getMeasuredWidth();
                int height = paint.getMeasuredHeight();
                paint.init(height, width);
            }
        });

    }

    private void uploadFile(File file) {
        Retrofiter.upload_classify(file, classificationCallback);
    }

    private void setOnClickMethods() {
        ImageButton save, color, stroke, undo;
        undo = rootView.findViewById(R.id.btn_undo);
        save = rootView.findViewById(R.id.btn_save);
        color = rootView.findViewById(R.id.btn_color);
        stroke = rootView.findViewById(R.id.btn_stroke);

        undo.setOnClickListener(view -> paint.undo());
        save.setOnClickListener(view -> {
            String[] permission_needed;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                permission_needed = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET
                };
            }else {
                permission_needed = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET
                };
            }

            if (!PermissionHandler.checkPermissions(permission_needed, requireContext())) {
                multiPermissionsCallback.setCallback(result -> {
                    if (!result.containsValue(Boolean.FALSE)) {
                        File imageFile = Imguru.storeImage(requireContext(), paint.save());
                        uploadFile(imageFile);
                    } else {
                        StringBuilder err_message = new StringBuilder("Permissions required were denied {");
                        for (Map.Entry<String, Boolean> i : result.entrySet())
                            if (!i.getValue())
                                err_message.append(i.getKey()).append(",");
                        err_message.append("}");
                        Toast.makeText(requireContext(), err_message.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestPermissionLauncher.launch(permission_needed);
            } else {
                File imageFile = Imguru.storeImage(requireContext(), paint.save());
                uploadFile(imageFile);
            }
        });
        color.setOnClickListener(view -> {
//            final ColorPicker colorPicker = new ColorPicker(requireActivity());
//            colorPicker.setColumns(5)
//                    .setDefaultColorButton(paint.getCurrentColor())
//                    .setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
//                        @Override
//                        public void setOnFastChooseColorListener(int position, int color) {
//                            paint.setColor(color);
//                        }
//
//                        @Override
//                        public void onCancel() {
//                            colorPicker.dismissDialog();
//                        }
//                    }).show();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startForResultFromGallery.launch(intent);
        });
        stroke.setOnClickListener(view -> {
            rangeSlider.setVisibility(rangeSlider.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        rangeSlider.setValueFrom(0.0f);
        rangeSlider.setValueTo(100.0f);
        rangeSlider.addOnChangeListener((slider, value, fromUser) -> paint.setStrokeWidth((int) value));
    }
}