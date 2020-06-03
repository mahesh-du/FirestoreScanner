package com.example.firestorescanner;

public interface Constants {

    //logCatTags
    String logCatTag_APPLICATION                = "Scanner";
    String logCatTag_Activity_SPLASH_SCREEN     = "SplashScreen_Activity";
    String logCatTag_Activity_REGISTER_DEVICE   = "RegisterDevice_Activity";
    String logCatTag_Activity_LOGIN             = "Login_Activity";
    String logCatTag_Activity_MAIN              = "Main_Activity";
    String logCatTag_Activity_SCAN              = "Scan_Activity";

    String logCatTag_Helpers_HELPER             = "Helper";
    String logCatTag_Helpers_LOCATION           = "Location";
    String logCatTag_Helpers_BATTERY_SERVICE    = "Battery_Service";

    String logCatTag_TASK_Helpers_REGISTER_DEVICE   = "Task_Helper_Register_Device";
    String logCatTag_TASK_Helpers_LOGIN_DEVICE      = "Task_Helper_Login_Device";

    String logCatTag_Task_Background_LOGIN_DEVICE       = "Task_Background_Login_Device";
    String logCatTag_Task_Background_REGISTER_DEVICE    = "Task_Background_Register_Device";

    //Shared Preferences Keys
    String KEY_institution_path ="INSTITUTION_PATH";
    String KEY_device_name      ="DEVICE_NAME";
    String KEY_actual_position  ="ACTUAL POSITION";
    String KEY_isBatteryAlertExists = "IS_BATTERY_ALERT_EXISTS";

    //Firestore Paths
    String  firestore_device            ="/Devices_Doc/Devices";
    String  firestore_alerts            ="/Devices_Doc/Alerts";
    String  firestore_alerts_battery    ="/Devices_Doc/Alerts/Battery";
    String  firestore_email             ="/DV/Email";
    String  firestore_ids               = "/ID_doc/IDS";
    String firestore_analysis_to_Year   = "/Analysis/Statistics/Year";

    //Firestore Fields.

    //Analysis fields
    String firestore_ANALYSIS_FIELD_DATA        = "data";
    //Id Fields
    String firestore_IDS_FIELD_Name             = "Name";
    String firestore_IDS_FIELD_Phone_Number     = "Phone No";
    String firestore_IDS_FIELD_Address          = "Address";
    String firestore_IDS_FIELD_Admission_No     = "Admission No";
    String firestore_IDS_FIELD_Age              = "Age";
    String firestore_IDS_FIELD_Blocked          = "Blocked";
    String firestore_IDS_FIELD_Email            = "Email";
    String firestore_IDS_FIELD_Gender           = "Gender";
    String firestore_IDS_FIELD_Image            = "Profile Picture";

    //Device Fields
    String firestore_DEVICE_FIELD_Device_Name       ="Device Name";
    String firestore_DEVICE_FIELD_IMEI              ="IMEI";
    String firestore_DEVICE_FIELD_Blocked           ="Blocked";
    String firestore_DEVICE_FIELD_Scans_Today       = "Scans Today";
    String firestore_DEVICE_FIELD_Actual_Position   ="Actual Position";
    String firestore_DEVICE_FIELD_GPS_Location      ="GPS Location";
    String firestore_DEVICE_FIELD_Network_Status    ="Network Status";
    String firestore_DEVICE_FIELD_Battery_Remaining ="Battery Remaining";
    String firestore_DEVICE_FIELD_Battery_Status    ="Battery Status";
    String firestore_DEVICE_FIELD_Logged_In_Email   ="Logged In Email";
    String firestore_DEVICE_FIELD_Logged_In         ="Logged In";
    //String  ="";

    //Alert Fields
    String firestore_ALERT_FIELD_Device_Name            = "Device Name";
    String firestore_ALERT_FIELD_Device_Actual_Position = "Device Actual Position";
    String firestore_ALERT_FIELD_Created_At             = "Created At";

    String firestore_ALERT_FIELD_Alert_Type_BATTERY_LOW = "Battery Low";
    String firestore_ALERT_FIELD_Alert_Type_OFFLINE     = "Device Offline";

    //Alert Battery Fields
    String firestore_ALERT_FIELD_Battery_Remaining  = "Battery Remaining";
    String firestore_ALERT_FIELD_Battery_Status     = "Battery Status";
    String firestore_ALERT_FIELD_Message            = "Message";

}
