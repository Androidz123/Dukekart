package com.dukeKart.android.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.dukeKart.android.R;
import com.dukeKart.android.common.AppUrls;
import com.dukeKart.android.common.AppsStrings;
import com.dukeKart.android.database.AppConstants;
import com.dukeKart.android.database.Preferences;
import com.dukeKart.android.views.BaseFragment;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.dukeKart.android.common.AppUtils.getTextInputEditTextData;
import static com.dukeKart.android.common.AppUtils.isEmailValid;
import static com.dukeKart.android.common.AppUtils.isNumberValid;
import static com.dukeKart.android.common.AppUtils.setImgPicasso;
import static com.dukeKart.android.common.AppUtils.showToast;
import static com.dukeKart.android.common.AppsStrings.apiErrorException;
import static com.dukeKart.android.common.AppsStrings.defResponse;

public class Profile extends BaseFragment implements View.OnClickListener {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int SELECT_PICTURE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static CircleImageView circularImageView;
    private static String IMAGE_DIRECTORY_NAME = "Hello Camera";
    public Uri fileUri;
    Preferences pref;
    String picString = "";
    ImageView ivBack;
    View view;
    Button btnContinue;
    EditText et_proFullName, et_proEmail, et_proMobile, et_proWhatsapp, et_proAltMobile;
    private Bitmap bitmap;

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {

            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_frag, container, false);
        startPage();

        return view;

    }

    private void startPage() {
        setIDs();
        setListeners();
        fillData();
    }

    public void setIDs() {
        pref = new Preferences(mActivity);
        circularImageView = view.findViewById(R.id.circularImageView);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        IMAGE_DIRECTORY_NAME = getResources().getString(R.string.app_name);
        et_proFullName = view.findViewById(R.id.et_proFullName);
        et_proEmail = view.findViewById(R.id.et_proEmail);
        et_proMobile = view.findViewById(R.id.et_proMobile);
        et_proWhatsapp = view.findViewById(R.id.et_proWhatsapp);
        et_proAltMobile = view.findViewById(R.id.et_proAltMobile);
/*
        et_proAddress = view.findViewById(R.id.et_proAddress);
        et_proLandMark = view.findViewById(R.id.et_proLandMark);
        et_proPincode = view.findViewById(R.id.et_proPincode);
        et_proCity = view.findViewById(R.id.et_proCity);
        et_proState = view.findViewById(R.id.et_proState);
*/
        btnContinue = view.findViewById(R.id.btnContinue);
    }

    public void setListeners() {
        circularImageView.setOnClickListener(this);
        btnContinue.setOnClickListener(this);

    }

    private void fillData() {
        et_proFullName.setText(pref.get(AppConstants.userName));
        et_proEmail.setText(pref.get(AppConstants.userEmail));
        et_proMobile.setText(pref.get(AppConstants.userMobile));
        et_proWhatsapp.setText(pref.get(AppConstants.userWhatsapNumber));
/*
        et_proAddress.setText(pref.get(AppConstants.userName));
        et_proLandMark.setText(pref.get(AppConstants.userLocality));
        et_proPincode.setText(pref.get(AppConstants.userPincode));
        et_proCity.setText(pref.get(AppConstants.userCity));
        et_proState.setText(pref.get(AppConstants.userState));
*/
        et_proAltMobile.setText(pref.get(AppConstants.userAlternateNumber));
        setImgPicasso(pref.get(AppConstants.userProfPic), mActivity, circularImageView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.circularImageView:
                showCameraGalleryDialog();
                break;
            case R.id.btnContinue:
                if (validate())
                    updateProfileApi();
                break;
        }
    }

    public void showCameraGalleryDialog() {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.cam_gal_popup);
        dialog.show();
        RelativeLayout rrCancel = dialog.findViewById(R.id.rr_cancel);
        LinearLayout llCamera = dialog.findViewById(R.id.ll_camera);
        LinearLayout llGallery = dialog.findViewById(R.id.ll_gallery);
        llCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
                dialog.dismiss();
            }
        });
        llGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryPicker();
                dialog.dismiss();
            }
        });
        rrCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void galleryPicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        photoPickerIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PICTURE);
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        String picturePath = "", filename = "", encodedString = "";
        Bitmap bitmap;
        Matrix matrix;
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                picturePath = fileUri.getPath();
                filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);
                Log.v("MYImgPath", filename);
                Log.v("MYImgPath", "img" + pref.get("MyImgUrl"));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(picturePath, options);
                final int REQUIRED_SIZE = 500;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(picturePath, options);
                matrix = new Matrix();
                matrix.postRotate(getImageOrientation(picturePath));
                if (bitmap != null) {
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                    byte[] ba = bao.toByteArray();
                    encodedString = getEncoded64ImageStringFromBitmap(bitmap);
                    Log.v("encodedstring", encodedString);
                    setPictures(bitmap, picturePath, matrix, encodedString);
                }
            }
        } else if (requestCode == SELECT_PICTURE) {
            if (data != null) {
                Uri contentURI = data.getData();
                //get the Uri for the captured image
                fileUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(contentURI, filePathColumn, null, null, null);
                cursor.moveToFirst();
                Log.v("piccc", "pic");
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                System.out.println("Image Path : " + picturePath);
                cursor.close();
                filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);
                String selectedImagePath = picturePath;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 500;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(selectedImagePath, options);

                matrix = new Matrix();
                matrix.postRotate(getImageOrientation(picturePath));
                if (bitmap != null) {
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                    byte[] ba = bao.toByteArray();
                    encodedString = getEncoded64ImageStringFromBitmap(bitmap);
                    Log.v("encodedstring", encodedString);
                    Log.v("picture_path====", filename);
                    setPictures(bitmap, picturePath, matrix, encodedString);
                }
            } else {
                Toast.makeText(getActivity(), "unable to select image", Toast.LENGTH_LONG).show();
            }

        }

    }

    public void setPictures(Bitmap b, String picturePath, Matrix matrix, String encodedString) {

        circularImageView.setImageBitmap(b);
        matrix.postRotate(getImageOrientation(picturePath));
        picString = encodedString;
        updateProfileApi();
    }

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }

    public boolean validate() {

        if (!isEmailValid(getTextInputEditTextData(et_proEmail))) {
            showToast(mActivity, "Enter a valid email address");
        } else if (!isNumberValid(getTextInputEditTextData(et_proMobile))) {
            showToast(mActivity, "Enter a valid mobile number");
        } else if (!isNumberValid(getTextInputEditTextData(et_proWhatsapp)) && getTextInputEditTextData(et_proWhatsapp).length() > 0) {
            showToast(mActivity, "Enter a valid whatsapp number");
        } else if (!isNumberValid(getTextInputEditTextData(et_proAltMobile)) && getTextInputEditTextData(et_proAltMobile).length() > 0) {
            showToast(mActivity, "Enter a valid alternate mobile number");
        } else {
            return true;
        }

        return false;
    }

    private void updateProfileApi() {

        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();
        String url = AppUrls.UpdateProfile;
        try {
            showLoading();
            AppsStrings.setApiString("updateProfile");
            Log.v(AppsStrings.apiUrl, url);
            json_data.put("user_id", pref.get(AppConstants.userId));
            json_data.put("username", getTextInputEditTextData(et_proFullName));
            json_data.put("email_id", getTextInputEditTextData(et_proEmail));
            json_data.put("mobile", getTextInputEditTextData(et_proMobile));
            json_data.put("whatsaap_number", getTextInputEditTextData(et_proWhatsapp));
            json_data.put("alternate_number", getTextInputEditTextData(et_proAltMobile));
            json_data.put("address", "");
            json_data.put("locality", "");
            json_data.put("city", "");
            json_data.put("state", "");
            json_data.put("pincode", "");
            json_data.put("profile_pic", picString);
            json.put(AppConstants.appName, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
            showToast(mActivity, defResponse);
            Log.v(apiErrorException, e.getMessage());
            e.printStackTrace();
        }


        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseUpdateProfile(response);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }
    private void parseUpdateProfile(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            showToast(mActivity, jsonObject.getString("res_msg"));

            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                pref.set(AppConstants.userId, jsonObject.getString("userId"));
                pref.set(AppConstants.userEmail, jsonObject.getString("userName"));
                pref.set(AppConstants.userMobile, jsonObject.getString("mobile"));
                pref.set(AppConstants.userProfPic, jsonObject.getString("profile_pic"));
                pref.set(AppConstants.userWhatsapNumber, jsonObject.getString("whatsaap_number"));
                pref.set(AppConstants.userAlternateNumber, jsonObject.getString("alternate_number"));
                pref.set(AppConstants.userAddress, jsonObject.getString("address"));
                pref.set(AppConstants.userLocality, jsonObject.getString("locality"));
                pref.set(AppConstants.userCity, jsonObject.getString("city"));
                pref.set(AppConstants.userState, jsonObject.getString("state"));
                pref.set(AppConstants.userPincode, jsonObject.getString("pincode"));
                pref.commit();

            }

        } catch (Exception e) {
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }


}
