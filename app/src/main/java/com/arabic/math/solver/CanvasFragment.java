package com.arabic.math.solver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.arabic.math.solver.drawview.ClearDialogFragment;
import com.arabic.math.solver.drawview.DrawView;
import com.arabic.math.solver.drawview.DrawViewManager;
import com.arabic.math.solver.drawview.DrawViewModes;
import com.arabic.math.solver.retrofit.Classification;
import com.arabic.math.solver.retrofit.Retrofiter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.Calendar;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CanvasFragment extends Fragment implements ClearDialogFragment.ClearDialogListener {
    private View rootView;
    private DrawViewManager drawViewManager;
    private Locker locker;
    private String methodSelected;
    private  Transition internet_transition, solve_transition, scrimTransition;
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
//                    uploadFile(img_file);
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
        locker = new Locker();

        TextView pred_textview = rootView.findViewById(R.id.pred_textview);
        rootView.findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
        setOnClickMethods();
        classificationCallback = new Callback<Classification>() {
            @Override
            public void onResponse(@NonNull Call<Classification> call,
                                   @NonNull Response<Classification> response) {
                Resources res = getResourcesRef();
                Typeface myTypeface = Typeface.create(ResourcesCompat.getFont(requireContext(), R.font.math_arabic),
                        Typeface.NORMAL);
                int HtmlFlag = Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH;
                SpannableStringBuilder builder = new SpannableStringBuilder();
                Classification classRes = response.body();
                if (classRes != null && !classRes.containsNull()) {
                    builder = classRes.buildResponeStr(res, myTypeface, HtmlFlag);
                } else {
                    builder.append("\n").append(res.getString(R.string.error_str)).append(" : ").append(response.message());
                }
                pred_textview.setText(builder);
                HorizontalScrollView scroll = rootView.findViewById(R.id.pred_textview_scroll);
                scroll.post(() -> scroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
                rootView.findViewById(R.id.progress_bar).setVisibility(locker.unlock() ? View.INVISIBLE : View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<Classification> call, @NonNull Throwable t) {
                String pred_result = getResourcesRef().getString(R.string.error_str) + " : " + t.getMessage();
                pred_textview.setText(pred_result);
                rootView.findViewById(R.id.progress_bar).setVisibility(locker.unlock() ? View.INVISIBLE : View.VISIBLE);
            }
        };
        initTransitionUsed();
        initConnectivityObserver();
        initDrawView();
        initBottomTools();
        initBottomNavDrawer();
    }

    private void initTransitionUsed() {
        solve_transition = new Fade();
        internet_transition = new Slide();
        scrimTransition = new Fade();
        internet_transition.setDuration(500);
        internet_transition.addTarget(R.id.internet_textview);
        solve_transition.setDuration(500);
        solve_transition.addTarget(R.id.solve_fab);
        scrimTransition.setDuration(200);
        scrimTransition.addTarget(R.id.scrim);
    }

    private void initConnectivityObserver() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                requireActivity().runOnUiThread(() -> {
                    setNoInternetConnectivity(true);
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                requireActivity().runOnUiThread(() -> {
                    setNoInternetConnectivity(false);
                });
            }

        };
        ConnectivityManager connectivityManager = requireContext().getApplicationContext().getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);

        setNoInternetConnectivity(checkCurrentInternetConnection());
    }

    private boolean checkCurrentInternetConnection() {
        ConnectivityManager connectivityManager = requireContext().getApplicationContext().getSystemService(ConnectivityManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    private void setNoInternetConnectivity(boolean isConnected) {
        Log.e("internet" , String.valueOf(isConnected));
        TextView internet_textview = rootView.findViewById(R.id.internet_textview);
        FloatingActionButton solve = rootView.findViewById(R.id.solve_fab);
        TransitionSet set =  new TransitionSet();
        set.addTransition(internet_transition);
        set.addTransition(solve_transition);
        TransitionManager.beginDelayedTransition((ViewGroup) rootView, set);
        if (isConnected) {
            internet_textview.setVisibility(View.GONE);
            solve.setVisibility(View.VISIBLE);
        } else {
            internet_textview.setVisibility(View.VISIBLE);
            solve.setVisibility(View.GONE);
        }
    }

    private void initBottomNavDrawer() {
        NavigationView bottomNavDrawer = rootView.findViewById(R.id.bottom_methods_nav);
        BottomSheetBehavior<View> navBehavior = BottomSheetBehavior.from(rootView.findViewById(R.id.bottom_sheet_behavior_id));
        FloatingActionButton methodsFab = rootView.findViewById(R.id.methods_fab);
        View scrim = rootView.findViewById(R.id.scrim);

        navBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        MenuItem defaultItem = bottomNavDrawer.getMenu().getItem(0);
        defaultItem.setChecked(true);
        methodsFab.setImageDrawable(defaultItem.getIcon());
        methodSelected = defaultItem.getTitle().toString();
        bottomNavDrawer.setCheckedItem(defaultItem);

        bottomNavDrawer.setNavigationItemSelectedListener(item -> {
            methodSelected = item.getTitleCondensed().toString();
            item.setChecked(true);
            methodsFab.setImageDrawable(item.getIcon());
            navBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return true;
        });
        methodsFab.setOnClickListener(v -> {
            navBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });


        scrim.setOnClickListener(view -> {
            navBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        navBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                TransitionManager.beginDelayedTransition((ViewGroup) rootView, scrimTransition);
                scrim.setVisibility(newState == BottomSheetBehavior.STATE_HIDDEN ? View.INVISIBLE : View.VISIBLE);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }

        });

    }


    private void initBottomTools() {
        BottomNavigationView bottom_tools_nav = rootView.findViewById(R.id.bottom_tools_nav);
        bottom_tools_nav.setSelectedItemId(R.id.pen_tool);
        bottom_tools_nav.setOnItemSelectedListener(item -> {
            int item_id = item.getItemId();
            if (item_id == R.id.move_tool) {
                drawViewManager.setMode(DrawViewModes.MOVE);
                return true;
            } else if (item_id == R.id.eraser_tool) {
                drawViewManager.setMode(DrawViewModes.ERASE);
                return true;
            } else if (item_id == R.id.pen_tool) {
                drawViewManager.setMode(DrawViewModes.DRAW);
                return true;
            }
            return false;
        });
    }

    private void initDrawView() {
        DrawView paint = rootView.findViewById(R.id.draw_view);
        FloatingActionButton undo, redo;
        Button clearBtn = rootView.findViewById(R.id.clear_all_btn);
        undo = rootView.findViewById(R.id.undo_fab);
        redo = rootView.findViewById(R.id.redo_fab);
        this.drawViewManager = DrawViewManager.getInstance(paint).withClear(clearBtn).withRedoUndo(undo, redo);
        paint.setManager(drawViewManager);

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

    private void setOnClickMethods() {
        ImageButton gallery;
        FloatingActionButton undo, redo, solve, save;
        Button clearBtn = rootView.findViewById(R.id.clear_all_btn);
        undo = rootView.findViewById(R.id.undo_fab);
        redo = rootView.findViewById(R.id.redo_fab);
        solve = rootView.findViewById(R.id.solve_fab);
        gallery = rootView.findViewById(R.id.btn_gallery);
        save = rootView.findViewById(R.id.save_fab);

        clearBtn.setOnClickListener(v -> {
            DialogFragment dialog = new ClearDialogFragment(this);
            dialog.show(getParentFragmentManager(), "ClearDialogTag");
        });
        redo.setOnClickListener(view -> drawViewManager.redo());
        undo.setOnClickListener(view -> drawViewManager.undo());
        solve.setOnClickListener(view -> {
            if (!multiPermissionsCallback.checkPermissions(requireContext())) {
                multiPermissionsCallback.setCallback(result -> {
                    if (!result.containsValue(Boolean.FALSE)) {
                        uploadImage();
                    } else {
                        StringBuilder err_message = new StringBuilder("Permissions required were denied {");
                        for (Map.Entry<String, Boolean> i : result.entrySet())
                            if (!i.getValue())
                                err_message.append(i.getKey()).append(",");
                        err_message.append("}");
                        Toast.makeText(requireContext(), err_message.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestPermissionLauncher.launch(multiPermissionsCallback.getPermissionsNeeded());
            } else {
                uploadImage();
            }
        });
        //TODO delete this after debugging
        save.setOnClickListener(view -> {
            Bitmap imageToStore = drawViewManager.save();
            Imguru.storeImage(requireContext(), imageToStore);
            imageToStore.recycle();

        });
        gallery.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startForResultFromGallery.launch(intent);
        });
    }

    private void uploadImage() {
        rootView.findViewById(R.id.progress_bar).setVisibility(locker.lock() ? View.INVISIBLE : View.VISIBLE);
        String fileName = "IMG_" + DateFormat.format("yyyyMMdd_HH_mm_ss", Calendar.getInstance().getTime()) + ".png";
        Bitmap currImg = drawViewManager.save();
        byte[] bytes = Imguru.getByteArrayFromImage(currImg);
        Retrofiter.upload_classify(bytes, fileName, classificationCallback, methodSelected.toLowerCase());
        currImg.recycle();
    }

    private Resources getResourcesRef() {
        Configuration conf = requireContext().getResources().getConfiguration();
        Context localizedContext = requireContext().createConfigurationContext(conf);
        return localizedContext.getResources();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        TextView predTextView = rootView.findViewById(R.id.pred_textview);
        predTextView.setText(R.string.pred_textview_str);
        drawViewManager.deleteAll();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
}