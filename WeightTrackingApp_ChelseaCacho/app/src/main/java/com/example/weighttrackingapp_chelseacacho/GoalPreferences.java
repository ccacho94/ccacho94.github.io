package com.example.weighttrackingapp_chelseacacho;

import android.content.Context;
import android.content.SharedPreferences;

public class GoalPreferences {

    private static final String PREFS_NAME = "WeightAppPrefs";

    // Goal keys
    public static final String KEY_GOAL_IS_SET = "goal_is_set";
    public static final String KEY_GOAL_START_WEIGHT = "goal_start_weight";
    public static final String KEY_GOAL_TARGET_WEIGHT = "goal_target_weight";

    // SMS keys
    public static final String KEY_SMS_ENABLED = "sms_enabled";
    public static final String KEY_SMS_PHONE = "sms_phone";

    // Prevent duplicate goal reached SMS/toast for same goal
    public static final String KEY_GOAL_SMS_SENT = "goal_sms_sent";

    private GoalPreferences() {
        // no instances
    }

    public static void saveGoal(Context context, double startWeight, double targetWeight) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_GOAL_IS_SET, true)
                .putFloat(KEY_GOAL_START_WEIGHT, (float) startWeight)
                .putFloat(KEY_GOAL_TARGET_WEIGHT, (float) targetWeight)
                .putBoolean(KEY_GOAL_SMS_SENT, false) // reset when new goal is set
                .apply();
    }

    public static GoalManager getGoalManager(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        boolean isSet = prefs.getBoolean(KEY_GOAL_IS_SET, false);
        if (!isSet) return null;

        float startWeight = prefs.getFloat(KEY_GOAL_START_WEIGHT, 0f);
        float targetWeight = prefs.getFloat(KEY_GOAL_TARGET_WEIGHT, 0f);

        return new GoalManager(startWeight, targetWeight);
    }

    public static void saveSmsSettings(Context context, boolean enabled, String phoneNumber) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_SMS_ENABLED, enabled)
                .putString(KEY_SMS_PHONE, phoneNumber == null ? "" : phoneNumber)
                .apply();
    }

    public static boolean isSmsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SMS_ENABLED, false);
    }

    public static String getSmsPhone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SMS_PHONE, "");
    }

    public static boolean isGoalSmsSent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_GOAL_SMS_SENT, false);
    }

    public static void setGoalSmsSent(Context context, boolean sent) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_GOAL_SMS_SENT, sent).apply();
    }

    // Optional helper if you want to clear goal later
    public static void clearGoal(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_GOAL_IS_SET)
                .remove(KEY_GOAL_START_WEIGHT)
                .remove(KEY_GOAL_TARGET_WEIGHT)
                .remove(KEY_GOAL_SMS_SENT)
                .apply();
    }
}