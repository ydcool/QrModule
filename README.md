### Android Qr Module

[TOC]

> Thanks to [Ryan_Tang](http://blog.csdn.net/ryantang03).
  本项目基于他[blog](http://blog.csdn.net/ryantang03/article/details/7831826)上的项目优化改进。

#### Features

* Qr Scan
* Qr Generate

##### Qr Scan

###### Demo

//TODO

###### Usage

1. add the [QrScannerActivity]() to your `AndroidManifest.xml`

```xml
    <activity android:name="me.ydcool.lib.qrmodule.activity.QrScannerActivity"/>
```

2. add permissions

```xml
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.FLASHLIGHT"/>
    <uses-permission android:name="android.permission.VIBRATE"/>
```

3. Start your scanner activity.

```java
    Intent intent = new Intent(MainActivity.this, QrScannerActivity.class);
    startActivityForResult(intent, QrScannerActivity.QR_REQUEST_CODE);
```

4. Finally receive scanner activity result in your MainActivity.

```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QrScannerActivity.QR_REQUEST_CODE) {
            Log.d(TAG, resultCode == RESULT_OK
                            ? data.getExtras().getString(QrScannerActivity.QR_RESULT_STR)
                            : "Scanned Nothing!");
        }
    }
```

##### QR Generate

###### Demo

//TODO

###### Usage

* Generate qr code with [QrGenerator.java]().

```java
    Bitmap qrCode = new QrGenerator.Builder()
        .content("https://github.com/Ydcool/QrModule")
        .qrSize(300)
        .margin(2)
        .color(Color.BLACK)
        .bgColor(Color.WHITE)
        .ecc(ErrorCorrectionLevel.H)
        .overlay(getContext(),R.mipmap.ic_launcher)
        .overlaySize(100)
        .overlayAlpha(255)
        .overlayXfermode(PortBuff.Mode.SRC_ATOP)
        .footNote("Hello World!")
        .encode();

    mImageView.setImageBitmap(qrCode);
```

* Functions List

//TODO

#### License

QrModule is under Apache License 2.0.See the [LICENCE](https://github.com/Ydcool/QrModule/blob/master/LICENSE) file for more info.