package me.ydcool.qrmodule.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.ydcool.lib.qrmodule.activity.QrScannerActivity;
import me.ydcool.qrmodule.R;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.txt_scan_result)
    TextView mTxtScanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.Main_btn_scan)
    void onScanBtnClick() {
        startActivityForResult(
                new Intent(MainActivity.this, QrScannerActivity.class),
                QrScannerActivity.QR_REQUEST_CODE);
    }

    @OnClick(R.id.Main_btn_generate)
    void onGenerateBtnClick() {
        startActivity(new Intent(MainActivity.this, DemoGeneratorActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QrScannerActivity.QR_REQUEST_CODE) {
            mTxtScanResult.setText(
                    resultCode == RESULT_OK
                            ? data.getExtras().getString(QrScannerActivity.QR_RESULT_STR)
                            : "Scanned Nothing!");
        }
    }
}
