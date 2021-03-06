package com.android.server.emcom;

import android.os.SystemProperties;

public class SmartcareConstants {
    public static final short BROWSER_ABNORMAL_WLAN_INFO_START = (short) 28;
    public static final int BROWSER_EXPERIENCE_ABNORMAL_LOG = 919000001;
    public static final short BROWSER_EXPERIENCE_FWK_INFO_START = (short) 4;
    public static final short BROWSER_EXPERIENCE_INFO_START = (short) 12;
    public static final int BROWSER_EXPERIENCE_NORMAL_LOG = 919000000;
    public static final short BROWSER_EXPERIENCE_TCP_INFO_START = (short) 13;
    public static final short BROWSER_NORMAL_WLAN_INFO_START = (short) 16;
    public static final String BROWSER_TYPE = "browser";
    public static final short COUNT_OF_TCP_STATUS_ITEM = (short) 12;
    public static final int CS_DOMAIN = 1;
    public static final int CS_PS_DOMAIN = 3;
    public static final boolean DEBUG_MODE = SystemProperties.getBoolean("ro.config.hwsmartcaretest", true);
    public static final short EMAIL_ABNORMAL_INFO_START = (short) 13;
    public static final int EMAIL_ABNORMAL_LOG = 919000005;
    public static final short EMAIL_ABNORMAL_WLAN_INFO_START = (short) 20;
    public static final short EMAIL_FWK_INFO_START = (short) 4;
    public static final short EMAIL_INFO_START = (short) 0;
    public static final int EMAIL_LOGIN_TYPE = 0;
    public static final short EMAIL_NORMAL_INFO_START = (short) 12;
    public static final int EMAIL_NORMAL_LOG = 919000004;
    public static final short EMAIL_NORMAL_WLAN_INFO_START = (short) 19;
    public static final int EMAIL_RECEIVE_TYPE = 1;
    public static final int EMAIL_SEND_TYPE = 2;
    public static final String EMAIL_TYPE = "email";
    public static final long HISI_SLICE_GAP = 32768;
    public static final int HISI_SLICE_MAX_TO_INT = 131071;
    public static final int IMONITOR_EVENT_ID_INVALID = 0;
    public static final int INT_CLOUD_OTA_SWITCH_STATE_ANY_NETWORK = 0;
    public static final int INT_CLOUD_OTA_SWITCH_STATE_CLOSED = 2;
    public static final int INT_CLOUD_OTA_SWITCH_STATE_ONLY_WIFI = 1;
    public static final int INVALID = Integer.MAX_VALUE;
    public static final byte KPI_INVALID = (byte) 0;
    public static final byte KPI_VALID = (byte) 1;
    public static final String LAB_PLMN1 = "46060";
    public static final String LAB_PLMN2 = "00101";
    public static final int MAX_PHONE_COUNT_DUAL_SIM = 2;
    public static final int MAX_PHONE_COUNT_SINGLE_SIM = 1;
    public static final int MAX_PHONE_COUNT_TRI_SIM = 3;
    public static final int MAX_UINT_PARA_VALUE = -1;
    public static final int MODEM_0 = 0;
    public static final int NAS_RADIO_IF_CDMA_1X = 1;
    public static final int NAS_RADIO_IF_CDMA_1XEVDO = 2;
    public static final int NAS_RADIO_IF_GSM = 4;
    public static final int NAS_RADIO_IF_LTE = 8;
    public static final int NAS_RADIO_IF_NO_SVC = 0;
    public static final int NAS_RADIO_IF_TDSCDMA = 9;
    public static final int NAS_RADIO_IF_UMTS = 5;
    public static final String NEED_MODEMLOG = "Y";
    public static final int NETWORK_TYPE_CDMA = 2;
    public static final int NETWORK_TYPE_EVDO = 4;
    public static final int NETWORK_TYPE_GSM = 0;
    public static final int NETWORK_TYPE_LTE = 3;
    public static final int NETWORK_TYPE_TDSCDMA = 5;
    public static final int NETWORK_TYPE_UMTS = 1;
    public static final int NETWORK_TYPE_UNKWON = -1;
    public static final String NO = "NO";
    public static final int NONE_DOMAIN = 0;
    public static final String NO_NEED_MODEMLOG = "N";
    public static final int PS_DOMAIN = 2;
    public static final int REJ_CS_DOMAIN = 0;
    public static final int REJ_CS_PS_DOMAIN = 2;
    public static final int REJ_PS_DOMAIN = 1;
    public static final String SIM_STATE_DUAL_CARD = "DUAL_CARD";
    public static final String SIM_STATE_MASTER_ONLY = "MASTER_ONLY";
    public static final String SIM_STATE_NO_SIMCARD = "NO_SIMCARD";
    public static final String SIM_STATE_SLAVE_ONLY = "SLAVE_ONLY";
    public static final int SLOT_0 = 0;
    public static final int SLOT_1 = 1;
    public static final String STRING_CDMA = "CDMA";
    public static final String STRING_CDMA_EVDO = "CDMA_EVDO";
    public static final String STRING_CLOUD_OTA_SWITCH_STATE_ANY_NETWORK = "ANY_NETWORK";
    public static final String STRING_CLOUD_OTA_SWITCH_STATE_CLOSED = "CLOSED";
    public static final String STRING_CLOUD_OTA_SWITCH_STATE_ONLY_WIFI = "ONLY_WIFI";
    public static final String STRING_EVDO = "EVDO";
    public static final String STRING_GLOBAL = "GLOBAL";
    public static final String STRING_GSM_ONLY = "GSM_ONLY";
    public static final String STRING_GSM_UMTS = "GSM_UMTS";
    public static final String STRING_LTE_ONLY = "LTE_ONLY";
    public static final String STRING_LTE_WCDMA = "LTE_WCDMA";
    public static final String STRING_L_C_E = "L_C_E";
    public static final String STRING_L_C_E_G_W = "L_C_E_G_W";
    public static final String STRING_L_G_W = "L_G_W";
    public static final String STRING_TDSCDMA_GSM = "TDSCDMA_GSM";
    public static final String STRING_TDSCDMA_LTE = "TDSCDMA_LTE";
    public static final String STRING_TDSCDMA_ONLY = "TDSCDMA_ONLY";
    public static final String STRING_TDSCDMA_WCDMA = "TDSCDMA_WCDMA";
    public static final String STRING_T_C_E_G_W = "T_C_E_G_W";
    public static final String STRING_T_G_L = "T_G_L";
    public static final String STRING_T_G_W = "T_G_W";
    public static final String STRING_T_G_W_L = "T_G_W_L";
    public static final String STRING_T_L_C_E_G_W = "T_L_C_E_G_W";
    public static final String STRING_T_W_L = "T_W_L";
    public static final String STRING_WCDMA_ONLY = "WCDMA_ONLY";
    public static final String STRING_WCDMA_PREF = "WCDMA_PREF";
    public static final int SUB_0 = 0;
    public static final int SUB_1 = 1;
    public static final int SUB_2 = 2;
    public static final int SUB_NUM = 3;
    public static final String UNAVAIBLE_VALUE = "NA";
    public static final String UNKNOWN = "UNKNOWN";
    public static final byte USER_SENSOR_LEVEL_HIGH = (byte) 2;
    public static final byte USER_SENSOR_LEVEL_LOW = (byte) 0;
    public static final byte USER_SENSOR_LEVEL_MEDIUM = (byte) 1;
    public static final short VIDEO_ABNORMAL_INFO_START = (short) 13;
    public static final int VIDEO_ABNORMAL_LOG = 919000007;
    public static final short VIDEO_FWK_INFO_START = (short) 4;
    public static final short VIDEO_INFO_START = (short) 0;
    public static final short VIDEO_NORMAL_INFO_START = (short) 12;
    public static final int VIDEO_NORMAL_LOG = 919000006;
    public static final String VIDEO_TYPE = "video";
    public static final int WEBCHAT_FAILED = -2;
    public static final int WEBCHAT_INVALID = -1;
    public static final String WEBCHAT_TYPE = "wechat";
    public static final int WECHAT_ABNORMAL_LOG = 919000003;
    public static final short WECHAT_ABNORMAL_WLAN_INFO_START = (short) 16;
    public static final short WECHAT_FWK_INFO_START = (short) 4;
    public static final short WECHAT_INFO_ABNORMAL_START = (short) 13;
    public static final short WECHAT_INFO_NORMAL_START = (short) 12;
    public static final int WECHAT_NORMAL_LOG = 919000002;
    public static final short WECHAT_NORMAL_WLAN_INFO_START = (short) 15;
    public static final String YES = "YES";
}
