package com.barbot.constant;

import com.barbot.BuildConfig;

public class Constants {

    // values have to be globally unique
    public static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    public static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    public static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    // values have to be unique within each app
    public static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    public static final String DATABASE_NAME = "barbot";

    public static final String DRINK_ONE = "drinkOne";
    public static final String DRINK_TWO = "drinkTwo";
    public static final String DRINK_THREE = "drinkThree";
    public static final String DRINK_FOUR = "drinkFour";
    public static final String DRINK_FIVE = "drinkFive";
    public static final String DRINK_SIX = "drinkSix";

    public static final String DRINK_ONE_NAME = "drinkOneName";
    public static final String DRINK_TWO_NAME = "drinkTwoName";
    public static final String DRINK_THREE_NAME = "drinkThreeName";
    public static final String DRINK_FOUR_NAME = "drinkFourName";
    public static final String DRINK_FIVE_NAME = "drinkFiveName";
    public static final String DRINK_SIX_NAME = "drinkSixName";

    public static final String DRINK_ONE_QUANTITY = "drinkOneQuantity";
    public static final String DRINK_TWO_QUANTITY = "drinkTwoQuantity";
    public static final String DRINK_THREE_QUANTITY = "drinkThreeQuantity";
    public static final String DRINK_FOUR_QUANTITY = "drinkFourQuantity";
    public static final String DRINK_FIVE_QUANTITY = "drinkFiveQuantity";
    public static final String DRINK_SIX_QUANTITY = "drinkSixQuantity";

    public static final String[] ALL_DRINKS_KEYS = {
            DRINK_ONE,
            DRINK_TWO,
            DRINK_THREE,
            DRINK_FOUR,
            DRINK_FIVE,
            DRINK_SIX,
    };

    public static final String[] ALL_DRINKS_NAMES = {
            DRINK_ONE_NAME,
            DRINK_TWO_NAME,
            DRINK_THREE_NAME,
            DRINK_FOUR_NAME,
            DRINK_FIVE_NAME,
            DRINK_SIX_NAME,
    };

    public static final String[] ALL_DRINKS_QUANTITY = {
            DRINK_ONE_QUANTITY,
            DRINK_TWO_QUANTITY,
            DRINK_THREE_QUANTITY,
            DRINK_FOUR_QUANTITY,
            DRINK_FIVE_QUANTITY,
            DRINK_SIX_QUANTITY,
    };

    private Constants() {}
}
