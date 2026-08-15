package com.example.weighttrackingapp_chelseacacho.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weighttrackingapp_chelseacacho.utility.GoalManager;

/**
 * Stores the user's weight goal and SMS settings
 * in the application's SharedPreferences file.
 */
public class GoalPreferences {

    private static final String PREFERENCES_NAME = "WeightAppPrefs";

    private static final String KEY_HAS_GOAL = "has_goal";
    private static final String KEY_START_WEIGHT = "start_weight";
    private static final String KEY_TARGET_WEIGHT = "target_weight";

    private static final String KEY_SMS_ENABLED = "sms_enabled";
    private static final String KEY_SMS_PHONE = "sms_phone";
    private static final String KEY_GOAL_SMS_SENT = "goal_sms_sent";

    private final SharedPreferences preferences;

    /**
     * Opens the application's shared preference file.
     *
     * @param context application or activity context
     */
    public GoalPreferences(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );
    }

    /**
     * Saves the user's starting weight and target weight.
     * Saving a new goal also resets the SMS notification status.
     *
     * @param startWeight  weight when the goal was created
     * @param targetWeight weight the user wants to reach
     */
    public void saveGoal(double startWeight, double targetWeight) {
        preferences.edit()
                .putBoolean(KEY_HAS_GOAL, true)
                .putLong(
                        KEY_START_WEIGHT,
                        Double.doubleToRawLongBits(startWeight)
                )
                .putLong(
                        KEY_TARGET_WEIGHT,
                        Double.doubleToRawLongBits(targetWeight)
                )
                .putBoolean(KEY_GOAL_SMS_SENT, false)
                .apply();
    }

    /**
     * Retrieves the saved goal and creates a GoalManager object.
     *
     * @return GoalManager containing the saved goal,
     *         or null when no goal has been saved
     */
    public GoalManager getGoalManager() {
        boolean hasGoal = preferences.getBoolean(KEY_HAS_GOAL, false);

        if (!hasGoal) {
            return null;
        }

        long defaultWeight = Double.doubleToRawLongBits(0.0);

        double startWeight = Double.longBitsToDouble(
                preferences.getLong(
                        KEY_START_WEIGHT,
                        defaultWeight
                )
        );

        double targetWeight = Double.longBitsToDouble(
                preferences.getLong(
                        KEY_TARGET_WEIGHT,
                        defaultWeight
                )
        );

        return new GoalManager(startWeight, targetWeight);
    }

    /**
     * Removes the user's saved goal and notification status.
     */
    public void clearGoal() {
        preferences.edit()
                .remove(KEY_HAS_GOAL)
                .remove(KEY_START_WEIGHT)
                .remove(KEY_TARGET_WEIGHT)
                .remove(KEY_GOAL_SMS_SENT)
                .apply();
    }

    /**
     * Checks whether SMS goal notifications are enabled.
     *
     * @return true when SMS notifications are enabled
     */
    public boolean isSmsEnabled() {
        return preferences.getBoolean(KEY_SMS_ENABLED, false);
    }

    /**
     * Retrieves the saved phone number.
     *
     * @return saved phone number or an empty string
     */
    public String getSmsPhone() {
        return preferences.getString(KEY_SMS_PHONE, "");
    }

    /**
     * Checks whether the goal notification has already been sent.
     *
     * @return true when the notification was already sent
     */
    public boolean isGoalSmsSent() {
        return preferences.getBoolean(KEY_GOAL_SMS_SENT, false);
    }

    /**
     * Updates the goal notification status.
     *
     * @param sent true after the goal SMS has been sent
     */
    public void setGoalSmsSent(boolean sent) {
        preferences.edit()
                .putBoolean(KEY_GOAL_SMS_SENT, sent)
                .apply();
    }

    /**
     * Saves the user's SMS notification settings.
     *
     * @param context application or activity context
     * @param enabled whether SMS notifications are enabled
     * @param phone user's phone number
     */
    public static void saveSmsSettings(
            Context context,
            boolean enabled,
            String phone
    ) {
        String safePhone = phone == null ? "" : phone.trim();

        context.getApplicationContext()
                .getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                )
                .edit()
                .putBoolean(KEY_SMS_ENABLED, enabled)
                .putString(KEY_SMS_PHONE, safePhone)
                .apply();
    }
}