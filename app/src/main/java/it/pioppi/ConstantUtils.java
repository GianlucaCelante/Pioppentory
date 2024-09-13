package it.pioppi;

import java.util.UUID;

public class ConstantUtils {

    // DATABASE
    public static final String APP_DATABASE = "INVENTORY";
    public static final String ITEM_TABLE_NAME = "ITEM";
    public static final String ITEM_DETAIL_TABLE_NAME = "ITEM_DETAIL";
    public static final String PROVIDER_TABLE_NAME = "PROVIDER";
    public static final String QUANTITY_TYPE_TABLE_NAME = "QUANTITY_TYPE";

    // BLUETOOTH
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    public static final int REQUEST_LOCATION_PERMISSION = 3;
    public static final int REQUEST_DISCOVERABLE = 4;
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String ACTION_CODE_SCANNED = "ACTION_CODE_SCANNED";
    public static final String SCANNED_CODE = "SCANNED_CODE";

    // FRAGMENT
    public static final String ITEM_ID = "itemId";


    // NOTIFICATION
    public static final String ITEM_DETAIL_FRAGMENT_TAG = "ItemDetailFragmentTag";
}
