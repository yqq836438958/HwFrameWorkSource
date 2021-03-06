package com.android.server.wifi.ABS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

public class HwABSModemScenario {
    public static final String ACTION_HW_CRR_CONN_IND = "com.huawei.action.ACTION_HW_CRR_CONN_IND";
    public static final String HUAWEI_SIM_REG_PLMNSELINFO_ACTION = "com.huawei.action.SIM_PLMN_SELINFO";
    private static final int MODEM_EXCEPTION_INTERVAL_TIME = 10000;
    private static final int MODEM_EXCEPTION_NUM = 3;
    private static final int MODEM_EXCEPTION_REPORT_TIME = 2000;
    private static final int MSG_ENTER_CONNECT_STATE = 6;
    private static final int MSG_ENTER_SEARCH_STATE = 1;
    private static final int MSG_EXIT_CONNECT_STATE = 7;
    private static final int MSG_EXIT_SEARCH_STATE = 2;
    private static String MSG_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
    private static final int MSG_REPORT_ENTER_CONNECT_STATE = 8;
    private static final int MSG_REPORT_ENTER_SEARCH_STATE_MODEM_0 = 3;
    private static final int MSG_REPORT_ENTER_SEARCH_STATE_MODEM_1 = 4;
    private static final int MSG_REPORT_ENTER_SEARCH_STATE_MODEM_2 = 5;
    private IntentFilter intentFilter = new IntentFilter();
    PhoneStateListener listener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case 0:
                    HwABSUtils.logD("CALL_STATE_IDLE");
                    HwABSModemScenario.this.mHandler.sendEmptyMessage(8);
                    return;
                case 1:
                    HwABSUtils.logD("CALL_STATE_RINGING");
                    HwABSModemScenario.this.mHandler.sendEmptyMessage(9);
                    return;
                case 2:
                    HwABSUtils.logD("CALL_STATE_OFFHOOK");
                    HwABSModemScenario.this.mHandler.sendEmptyMessage(10);
                    return;
                default:
                    return;
            }
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            int voiceState = serviceState.getState();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onServiceStateChanged  voiceState= ");
            stringBuilder.append(voiceState);
            HwABSUtils.logD(stringBuilder.toString());
            if (voiceState != 3) {
                switch (voiceState) {
                    case 0:
                        HwABSModemScenario.this.mHandler.sendEmptyMessage(25);
                        return;
                    default:
                        return;
                }
            }
            HwABSModemScenario.this.mHandler.sendEmptyMessage(22);
        }
    };
    private Context mContext;
    private long mEnterConnectState = 0;
    private int mEnterConnectStateNum = 0;
    private long mEnterSearchState = 0;
    private int mEnterSearchStateNum = 0;
    private Handler mHandler;
    private BroadcastReceiver mModemBroadcastReceiver = new ModemBroadcastReceiver(this, null);
    private Handler mProcessHandler = new Handler() {
        Message enterMsg = null;
        Bundle mData = null;
        int mSubID = -1;

        public void handleMessage(Message msg) {
            StringBuilder stringBuilder;
            switch (msg.what) {
                case 1:
                    HwABSUtils.logD("MSG_ENTER_SEARCH_STATE");
                    this.mData = msg.getData();
                    this.enterMsg = new Message();
                    this.enterMsg.setData(this.mData);
                    if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterSearchState > 10000) {
                        HwABSModemScenario.this.mEnterSearchStateNum = 0;
                        HwABSModemScenario.this.mEnterSearchState = System.currentTimeMillis();
                        this.mSubID = this.mData.getInt(HwABSUtils.SUB_ID);
                        switch (this.mSubID) {
                            case 0:
                                this.enterMsg.what = 3;
                                break;
                            case 1:
                                this.enterMsg.what = 4;
                                break;
                            case 2:
                                this.enterMsg.what = 5;
                                break;
                            default:
                                return;
                        }
                        sendMessageDelayed(this.enterMsg, 2000);
                        break;
                    }
                    HwABSModemScenario.this.mEnterSearchStateNum = HwABSModemScenario.this.mEnterSearchStateNum + 1;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("MSG_ENTER_SEARCH_STATE mEnterSearchStateNum = ");
                    stringBuilder.append(HwABSModemScenario.this.mEnterSearchStateNum);
                    HwABSUtils.logD(stringBuilder.toString());
                    if (HwABSModemScenario.this.mEnterSearchStateNum >= 3) {
                        this.enterMsg.what = 14;
                        HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                        break;
                    }
                    break;
                case 2:
                    this.mData = msg.getData();
                    this.enterMsg = new Message();
                    this.enterMsg.setData(this.mData);
                    this.enterMsg.what = 15;
                    this.mSubID = this.mData.getInt(HwABSUtils.SUB_ID);
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("MSG_EXIT_SEARCH_STATE mSubID = ");
                    stringBuilder.append(this.mSubID);
                    HwABSUtils.logD(stringBuilder.toString());
                    switch (this.mSubID) {
                        case 0:
                            removeMessages(3);
                            break;
                        case 1:
                            removeMessages(4);
                            break;
                        case 2:
                            removeMessages(5);
                            break;
                        default:
                            return;
                    }
                    HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                    break;
                case 3:
                case 4:
                case 5:
                    HwABSUtils.logD("MSG_REPORT_ENTER_SEARCH_STATE");
                    this.mData = msg.getData();
                    this.enterMsg = new Message();
                    this.enterMsg.setData(this.mData);
                    this.enterMsg.what = 14;
                    HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                    break;
                case 6:
                    HwABSUtils.logD("MSG_ENTER_CONNECT_STATE");
                    if (HwABSModemScenario.this.mEnterConnectState != 0) {
                        if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterConnectState > 10000) {
                            HwABSModemScenario.this.mEnterConnectStateNum = 0;
                            HwABSModemScenario.this.mEnterConnectState = System.currentTimeMillis();
                            HwABSModemScenario.this.mProcessHandler.sendEmptyMessageDelayed(8, 2000);
                            break;
                        }
                        HwABSModemScenario.this.mEnterConnectStateNum = HwABSModemScenario.this.mEnterConnectStateNum + 1;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("MSG_ENTER_CONNECT_STATE mEnterConnectStateNum = ");
                        stringBuilder.append(HwABSModemScenario.this.mEnterConnectStateNum);
                        HwABSUtils.logD(stringBuilder.toString());
                        if (HwABSModemScenario.this.mEnterConnectStateNum >= 3) {
                            HwABSModemScenario.this.mHandler.sendEmptyMessage(12);
                            break;
                        }
                    }
                    HwABSModemScenario.this.mEnterConnectState = System.currentTimeMillis();
                    HwABSModemScenario.this.mProcessHandler.sendEmptyMessageDelayed(8, 2000);
                    break;
                    break;
                case 7:
                    HwABSUtils.logD("ACTION_ABS_EXIT_CONNECT");
                    if (System.currentTimeMillis() - HwABSModemScenario.this.mEnterConnectState < 2000) {
                        HwABSModemScenario.this.mProcessHandler.removeMessages(8);
                    }
                    HwABSModemScenario.this.mHandler.sendEmptyMessage(13);
                    break;
                case 8:
                    HwABSUtils.logD("MSG_REPORT_ENTER_CONNECT_STATE");
                    this.mData = msg.getData();
                    this.enterMsg = new Message();
                    this.enterMsg.what = 11;
                    this.enterMsg.setData(this.mData);
                    HwABSModemScenario.this.mHandler.sendMessage(this.enterMsg);
                    break;
            }
        }
    };
    private TelephonyManager mTelephonyManager;

    private class ModemBroadcastReceiver extends BroadcastReceiver {
        private ModemBroadcastReceiver() {
        }

        /* synthetic */ ModemBroadcastReceiver(HwABSModemScenario x0, AnonymousClass1 x1) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int subId;
            int result;
            if (HwABSModemScenario.HUAWEI_SIM_REG_PLMNSELINFO_ACTION.equals(action)) {
                subId = intent.getIntExtra(HwABSUtils.SUB_ID, 0);
                int flag = intent.getIntExtra(HwABSUtils.FLAG, 0);
                result = intent.getIntExtra(HwABSUtils.RES, 0);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("HUAWEI_SIM_REG_PLMNSELINFO_ACTION subId = ");
                stringBuilder.append(subId);
                stringBuilder.append(" flag =");
                stringBuilder.append(flag);
                stringBuilder.append(" result =");
                stringBuilder.append(result);
                HwABSUtils.logD(stringBuilder.toString());
                Bundle data = new Bundle();
                data.putInt(HwABSUtils.SUB_ID, subId);
                data.putInt(HwABSUtils.FLAG, flag);
                data.putInt(HwABSUtils.RES, result);
                Message msg = new Message();
                if (flag == 0) {
                    msg.what = 1;
                } else {
                    msg.what = 2;
                }
                msg.setData(data);
                HwABSModemScenario.this.mProcessHandler.sendMessage(msg);
            } else if (HwABSModemScenario.ACTION_HW_CRR_CONN_IND.equals(action)) {
                subId = intent.getIntExtra(HwABSUtils.MODEM0, 0);
                int modem1 = intent.getIntExtra(HwABSUtils.MODEM1, 0);
                result = intent.getIntExtra(HwABSUtils.MODEM2, 0);
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("ACTION_HW_CRR_CONN_IND modem0 = ");
                stringBuilder2.append(subId);
                stringBuilder2.append(" modem1 =");
                stringBuilder2.append(modem1);
                stringBuilder2.append(" modem2 =");
                stringBuilder2.append(result);
                HwABSUtils.logD(stringBuilder2.toString());
                if (subId == 0 && modem1 == 0 && result == 0) {
                    HwABSModemScenario.this.mProcessHandler.sendEmptyMessage(7);
                } else {
                    HwABSModemScenario.this.mProcessHandler.sendEmptyMessage(6);
                }
            } else if (HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED.equals(action)) {
                HwABSModemScenario.this.mHandler.sendEmptyMessage(16);
            } else if ("android.intent.action.NEW_OUTGOING_CALL".equals(action)) {
                HwABSUtils.logD("MSG_OUTGOING_CALL");
                HwABSModemScenario.this.mHandler.sendEmptyMessage(7);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwABSUtils.logD("ACTION_BOOT_COMPLETED");
                HwABSModemScenario.this.mHandler.sendEmptyMessage(37);
                HwABSModemScenario.this.mTelephonyManager.listen(HwABSModemScenario.this.listener, 33);
            }
        }
    }

    public HwABSModemScenario(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        HwABSUtils.logD("registerBroadcastReceiver");
        this.intentFilter.addAction(MSG_OUTGOING_CALL);
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.intentFilter.addAction(HUAWEI_SIM_REG_PLMNSELINFO_ACTION);
        this.intentFilter.addAction(ACTION_HW_CRR_CONN_IND);
        this.intentFilter.addAction(HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED);
        this.mContext.registerReceiver(this.mModemBroadcastReceiver, this.intentFilter);
    }
}
