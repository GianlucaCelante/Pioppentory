package it.pioppi.utils;

import java.util.UUID;

public class ConstantUtils {

    // DATABASE
    public static final String APP_DATABASE = "INVENTORY";
    public static final String ITEM_TABLE_NAME = "ITEM";
    public static final String ITEM_DETAIL_TABLE_NAME = "ITEM_DETAIL";
    public static final String PROVIDER_TABLE_NAME = "PROVIDER";
    public static final String QUANTITY_TYPE_TABLE_NAME = "QUANTITY_TYPE";
    public static final String ITEM_TAG_TABLE_NAME = "ITEM_TAG";
    public static final String ITEM_HISTORY_TABLE_NAME = "ITEM_HISTORY";
    public static final String ITEM_TAG_JOIN_TABLE_NAME = "ITEM_TAG_JOIN";

    // BLUETOOTH
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    public static final int REQUEST_LOCATION_PERMISSION = 3;
    public static final int REQUEST_DISCOVERABLE = 4;
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String ACTION_CODE_SCANNED = "ACTION_CODE_SCANNED";
    public static final String SCANNED_CODE = "SCANNED_CODE";

    // ITEM FRAGMENT
    public static final String ITEM_ID = "itemId";
    public static final String IMAGE_URL = "imageUrl";
    public static final int GRID_LAYOUT_NUMBER_COLUMNS = 5;
    
    // TIMESTAMP
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_FORMAT_DATE = "yyyy-MM-dd";
    public static final String ZONE_ID = "Europe/Rome";

    public static final int TYPE_HEADER = 1;
}
