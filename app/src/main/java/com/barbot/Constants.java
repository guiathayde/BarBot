package com.barbot;

class Constants {

    // values have to be globally unique
    static final String INTENT_ACTION_DISCONNECT = "com.barbot.Disconnect";
    static final String NOTIFICATION_CHANNEL = "com.barbot.Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY = "com.barbot.MainActivity";

    // values have to be unique within each app
    static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    private Constants() {}
}
