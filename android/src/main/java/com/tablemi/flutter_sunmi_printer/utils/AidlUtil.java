package com.tablemi.flutter_sunmi_printer.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.sunmi.pay.hardware.aidl.print.PrinterCallback;

import com.tablemi.flutter_sunmi_printer.entities.TableItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

public class AidlUtil {
    private static final String SERVICE＿PACKAGE = "woyou.aidlservice.jiuiv5";
    private static final String SERVICE＿ACTION = "woyou.aidlservice.jiuiv5.IWoyouService";

    private IWoyouService woyouService;
    private static AidlUtil mAidlUtil = new AidlUtil();
    private static final int LINE_BYTE_SIZE = 32;
    private Context context;

    private AidlUtil() {
    }

    public static AidlUtil getInstance() {
        return mAidlUtil;
    }

    /**
     * 连接服务
     *
     * @param context context
     */
    public void connectPrinterService(Context context) {
        this.context = context.getApplicationContext();
        Intent intent = new Intent();
        intent.setPackage(SERVICE＿PACKAGE);
        intent.setAction(SERVICE＿ACTION);
        context.getApplicationContext().startService(intent);
        context.getApplicationContext().bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    /**
     * 断开服务
     *
     * @param context context
     */
    public void disconnectPrinterService(Context context) {
        if (woyouService != null) {
            context.getApplicationContext().unbindService(connService);
            woyouService = null;
        }
    }

    public boolean isConnect() {
        return woyouService != null;
    }

    private ServiceConnection connService = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            woyouService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            woyouService = IWoyouService.Stub.asInterface(service);
        }
    };

    public ICallback generateCB(final PrinterCallback printerCallback) {
        return new ICallback.Stub() {

            @Override
            public void onRunResult(boolean isSuccess) {

            }

            @Override
            public void onReturnString(String result) {
            }

            @Override
            public void onRaiseException(int code, String msg) {

            }

            @Override
            public void onPrintResult(int code, String msg) {

            }
        };
    }

    /**
     * 设置打印浓度
     */
    private int[] darkness = new int[] { 0x0600, 0x0500, 0x0400, 0x0300, 0x0200, 0x0100, 0, 0xffff, 0xfeff, 0xfdff,
            0xfcff, 0xfbff, 0xfaff };

    public void setDarkness(int index) {
        if (woyouService == null) {
            return;
        }

        int k = darkness[index];
        try {
            woyouService.sendRAWData(ESCUtil.setPrinterDarkness(k), null);
            woyouService.printerSelfChecking(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取得打印机系统信息，放在list中
     *
     * @return list
     */
    public List<String> getPrinterInfo(PrinterCallback printerCallback1, PrinterCallback printerCallback2) {
        if (woyouService == null) {
            return null;
        }

        List<String> info = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        try {
            if(printerCallback1 != null) woyouService.getPrintedLength(generateCB(printerCallback1));
            if(printerCallback2 != null) woyouService.getPrinterFactory(generateCB(printerCallback2));
            info.add(woyouService.getPrinterSerialNo());
            info.add(woyouService.getPrinterModal());
            info.add(woyouService.getPrinterVersion());
            PackageInfo packageInfo = packageManager.getPackageInfo(SERVICE＿PACKAGE, 0);
            if (packageInfo != null) {
                info.add(packageInfo.versionName);
                info.add(packageInfo.versionCode + "");
            } else {
                info.add("");
                info.add("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    /**
     * 初始化打印机
     */
    public void initPrinter() {
        if (woyouService == null) {
            return;
        }

        try {
            woyouService.printerInit(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setFontSize(int size) {
        try {
            woyouService.setFontSize(size, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印二维码
     */
    public void printQr(String data, int modulesize, int errorlevel) {
        if (woyouService == null) {
            return;
        }

        try {
            woyouService.setAlignment(1, null);
            woyouService.printQRCode(data, modulesize, errorlevel, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印条形码
     */
    public void printBarCode(String data, int symbology, int height, int width, int textposition) {
        if (woyouService == null) {
            return;
        }
        try {
            woyouService.printBarCode(data, symbology, height, width, textposition, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印文字
     */
    public void printText(String content, float size, boolean isBold, boolean isUnderLine) {
        if (woyouService == null) {
            return;
        }
        try {
            if (isBold) {
                woyouService.sendRAWData(ESCUtil.boldOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.boldOff(), null);
            }

            if (isUnderLine) {
                woyouService.sendRAWData(ESCUtil.underlineWithOneDotWidthOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.underlineOff(), null);
            }

            woyouService.printTextWithFont(content, null, size, null);
            // woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*
     * 打印图片
     */
    public void printBitmap(Bitmap bitmap, int align) {
        if (woyouService == null) {
            return;
        }

        try {
            woyouService.setAlignment(align, null);
            woyouService.printBitmap(bitmap, null);
            woyouService.lineWrap(1, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void lineWrap(int line) {
        try {
            woyouService.lineWrap(line, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印图片和文字按照指定排列顺序
     */
    public void printBitmap2(Bitmap bitmap, int orientation) {
        if (woyouService == null) {
            Toast.makeText(context, "服务已断开！", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (orientation == 0) {
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("横向排列\n", null);
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("横向排列\n", null);
            } else {
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("\n纵向排列\n", null);
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("\n纵向排列\n", null);
            }
            woyouService.lineWrap(3, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印表格
     */
    public void printTable(LinkedList<TableItem> list) {
        if (woyouService == null) {
            return;
        }
        try {
            for (TableItem tableItem : list) {
                woyouService.printColumnsString(tableItem.getText(), tableItem.getWidth(), tableItem.getAlign(), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印表格项
     */
    public void printTableItem(String[] text, int[] width, int[] align) {
        if (woyouService == null) {
            return;
        }
        try {
            woyouService.printColumnsString(text, width, align, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*
     * 空打三行！
     */
    public void print3Line() {
        if (woyouService == null) {
            return;
        }

        try {
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendRawData(byte[] data) {
        if (woyouService == null) {
            return;
        }

        try {
            woyouService.sendRAWData(data, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendRawDatabyBuffer(byte[] data, ICallback iCallback) {
        if (woyouService == null) {
            return;
        }

        try {
            woyouService.enterPrinterBuffer(true);
            woyouService.sendRAWData(data, iCallback);
            woyouService.exitPrinterBufferWithCallback(true, iCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
