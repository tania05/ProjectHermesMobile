package ca.projecthermes.projecthermes;

import android.os.Bundle;
import android.view.View;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class PortraitCaptureActivity extends CaptureActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //DecoratedBarcodeView barcodeScanner = (DecoratedBarcodeView) this.findViewById(R.id.zxing_barcode_scanner);
        //barcodeScanner.getViewFinder().setVisibility(View.INVISIBLE);
    }

}
