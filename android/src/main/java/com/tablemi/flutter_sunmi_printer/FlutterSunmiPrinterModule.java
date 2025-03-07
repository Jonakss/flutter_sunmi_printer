package com.tablemi.flutter_sunmi_printer;

import android.content.Context;
import android.util.Log;

import com.tablemi.flutter_sunmi_printer.utils.AidlUtil;
import com.tablemi.flutter_sunmi_printer.utils.Base64Utils;
import com.tablemi.flutter_sunmi_printer.utils.BitmapUtil;
import com.tablemi.flutter_sunmi_printer.utils.ESCUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FlutterSunmiPrinterModule {

  private static boolean isPrinting = false;
  public static int DEFAULT_FONT_SIZE = 24;

  public void initAidl(Context context) {
    AidlUtil.getInstance().connectPrinterService(context);
    AidlUtil.getInstance().initPrinter();
  }

  public void reset() {
    AidlUtil.getInstance().initPrinter();
  }

  public void startPrint() {
    isPrinting = true;
  }

  public void stopPrint() {
    isPrinting = false;
  }

  public boolean isPrinting() {
    return isPrinting;
  }

  public void boldOn() {
    AidlUtil.getInstance().sendRawData(ESCUtil.boldOn());
  }

  public void boldOff() {
    AidlUtil.getInstance().sendRawData(ESCUtil.boldOff());
  }

  public void underlineOn() {
    AidlUtil.getInstance().sendRawData(ESCUtil.underlineWithOneDotWidthOn());
  }

  public void underlineOff() {
    AidlUtil.getInstance().sendRawData(ESCUtil.underlineOff());
  }

  public void emptyLines(int n) {
    AidlUtil.getInstance().lineWrap(n);
  }

  public void setFontSize(int size) {
    AidlUtil.getInstance().setFontSize(size);
  }

  public String getPrinterStatus(){
    List<String> list = AidlUtil.getInstance().getPrinterInfo(null, null);
    return list.get(0);
  };

  public void text(String text, int align, boolean bold, boolean underline, int size, int linesAfter) {
    // Set styles
    if (bold) {
      boldOn();
    }
    if (underline) {
      underlineOn();
    }

    // Print text
    setFontSize(size);
    AidlUtil.getInstance().printTableItem(new String[] { text }, new int[] { 32 }, new int[] { align });
    if (linesAfter > 0) {
      emptyLines(linesAfter);
    }
    setFontSize(DEFAULT_FONT_SIZE);

    // Reset styles
    if (bold) {
      boldOff();
    }
    if (underline) {
      underlineOff();
    }
  }

  public void row(String colsStr, boolean bold, boolean underline, int textSize, int linesAfter) {
    try {
      // Set styles
      if (bold) {
        boldOn();
      }
      if (underline) {
        underlineOn();
      }

      // Prepare row data
      JSONArray cols = new JSONArray(colsStr);
      String[] colsText = new String[cols.length()];
      int[] colsWidth = new int[cols.length()];
      int[] colsAlign = new int[cols.length()];
      for (int i = 0; i < cols.length(); i++) {
        JSONObject col = cols.getJSONObject(i);
        String text = col.getString("text");
        int width = col.getInt("width");
        int align = col.getInt("align");
        colsText[i] = text;
        colsWidth[i] = width;
        colsAlign[i] = align;
      }

      // Print row
      setFontSize(textSize);
      AidlUtil.getInstance().printTableItem(colsText, colsWidth, colsAlign);
      if (linesAfter > 0) {
        emptyLines(linesAfter);
      }
      setFontSize(DEFAULT_FONT_SIZE);

      // Reset styles
      if (bold) {
        boldOff();
      }
      if (underline) {
        underlineOff();
      }
    } catch (Exception err) {
      Log.d("SunmiPrinter", err.getMessage());
    }
  }

  public void printImage(String base64, int align) {
    byte[] bytes = Base64Utils.decode(base64);
    for (int i = 0; i < bytes.length; ++i) {
      // ajust data
      if (bytes[i] < 0) {
        bytes[i] += 256;
      }
    }
    AidlUtil.getInstance().printBitmap(BitmapUtil.convertToThumb(bytes, 280), align);
    // AidlUtil.getInstance().lineWrap(1);
  }
}
