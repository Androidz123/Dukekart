package com.dukeKart.android;

import static com.dukeKart.android.common.AppUrls.AppVersion;
import static com.dukeKart.android.common.AppUtils.getAppVersion;
import static com.dukeKart.android.common.AppUtils.getDays;
import static com.dukeKart.android.common.AppUtils.readTimeStampDate;
import static com.dukeKart.android.common.AppUtils.showAlertDialog;
import static com.dukeKart.android.common.AppUtils.showToast;
import static com.dukeKart.android.common.AppUtils.updateAlertDialog;
import static com.dukeKart.android.common.AppsStrings.apiErrorBody;
import static com.dukeKart.android.common.AppsStrings.apiErrorCode;
import static com.dukeKart.android.common.AppsStrings.apiErrorDetail;
import static com.dukeKart.android.common.AppsStrings.apiErrorException;
import static com.dukeKart.android.common.AppsStrings.setApiString;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.dukeKart.android.activity.Dashboard;
import com.dukeKart.android.views.BaseActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Splash extends BaseActivity {
    private static final int permission_granted_code = 100;
    private static final String[] appPermission = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE};
    private static final String[] appPermissionTiramisu = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE};
    private String currentVersion = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Button crashButton = new Button(this);
        crashButton.setText("Crash!");
        crashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                throw new RuntimeException("Test Crash"); // Force a crash
            }
        });

        if (getPermission()) {
            setInitals();

        }
    }

    private void setInitals() {
        currentVersion = getAppVersion(mActivity);
        getAppVersionApi();
    }

    private void getAppVersionApi() {
        setApiString("AppVersion");
        String url = AppVersion;
        // showLoading();
        Log.d("testApi", url);
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        parseAppVersionApi(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        // hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            //showToast(mActivity, defResponse);
                            Log.v(apiErrorCode, "Request : " + error.getErrorCode());
                            Log.v(apiErrorBody, "Request : " + error.getErrorBody());
                            Log.v(apiErrorDetail, "Request : " + error.getErrorDetail());
                        } else {
                            // showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    private void parseAppVersionApi(JSONObject response) {
        Log.d("apitestApi", response.toString());
        // hideLoading();
        try {
            if (response.has("paymentStatus")) {

//                FirebaseApp.initializeApp(this);
                String payment_Status = response.getString("paymentStatus");
                String version = response.getString("version");
                String message = response.getString("userMessage");
                String validDate = readTimeStampDate(response.getString("dateOfValidation"));
                Log.d("apitrail", getDays(validDate) + "work");
                if (payment_Status.equalsIgnoreCase("Trail")) {

                    if (getDays(validDate) > 0) {
                        // show validation message

                        updateAlertDialog(mActivity, getString(R.string.trailVersionExpire),
                                getString(R.string.trailVersionExpireMessage) + ": " + validDate + " " + message, (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    finish();
                                });
                    } else {
                        proceedToActivity(version);
                    }
                } else if (payment_Status.equalsIgnoreCase("Paid")) {
                    proceedToActivity(version);
                } else {
                    // show blocked message
                    updateAlertDialog(mActivity, getString(R.string.applicationBlocked),
                            getString(R.string.trailVersionExpireMessage) + ": " + validDate + " " + message, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                finish();
                            });
                }
            } else {

                // show error message
                showToast(this,"has payment status");
                updateAlertDialog(mActivity, getString(R.string.applicationError),
                        getString(R.string.contactYourServiceProvider), (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            finish();
                        });
            }

        } catch (Exception e) {
            showToast(this,"catch me "+e.getMessage() );

            Log.v(apiErrorException, e.getMessage());
            updateAlertDialog(mActivity, getString(R.string.applicationError),
                    getString(R.string.contactYourServiceProvider), (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        finish();
                    });
        }
    }

    private void proceedToActivity(String version) {


        if (Double.parseDouble(version) > Double.parseDouble(currentVersion)) {
            Log.v("MyVersion", "C " + currentVersion + "L " + version);
            updateAlertsDialog(version);
        } else {

            startActivity(new Intent(mActivity, Dashboard.class));
            finish();

        }
    }


    public boolean getPermission() {
        List<String> listPermissionsNeedFor = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            for (String permission : appPermissionTiramisu)
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                    listPermissionsNeedFor.add(permission);

                else
                    for (String permissions : appPermission)
                        if (ContextCompat.checkSelfPermission(this, permissions) != PackageManager.PERMISSION_GRANTED)
                            listPermissionsNeedFor.add(permissions);


        if (!listPermissionsNeedFor.isEmpty()) {
            ActivityCompat.requestPermissions(mActivity, listPermissionsNeedFor.toArray(new String[listPermissionsNeedFor.size()]), permission_granted_code);
            return false;
        }
        return true;
    }

    /*public boolean getPermission() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            List<String> listPermissionsNeedeFor = new ArrayList<>();
            for (String permission : appPermission) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeedeFor.add(permission);
                }
            }
            if (!listPermissionsNeedeFor.isEmpty()) {
                ActivityCompat.requestPermissions(mActivity, listPermissionsNeedeFor.toArray(new String[listPermissionsNeedeFor.size()]), permission_granted_code);
                return false;
            }
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.POST_NOTIFICATIONS},789);
                return false;
            }
        }
        return true;
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permission_granted_code) {
            HashMap<String, Integer> permisionResults = new HashMap<>();
            int deniedPermissionCount = 0;


            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permisionResults.put(permissions[i], grantResults[i]);
                    deniedPermissionCount++;
                    Log.v("Denied", "Permission: "+ permissions[i]);
                }
            }
            if (deniedPermissionCount == 0) {
                setInitals();
            } else {
                for (Map.Entry<String, Integer> entry : permisionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permName)) {
//                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
                        showAlertDialog(mActivity, "", "Do allow or permission to make application work fine"
                                , "Yes, Grant Permission", (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    getPermission();
                                }, "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();

                                    }
                                }, false);
//                        }
                    } else {
//                        if(permName.equals(Manifest.permission.POST_NOTIFICATIONS)){
//                            setInitals();
//                            return;
//                        }
                        showAlertDialog(mActivity, "", "You have denied some permissons. Allow all permissions at [Settings] > [Permissions]", "Go to settings"
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();

                                    }
                                }, false);
                        break;

                    }
                }
            }
        } else if (requestCode == 789) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.POST_NOTIFICATIONS)) {
                        showAlertDialog(mActivity, "", "Allow notification to get updated about sales and offers."
                                , "Yes, Grant Permission", (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    getPermission();
                                }, "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();

                                    }
                                }, false);
                    } else {
                        setInitals();
                    }
                } else {
                    setInitals();
                }
            }
        }
    }

    public void updateAlertsDialog(String latestVersion) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(mActivity,
                R.style.Theme_AppCompat_DayNight_Dialog));

        alertDialogBuilder.setTitle(mActivity.getString(R.string.youAreNotUpdatedTitle));
        alertDialogBuilder.setMessage(mActivity.getString(R.string.youAreNotUpdatedMessage) + " " + latestVersion + mActivity.getString(R.string.youAreNotUpdatedMessage1));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mActivity.getPackageName())));
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();

    }


}