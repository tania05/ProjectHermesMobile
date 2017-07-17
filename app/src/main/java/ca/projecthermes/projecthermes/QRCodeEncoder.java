// Taken from https://github.com/zxing/zxing/blob/master/android/src/com/google/zxing/client/android/encode/QRCodeEncoder.java
/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.projecthermes.projecthermes;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import ca.projecthermes.projecthermes.util.Encryption;

/**
 * This class does the work of decoding the user's request and extracting all the data
 * to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class QRCodeEncoder {

    private static final String TAG = QRCodeEncoder.class.getSimpleName();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    static void scanQRCode(Activity sendMessageActivity) {
        IntentIntegrator integrator = new IntentIntegrator(sendMessageActivity);
        integrator.setOrientationLocked(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setPrompt("Please scan QR Code");
        integrator.setCaptureActivity(PortraitCaptureActivity.class);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    static public void saveQRCode(KeyPair keyPair, Activity activity, String keyName) {
        FileOutputStream out = null;
        try {
            BitMatrix encoded = (new BarcodeEncoder()).encode(Base64.encodeToString(Encryption.getEncodedPublicKey(keyPair), Base64.DEFAULT), BarcodeFormat.QR_CODE, 20, 20);
            File storageDir = activity.getFilesDir();
            File image = new File(storageDir, keyName+".png");

            out = new FileOutputStream(image);
            Log.d("hermes", "saving to " + image.getAbsolutePath());
            Bitmap bit = (new QRCodeEncoder()).encodeAsBitmap(encoded);
            bit.compress(Bitmap.CompressFormat.PNG, 100, out);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    Bitmap encodeAsBitmap(BitMatrix matrix) throws WriterException {
        int width = matrix.getWidth() * 10;
        int height = matrix.getHeight() * 10;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x / 10, y / 10) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}