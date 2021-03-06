package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanSettings;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.wifipro.HwAutoConnectManager;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HwWifiConnectivityManager extends WifiConnectivityManager {
    private static final int DEFAULT_SCAN_PERIOD_SKIP_COUNTER = 4;
    private static final int DEFAULT_SCAN_PERIOD_SKIP_COUNTER_FOR_CLONE = 50;
    private static final int HW_MAX_PERIODIC_SCAN_INTERVAL_MS = 60000;
    private static final int HW_MAX_STATIONARY_PERIODIC_SCAN_INTERVAL_MS = 300000;
    private static final int HW_MID_PERIODIC_SCAN_INTERVAL_MS = 30000;
    private static final int HW_MIX_PERIODIC_SCAN_INTERVAL_MS = 10000;
    private static final int HW_NAVIGATION_PERIODIC_SCAN_INTERVAL_MS = 120000;
    private static final String PG_AR_STATE_ACTION = "com.huawei.intent.action.PG_AR_STATE_ACTION";
    private static final String PG_RECEIVER_PERMISSION = "com.huawei.powergenie.receiverPermission";
    private static final int SCAN_COUNT_CHANGE_REASON_ADD = 0;
    private static final int SCAN_COUNT_CHANGE_REASON_MINUS = 1;
    private static final int SCAN_COUNT_CHANGE_REASON_RESET = 2;
    private static final int STATE_GPS = 3;
    private static final String TAG = "HwWifiConnectivityManager";
    private static WifiStateMachineUtils wifiStateMachineUtils = ((WifiStateMachineUtils) EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class));
    private boolean bExtendWifiScanPeriodForP2p = false;
    private int iScanPeriodSkipTimes = 4;
    private Context mContext;
    private int mExponentialPeriodicSingleScanInterval;
    private boolean mExtendWifiScanPeriodForClone = false;
    private int mHwSingleScanCounter = 0;
    private boolean mIsStationary = false;
    private int mPeriodicSingleScanInterval;
    private WifiScanGenieController mWifiScanGenieController;
    private int mWifiScanPeriodCounter = 0;
    private WifiStateMachine mWifiStateMachine;

    public HwWifiConnectivityManager(Context context, ScoringParams scoringParams, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog, boolean enable, FrameworkFacade frameworkFacade, SavedNetworkEvaluator savedNetworkEvaluator, ScoredNetworkEvaluator scoredNetworkEvaluator, PasspointNetworkEvaluator passpointNetworkEvaluator) {
        Context context2 = context;
        super(context, scoringParams, stateMachine, scanner, configManager, wifiInfo, networkSelector, connectivityHelper, wifiLastResortWatchdog, openNetworkNotifier, carrierNetworkNotifier, carrierNetworkConfig, wifiMetrics, looper, clock, localLog, enable, frameworkFacade, savedNetworkEvaluator, scoredNetworkEvaluator, passpointNetworkEvaluator);
        this.mContext = context2;
        this.mWifiScanGenieController = WifiScanGenieController.createWifiScanGenieControllerImpl(this.mContext);
        this.mWifiStateMachine = stateMachine;
        if (PreconfiguredNetworkManager.IS_R1) {
            this.mNetworkSelector.registerNetworkEvaluator(new PreconfiguredNetworkEvaluator(context2, configManager), 4);
        } else {
            WifiConfigManager wifiConfigManager = configManager;
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    boolean stationary = intent.getBooleanExtra("stationary", false);
                    String str = HwWifiConnectivityManager.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Current stationary = ");
                    stringBuilder.append(HwWifiConnectivityManager.this.mIsStationary);
                    stringBuilder.append(", new stationary = ");
                    stringBuilder.append(stationary);
                    Log.d(str, stringBuilder.toString());
                    if (stationary != HwWifiConnectivityManager.this.mIsStationary) {
                        HwWifiConnectivityManager.this.mIsStationary = stationary;
                        if (HwWifiConnectivityManager.this.mWifiStateMachine == null || HwWifiConnectivityManager.wifiStateMachineUtils.getScreenOn(HwWifiConnectivityManager.this.mWifiStateMachine)) {
                            HwWifiConnectivityManager.this.startConnectivityScan(false, false);
                        } else {
                            Log.d(HwWifiConnectivityManager.TAG, "PG_AR_STATE_ACTION ScreenOff do nothing return !");
                        }
                    }
                }
            }
        }, new IntentFilter(PG_AR_STATE_ACTION), PG_RECEIVER_PERMISSION, null);
        log("HwWifiConnectivityManager init!");
    }

    public String unselectDhcpFailedBssid(String targetBssid, String scanResultBssid, WifiConfiguration candidate) {
        if (HwSelfCureEngine.getInstance() == null || candidate == null) {
            return targetBssid;
        }
        if (scanResultBssid != null && HwSelfCureEngine.getInstance().isDhcpFailedConfigKey(candidate.configKey())) {
            return scanResultBssid;
        }
        if (scanResultBssid != null && HwAutoConnectManager.getInstance() != null && HwAutoConnectManager.getInstance().isPortalNotifyOn()) {
            return scanResultBssid;
        }
        if (scanResultBssid == null || HwAutoConnectManager.getInstance() == null || !HwAutoConnectManager.getInstance().isAutoJoinAllowedSetTargetBssid(candidate, scanResultBssid)) {
            return targetBssid;
        }
        return scanResultBssid;
    }

    protected void extendWifiScanPeriodForP2p(boolean bExtend, int iTimes) {
        this.mWifiScanPeriodCounter = 0;
        this.bExtendWifiScanPeriodForP2p = bExtend;
        if (!bExtend || iTimes <= 0) {
            this.iScanPeriodSkipTimes = 4;
            this.mExtendWifiScanPeriodForClone = false;
        } else {
            this.iScanPeriodSkipTimes = iTimes;
        }
        if (iTimes == 50) {
            this.mExtendWifiScanPeriodForClone = true;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("extendWifiScanPeriodForP2p: ");
        stringBuilder.append(this.bExtendWifiScanPeriodForP2p);
        stringBuilder.append(", Times = ");
        stringBuilder.append(this.iScanPeriodSkipTimes);
        log(stringBuilder.toString());
    }

    protected boolean isScanThisPeriod(boolean isP2pConn) {
        if (!isP2pConn && !this.mExtendWifiScanPeriodForClone) {
            if (this.mWifiScanPeriodCounter > 0) {
                this.bExtendWifiScanPeriodForP2p = false;
                this.mWifiScanPeriodCounter = 0;
                this.iScanPeriodSkipTimes = 4;
            }
            return true;
        } else if (!this.bExtendWifiScanPeriodForP2p) {
            return true;
        } else {
            this.mWifiScanPeriodCounter++;
            if (this.mExtendWifiScanPeriodForClone) {
                if (this.mWifiScanPeriodCounter < this.iScanPeriodSkipTimes) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("isScanThisPeriod: false for clone mWifiScanPeriodCounter is: ");
                    stringBuilder.append(this.mWifiScanPeriodCounter);
                    log(stringBuilder.toString());
                    return false;
                }
                this.bExtendWifiScanPeriodForP2p = false;
                this.mWifiScanPeriodCounter = 0;
                this.iScanPeriodSkipTimes = 4;
                this.mExtendWifiScanPeriodForClone = false;
                return true;
            } else if (this.mWifiScanPeriodCounter % this.iScanPeriodSkipTimes == 0) {
                return true;
            } else {
                log("isScanThisPeriod: false");
                return false;
            }
        }
    }

    private int getScanGeniePeriodicSingleScanInterval() {
        if (this.mHwSingleScanCounter < 4) {
            this.mPeriodicSingleScanInterval = HW_MIX_PERIODIC_SCAN_INTERVAL_MS;
        } else if (this.mHwSingleScanCounter < 7) {
            this.mPeriodicSingleScanInterval = 30000;
        } else {
            if (checkProduct() && checkNavigationMode()) {
                this.mPeriodicSingleScanInterval = 120000;
            } else {
                this.mPeriodicSingleScanInterval = 60000;
            }
            if (this.mIsStationary && shouldDisconnectScanControl()) {
                this.mPeriodicSingleScanInterval = 300000;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HwSingleScanCounter: ");
        stringBuilder.append(this.mHwSingleScanCounter);
        stringBuilder.append(", mPeriodicSingleScanInterval : ");
        stringBuilder.append(this.mPeriodicSingleScanInterval);
        stringBuilder.append(" ms");
        log(stringBuilder.toString());
        return this.mPeriodicSingleScanInterval;
    }

    private boolean checkProduct() {
        ArrayList<String> productList = new ArrayList(Arrays.asList(new String[]{"VTR", "VKY", "MHA", "LON", "EML", "CLT", "NEO", "RVL"}));
        for (int k = 0; k < productList.size(); k++) {
            if (SystemProperties.get("ro.product.name", "").contains((CharSequence) productList.get(k))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNavigationMode() {
        ArrayList<String> navigationPackages = new ArrayList(Arrays.asList(new String[]{"com.autonavi.minimap", "com.baidu.BaiduMap", "com.autonavi.xmgd.navigator", "com.tencent.map", "com.ovital.ovitalMap", "com.google.android.apps.maps", "com.baidu.navi", "cld.navi.mainframe", "com.sogou.map.android.maps", "com.uu.uunavi", "com.sunboxsoft.oilforgdandroid", "com.pdager", "com.itotem.traffic.broadcasts", "com.mapbar.android.mapbarmap", "com.autonavi.xmgd.navigator.toc", "cn.com.tiros.android.navidog", "com.autonavi.minimap.custom", "com.autonavi.cmccmap", "com.baidu.BaiduMap.pad", "com.baidu.carlife", "com.tigerknows", "com.erlinyou.worldlist", "com.uu.uueeye", "com.mapbar.android.trybuynavi", "com.zhituo.gpslocation", "com.waze", "ru.yandex.yandexnavi"}));
        PGSdk mPGSdk = PGSdk.getInstance();
        if (mPGSdk == null) {
            return false;
        }
        boolean state = false;
        int k = 0;
        while (true) {
            int k2 = k;
            if (k2 >= navigationPackages.size()) {
                return false;
            }
            try {
                state = mPGSdk.checkStateByPkg(this.mContext, (String) navigationPackages.get(k2), 3);
            } catch (RemoteException e) {
                log("checkStateByPkg occur exception.");
            }
            if (state) {
                return true;
            }
            k = k2 + 1;
        }
    }

    protected int getPeriodicSingleScanInterval() {
        if (!isSupportWifiScanGenie() || this.mWifiState == 1) {
            log("****isSupportWifiScanGenie :  fasle: ");
            this.mExponentialPeriodicSingleScanInterval *= 2;
            int maxInterval = 160000;
            if (this.mIsStationary) {
                maxInterval = 300000;
            }
            if (this.mExponentialPeriodicSingleScanInterval > maxInterval) {
                this.mExponentialPeriodicSingleScanInterval = maxInterval;
            }
            return this.mExponentialPeriodicSingleScanInterval;
        }
        log("****isSupportWifiScanGenie :  true: ");
        return getScanGeniePeriodicSingleScanInterval();
    }

    protected void resetPeriodicSingleScanInterval() {
        this.mExponentialPeriodicSingleScanInterval = 20000;
        handleScanCountChanged(2);
    }

    protected void handleSingleScanFailure(int reason) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("handleSingleScanFailure reason ");
        stringBuilder.append(reason);
        log(stringBuilder.toString());
        handleScanCountChanged(1);
    }

    protected void handleSingleScanSuccess() {
    }

    protected void handleScanCountChanged(int reason) {
        if (reason == 0) {
            this.mHwSingleScanCounter++;
            this.mWifiScanGenieController.notifyUseFullChannels();
        } else if (1 == reason) {
            if (this.mHwSingleScanCounter > 0) {
                this.mHwSingleScanCounter--;
            }
        } else if (2 == reason) {
            this.mHwSingleScanCounter = 0;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("handleScanCounterChanged,reason: ");
        stringBuilder.append(reason);
        stringBuilder.append(", mHwSingleScanCounter: ");
        stringBuilder.append(this.mHwSingleScanCounter);
        log(stringBuilder.toString());
    }

    protected boolean isSupportWifiScanGenie() {
        return true;
    }

    protected boolean isWifiScanSpecialChannels() {
        if (!isSupportWifiScanGenie()) {
            return false;
        }
        if (this.mHwSingleScanCounter <= 1) {
            return true;
        }
        this.mWifiScanGenieController.notifyUseFullChannels();
        return false;
    }

    protected ScanSettings getScanGenieSettings() {
        return getHwScanSettings();
    }

    protected boolean handleForceScan() {
        return false;
    }

    private ScanSettings getHwScanSettings() {
        List<Integer> fusefrequencyList = this.mWifiScanGenieController.getScanfrequencys();
        if (fusefrequencyList == null || fusefrequencyList.size() == 0) {
            log("getHwScanSettings,fusefrequencyList is null:");
            return null;
        }
        ScanSettings settings = new ScanSettings();
        int i = 0;
        settings.band = 0;
        settings.reportEvents = 3;
        settings.numBssidsPerScan = 0;
        ChannelSpec[] channels = new ChannelSpec[fusefrequencyList.size()];
        while (i < fusefrequencyList.size()) {
            channels[i] = new ChannelSpec(((Integer) fusefrequencyList.get(i)).intValue());
            i++;
        }
        settings.channels = channels;
        return settings;
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    private boolean shouldDisconnectScanControl() {
        if (this.mWifiStateMachine == null || !this.mWifiStateMachine.isPortalConnectLast() || this.mHwSingleScanCounter >= 14) {
            return true;
        }
        log("last disconnected network is portal, delay scan control");
        return false;
    }
}
