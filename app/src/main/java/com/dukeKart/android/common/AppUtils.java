package com.dukeKart.android.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.dukeKart.android.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtils {
    private static Toast mToast;
    private static boolean doubleBackToExitPressedOnce;

    public static String getColoredSpanned(String text, String color) {
        return "<font color=" + color + ">" + text + "</font>";
    }

    public static String readTimeStampDate(String timeStampDate) {
        try {
            Date date = new Date(Long.parseLong(timeStampDate));
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
            String myDate = format.format(date);
            return myDate;
        } catch (Exception e) {
            handleCatch(e);
        }
        return "";
    }

    public static int getDays(String startDate) {
        try {
            long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
            long begin = dateFormat.parse(startDate).getTime();
            long end = new Date().getTime(); // 2nd date want to compare
            long diff = (end - begin) / (MILLIS_PER_DAY);


            Log.d("Days_Ago", "my " + diff);
            return (int) diff;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static boolean isNumberValid(String mobile) {
        boolean isValid = false;
        String expression = "^[0-9]{10}$";
        CharSequence inputStr = mobile;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isEmailValid(String email) {

        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static String getTextInputEditTextData(TextInputEditText textInputEditText) {
        return textInputEditText.getText().toString().trim();
    }

    public static String getTextInputEditTextData(EditText textInputEditText) {
        return textInputEditText.getText().toString().trim();
    }

    public static String getDeviceID(Context ctx) {
        // return "123456";
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public static void showToast(Context context, String text) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void handleCatch(Context context, Exception e) {
        e.printStackTrace();
        showToast(context, AppsStrings.defResponse);
    }

    public static void handleCatch(Exception e) {
        e.printStackTrace();
    }

    public static void shareApplication(Context context) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "*" + "dukeKart App" + "*" + "\n" + "Hi There!\n" +
                "Download the dukeKart app and register yourself. \n" +
                "Download link - https://play.google.com/store/apps/details?id=" + context.getPackageName() + "\n" +
                "Hava a nice day!\n" +
                "dukeKart Operation Team");
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public static void rateApplication(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
    }

    public static void shareToBrowser(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);

    }

    public static String getAppVersion(Activity context) {
        String latestVersion = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            latestVersion = String.valueOf(info.versionName);

        } catch (Exception e) {
            handleCatch(context, e);
        }
//        Toast.makeText(this, "VersionCode : " + info.versionCode + ", VersionName : " + info.versionName , Toast.LENGTH_LONG).show();
        return latestVersion;
    }

    @SuppressLint("NewApi")
    public static void onBackPressed(Activity mActivity) {
        if (doubleBackToExitPressedOnce) {
            mActivity.finishAffinity();
            return;
        }

        doubleBackToExitPressedOnce = true;

        AppUtils.showToast(mActivity, "Press again to exit");
        //Toast.makeText(mActivity, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity
                    .getCurrentFocus().getWindowToken(), 0);
    }

    public static void setImgPicasso(String imgPicasso, Context context, ImageView imgView) {
        if (imgPicasso.contains("dl=0")&& !imgPicasso.isEmpty()) {
            imgPicasso = imgPicasso.replace("dl=0", "raw=1");
        }
        if (!imgPicasso.isEmpty())
            PicassoTrustAll.getInstance(context)
                    .load(imgPicasso)
                    .placeholder(R.mipmap.placeholder)
                    .into(imgView);
    }

    public static String getDateTimeFromTimestamp(long timestamp) {

        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");

        System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp * 1000);
        System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String getDate(long timestamp) {

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp * 1000);
        System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String setPrice(String price) {
        return "₹ " + price;
    }

    public static String setPriceTotal(String price) {
        return "₹ " + price + " /-";
    }

    public static String setPriceTotal(int price) {
        return "₹ " + price + " /-";
    }

    public static String updateQuantity(String oldQuantity, int newQuantity) {
        if ((oldQuantity.equalsIgnoreCase("0") || oldQuantity.equalsIgnoreCase("")) && newQuantity <= 0)
            return "0";
        return Integer.toString(Integer.parseInt(oldQuantity) + newQuantity);
    }

    public static void updateAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener negativeClickListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                R.style.Theme_AppCompat_DayNight_Dialog));
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setNegativeButton(R.string.close, negativeClickListener);

        alertDialogBuilder.show();

    }

    public static AlertDialog showAlertDialog(Context context, String title, String message, String positiveLabel,
                                              DialogInterface.OnClickListener positiveClick, String negativeLabel,
                                              DialogInterface.OnClickListener negativeClick, boolean isCancelable) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title);
        dialogBuilder.setCancelable(isCancelable);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(positiveLabel, positiveClick);
        dialogBuilder.setNegativeButton(negativeLabel, negativeClick);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        return alertDialog;
    }


    public static String addDays(String oldDate, int days) {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        try {
            Date date = formatter.parse(oldDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            calendar.add(Calendar.DAY_OF_MONTH, days);

            System.out.println("Cool" + formatter.format(calendar.getTime()));
            return formatter.format(calendar.getTime());
        } catch (ParseException e) {
            System.out.println("Wrong date");
        }
        return "";
    }

    public static String getTomorrowDate() {

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");// HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        System.out.println("Current time => " + formattedDate);

        return formattedDate;
    }


    public static int dpToPx(int dp, Context context) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }




}
