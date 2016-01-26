package me.ydcool.qrmodule.demo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.ydcool.lib.qrmodule.encoding.QrGenerator;
import me.ydcool.qrmodule.R;

public class DemoGeneratorActivity extends AppCompatActivity {
    private static final String TAG = "DemoGeneratorActivity";

    @InjectView(R.id.root_scrollview)
    ScrollView mScrollView;
    @InjectView(R.id.edt_content)
    EditText mEdtContent;
    @InjectView(R.id.edt_size)
    EditText mEdtSize;
    @InjectView(R.id.edt_margin)
    EditText mEdtMargin;
    @InjectView(R.id.edt_color_r)
    EditText mEdtColorR;
    @InjectView(R.id.edt_color_g)
    EditText mEdtColorG;
    @InjectView(R.id.edt_color_b)
    EditText mEdtColorB;
    @InjectView(R.id.edt_color_bg_r)
    EditText mEdtColorBgR;
    @InjectView(R.id.edt_color_bg_g)
    EditText mEdtColorBgG;
    @InjectView(R.id.edt_color_bg_b)
    EditText mEdtColorBgB;
    @InjectView(R.id.spinner_ecc)
    Spinner mSpinnerEcc;
    @InjectView(R.id.toggle_overlay)
    ToggleButton mTbOverlay;
    @InjectView(R.id.edt_overlay_size)
    EditText mEdtOverlaySize;
    @InjectView(R.id.edt_overlay_aplha)
    EditText mEdtOverlayAplha;
    @InjectView(R.id.spinner_xfermode)
    Spinner mSpinnerXfermode;
    @InjectView(R.id.edt_footnote)
    EditText mEdtFootnote;
    @InjectView(R.id.img_qr_generated)
    ImageView mImgQrGenerated;

    private ErrorCorrectionLevel mEcc = ErrorCorrectionLevel.L;
    private PorterDuff.Mode mXfermode = PorterDuff.Mode.SRC;

    private boolean mOverlayEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);
        ButterKnife.inject(this);

        setUpEccSpinner();
        setUpXfermodeSpinner();
        setUpOverlayToggleBtn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_quick_build:
                quickEdit(false);
                return true;
            case R.id.action_clear:
                quickEdit(true);
                return true;
            case R.id.action_generate:
                onGenerateClick();
            default:
                return false;
        }
    }

    void onGenerateClick() {
        if (checkEmpty(mEdtContent)) {
            Toast.makeText(DemoGeneratorActivity.this, "Content Required!", Toast.LENGTH_SHORT).show();
        } else if (checkEmpty(mEdtSize)) {
            Toast.makeText(DemoGeneratorActivity.this, "Qr Size Required!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                int _color = Color.rgb(getInputtedInt(mEdtColorR, 0), getInputtedInt(mEdtColorG, 0), getInputtedInt(mEdtColorB, 0));
                int _bgColor = Color.rgb(getInputtedInt(mEdtColorBgR, 255), getInputtedInt(mEdtColorBgG, 255), getInputtedInt(mEdtColorBgB, 255));

                Bitmap qrCode = new QrGenerator.Builder()
                        .content(mEdtContent.getText().toString())
                        .qrSize(getInputtedInt(mEdtSize, 400))
                        .margin(getInputtedInt(mEdtMargin, 2))
                        .color(_color)
                        .bgColor(_bgColor)
                        .ecc(mEcc)
                        .overlay(mOverlayEnabled ? BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher) : null)
                        .overlaySize(getInputtedInt(mEdtOverlaySize, 100))
                        .overlayAlpha(getInputtedInt(mEdtOverlayAplha, 255))
                        .overlayXfermode(mXfermode)
                        .footNote(mEdtFootnote.getText().toString())
                        .encode();

                mImgQrGenerated.setImageBitmap(qrCode);
                mScrollView.smoothScrollTo(0, 0);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpEccSpinner() {
        final String[] items = getEnumNames(ErrorCorrectionLevel.class);
        ArrayAdapter<String> eccSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        eccSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerEcc.setAdapter(eccSpinnerAdapter);
        mSpinnerEcc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEcc = ErrorCorrectionLevel.valueOf(items[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpXfermodeSpinner() {
        final String[] items = getEnumNames(PorterDuff.Mode.class);
        ArrayAdapter<String> xfermodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        xfermodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerXfermode.setAdapter(xfermodeAdapter);
        mSpinnerXfermode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mXfermode = PorterDuff.Mode.valueOf(items[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpOverlayToggleBtn() {
        mTbOverlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mOverlayEnabled = isChecked;
            }
        });
    }

    private boolean checkEmpty(EditText e) {
        return TextUtils.isEmpty(e.getText());
    }

    @SuppressLint("SetTextI18n")
    private void quickEdit(boolean clear) {
        mEdtContent.setText(clear ? "" : "http://github.com/ydcool/QrModule");
        mEdtSize.setText(clear ? "" : "400");
        mEdtMargin.setText(clear ? "" : "2");
        mEdtColorR.setText(clear ? "" : "65");
        mEdtColorG.setText(clear ? "" : "82");
        mEdtColorB.setText(clear ? "" : "172");
        mEdtColorBgR.setText(clear ? "" : "233");
        mEdtColorBgG.setText(clear ? "" : "233");
        mEdtColorBgB.setText(clear ? "" : "233");

        mSpinnerEcc.setSelection(ErrorCorrectionLevel.H.ordinal(), true);

        mOverlayEnabled = !clear;
        mTbOverlay.setChecked(!clear);

        mEdtOverlaySize.setText(clear ? "" : "100");
        mEdtOverlayAplha.setText(clear ? "" : "255");

        mXfermode = PorterDuff.Mode.SRC_ATOP;
        mSpinnerXfermode.setSelection(PorterDuff.Mode.SRC_ATOP.ordinal(), true);

        if (clear)
            mImgQrGenerated.setImageBitmap(null);
    }

    private int getInputtedInt(EditText edt, int def) {
        try {
            String s = edt.getText().toString();

            if (TextUtils.isEmpty(s))
                return def;
            else
                return Integer.parseInt(edt.getText().toString());

        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    private <E extends Enum<E>> String[] getEnumNames(Class<E> enumType) {
        EnumSet<E> set = EnumSet.allOf(enumType);

        String[] names = new String[set.size()];

        for (Enum<E> e : set) {
            names[e.ordinal()] = e.toString();
        }

        return names;
    }
}
