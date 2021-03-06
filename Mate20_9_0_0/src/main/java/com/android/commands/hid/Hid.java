package com.android.commands.hid;

import android.util.Log;
import android.util.SparseArray;
import com.android.commands.hid.Event.Reader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import libcore.io.IoUtils;

public class Hid {
    private static final String TAG = "HID";
    private final SparseArray<Device> mDevices = new SparseArray();
    private final Reader mReader;

    private static void usage() {
        error("Usage: hid [FILE]");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
            System.exit(1);
        }
        InputStream stream = null;
        try {
            if (args[0].equals("-")) {
                stream = System.in;
            } else {
                stream = new FileInputStream(new File(args[0]));
            }
            new Hid(stream).run();
        } catch (Exception e) {
            error("HID injection failed.", e);
            System.exit(1);
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
        }
        IoUtils.closeQuietly(stream);
    }

    private Hid(InputStream in) {
        try {
            this.mReader = new Reader(new InputStreamReader(in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void run() {
        while (true) {
            try {
                Event nextEvent = this.mReader.getNextEvent();
                Event e = nextEvent;
                if (nextEvent == null) {
                    break;
                }
                process(e);
            } catch (IOException ex) {
                error("Error reading in events.", ex);
            }
        }
        for (int i = 0; i < this.mDevices.size(); i++) {
            ((Device) this.mDevices.valueAt(i)).close();
        }
    }

    private void process(Event e) {
        int index = this.mDevices.indexOfKey(e.getId());
        if (index >= 0) {
            Device d = (Device) this.mDevices.valueAt(index);
            StringBuilder stringBuilder;
            if (Event.COMMAND_DELAY.equals(e.getCommand())) {
                d.addDelay(e.getDuration());
            } else if (Event.COMMAND_REPORT.equals(e.getCommand())) {
                d.sendReport(e.getReport());
            } else if (Event.COMMAND_REGISTER.equals(e.getCommand())) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Device id=");
                stringBuilder.append(e.getId());
                stringBuilder.append(" is already registered. Ignoring event.");
                error(stringBuilder.toString());
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown command \"");
                stringBuilder.append(e.getCommand());
                stringBuilder.append("\". Ignoring event.");
                error(stringBuilder.toString());
            }
        } else if (Event.COMMAND_REGISTER.equals(e.getCommand())) {
            registerDevice(e);
        } else {
            Log.e(TAG, "Unknown device id specified. Ignoring event.");
        }
    }

    private void registerDevice(Event e) {
        if (Event.COMMAND_REGISTER.equals(e.getCommand())) {
            int id = e.getId();
            this.mDevices.append(id, new Device(id, e.getName(), e.getVendorId(), e.getProductId(), e.getDescriptor(), e.getReport()));
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Tried to send command \"");
        stringBuilder.append(e.getCommand());
        stringBuilder.append("\" to an unregistered device!");
        throw new IllegalStateException(stringBuilder.toString());
    }

    private static void error(String msg) {
        error(msg, null);
    }

    private static void error(String msg, Exception e) {
        Log.e(TAG, msg);
        if (e != null) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
