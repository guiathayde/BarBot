package com.barbot;

class Constants {

    // values have to be globally unique
    static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    // values have to be unique within each app
    static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    static final String DATABASE_NAME = "barbot";

    static final String DRINK_ONE = "drinkOne";
    static final String DRINK_TWO = "drinkTwo";
    static final String DRINK_THREE = "drinkThree";
    static final String DRINK_FOUR = "drinkFour";
    static final String DRINK_FIVE = "drinkFive";
    static final String DRINK_SIX = "drinkSix";

    static final String DRINK_ONE_NAME = "drinkOneName";
    static final String DRINK_TWO_NAME = "drinkTwoName";
    static final String DRINK_THREE_NAME = "drinkThreeName";
    static final String DRINK_FOUR_NAME = "drinkFourName";
    static final String DRINK_FIVE_NAME = "drinkFiveName";
    static final String DRINK_SIX_NAME = "drinkSixName";

    static final String DRINK_ONE_QUANTITY = "drinkOneQuantity";
    static final String DRINK_TWO_QUANTITY = "drinkTwoQuantity";
    static final String DRINK_THREE_QUANTITY = "drinkThreeQuantity";
    static final String DRINK_FOUR_QUANTITY = "drinkFourQuantity";
    static final String DRINK_FIVE_QUANTITY = "drinkFiveQuantity";
    static final String DRINK_SIX_QUANTITY = "drinkSixQuantity";

    static final String[] ALL_DRINKS_KEYS = {
            DRINK_ONE,
            DRINK_TWO,
            DRINK_THREE,
            DRINK_FOUR,
            DRINK_FIVE,
            DRINK_SIX,
    };

    static final String[] ALL_DRINKS_NAMES = {
            DRINK_ONE_NAME,
            DRINK_TWO_NAME,
            DRINK_THREE_NAME,
            DRINK_FOUR_NAME,
            DRINK_FIVE_NAME,
            DRINK_SIX_NAME,
    };

    static final String[] ALL_DRINKS_QUANTITY = {
            DRINK_ONE_QUANTITY,
            DRINK_TWO_QUANTITY,
            DRINK_THREE_QUANTITY,
            DRINK_FOUR_QUANTITY,
            DRINK_FIVE_QUANTITY,
            DRINK_SIX_QUANTITY,
    };

    private Constants() {}
}
