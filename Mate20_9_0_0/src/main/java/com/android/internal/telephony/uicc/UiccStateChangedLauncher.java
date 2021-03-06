package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;

public class UiccStateChangedLauncher extends Handler {
    private static final int EVENT_ICC_CHANGED = 1;
    private static final String TAG = UiccStateChangedLauncher.class.getName();
    private static String sDeviceProvisioningPackage = null;
    private Context mContext;
    private boolean[] mIsRestricted = null;
    private UiccController mUiccController;

    public UiccStateChangedLauncher(Context context, UiccController controller) {
        sDeviceProvisioningPackage = context.getResources().getString(17039794);
        if (sDeviceProvisioningPackage != null && !sDeviceProvisioningPackage.isEmpty()) {
            this.mContext = context;
            this.mUiccController = controller;
            this.mUiccController.registerForIccChanged(this, 1, null);
        }
    }

    public void handleMessage(Message msg) {
        if (msg.what == 1) {
            boolean shouldNotify = false;
            if (this.mIsRestricted == null) {
                this.mIsRestricted = new boolean[TelephonyManager.getDefault().getPhoneCount()];
                shouldNotify = true;
            }
            boolean shouldNotify2 = shouldNotify;
            for (int i = 0; i < this.mIsRestricted.length; i++) {
                UiccCard uiccCard = this.mUiccController.getUiccCardForPhone(i);
                boolean z = uiccCard == null || uiccCard.getCardState() != CardState.CARDSTATE_RESTRICTED;
                if (z != this.mIsRestricted[i]) {
                    this.mIsRestricted[i] = this.mIsRestricted[i] ^ 1;
                    shouldNotify2 = true;
                }
            }
            if (shouldNotify2) {
                notifyStateChanged();
                return;
            }
            return;
        }
        throw new RuntimeException("unexpected event not handled");
    }

    private void notifyStateChanged() {
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.setPackage(sDeviceProvisioningPackage);
        try {
            this.mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
