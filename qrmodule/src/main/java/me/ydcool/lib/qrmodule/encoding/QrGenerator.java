package me.ydcool.lib.qrmodule.encoding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * @author Ryan Tang
 */
public final class QrGenerator {
    private int mQrSize;
    private int mColor;
    private int mMargin;
    private int mBgColor;
    private int mOverlaySize;
    private int mOverlayAlpha;
    private String mContent;
    private String mFootNote;
    private Bitmap mOverlay;
    private ErrorCorrectionLevel mEcl;
    private PorterDuff.Mode mXfermode;

    private QrGenerator(Builder builder) {
        mMargin = builder.mMargin;
        mQrSize = builder.mQrSize;
        mColor = builder.mColor;
        mBgColor = builder.mBgColor;
        mContent = builder.mContent;
        mEcl = builder.mEcl;
        mOverlay = builder.mOverlay;
        mOverlaySize = builder.mOverlaySize;
        mOverlayAlpha = builder.mOverlayAlpha;
        mXfermode = builder.mXFerMode;
        mFootNote = builder.mFootNote;
    }

    private Bitmap createQRCode()
            throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();

        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, mEcl);
        hints.put(EncodeHintType.MARGIN, mMargin);

        BitMatrix matrix = new MultiFormatWriter().encode(mContent,
                BarcodeFormat.QR_CODE, mQrSize, mQrSize, hints);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = mColor;
                } else {
                    pixels[y * width + x] = mBgColor;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        //draw overlay
        if (mOverlay != null && mOverlaySize > 0) {
            Bitmap w = Bitmap.createBitmap(mOverlay);
            Bitmap o = w.copy(Bitmap.Config.ARGB_8888, true);
            w.recycle();

            int overlayW = o.getWidth();
            int overlayH = o.getHeight();

            int scaledH = mOverlaySize * overlayW / overlayH;
            int offsetX = (mQrSize - mOverlaySize) / 2;
            int offsetY = (mQrSize - scaledH) / 2;

            Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
            p.setAlpha(mOverlayAlpha);
            p.setXfermode(new PorterDuffXfermode(mXfermode));

            Canvas canvas = new Canvas(bitmap);
            Rect src = new Rect(0, 0, overlayW, overlayH);
            Rect dst = new Rect(0, 0, mOverlaySize, scaledH);

            canvas.translate(offsetX, offsetY);
            canvas.drawBitmap(o, src, dst, p);
        }

        //draw enlarge the canvas and add footnote
        if (!TextUtils.isEmpty(mFootNote)) {
            Bitmap result = Bitmap.createBitmap(mQrSize, mQrSize * 3 / 2, Bitmap.Config.ARGB_8888);

            TextPaint textPaint = new TextPaint();
            textPaint.setColor(mColor);
            textPaint.setTextSize(20);
            textPaint.setAntiAlias(true);

            StaticLayout mTextLayout = new StaticLayout(
                    mFootNote,
                    textPaint,
                    mQrSize,
                    Layout.Alignment.ALIGN_CENTER,
                    1.4f, 0.2f, false);

            Canvas canvas = new Canvas(result);
            canvas.drawColor(mBgColor);
            canvas.drawBitmap(bitmap, 0, 0, null);

            canvas.translate(0, mQrSize * 9 / 8);
            mTextLayout.draw(canvas);

            return result;
        }

        return bitmap;
    }

    public static class Builder {
        private int mColor = Color.BLACK;
        private int mBgColor = Color.WHITE;
        private int mMargin = 2;
        private int mQrSize;
        private int mOverlaySize;
        private int mOverlayAlpha = 255;

        private String mContent;
        private String mFootNote;
        private Bitmap mOverlay;
        private ErrorCorrectionLevel mEcl = ErrorCorrectionLevel.L;
        private PorterDuff.Mode mXFerMode = PorterDuff.Mode.SRC_OVER;

        public Bitmap encode() throws WriterException {
            return new QrGenerator(this).createQRCode();
        }

        /**
         * @param content qr content
         * @return builder instance
         */
        public Builder content(String content) {
            this.mContent = content;
            return this;
        }

        /**
         * @param widthAndHeight qr image size
         * @return builder instance
         */
        public Builder qrSize(int widthAndHeight) {
            this.mQrSize = widthAndHeight;
            return this;
        }

        /**
         * Optional
         *
         * @param margin default is 2.See more about {@link EncodeHintType#MARGIN}
         * @return builder instance
         */
        public Builder margin(int margin) {
            this.mMargin = margin;
            return this;
        }

        /**
         * Optional
         *
         * @param color QRCode foreground color,default is {@link Color#BLACK}
         * @return builder instance
         */
        public Builder color(int color) {
            this.mColor = color;
            return this;
        }

        /**
         * Optional
         *
         * @param context context
         * @param color   <b>@ColorRes</b>: QRCode foreground color resource,default is {@link Color#BLACK}
         * @return builder instance
         */
        public Builder color(Context context, int color) {
            return color(context.getResources().getColor(color));
        }

        /**
         * Optional
         *
         * @param bgColor QRCode background color,default is {@link Color#WHITE}
         * @return builder instance
         */
        public Builder bgColor(int bgColor) {
            this.mBgColor = bgColor;
            return this;
        }

        /**
         * Optional
         *
         * @param ecl ErrorCorrectionLevel,default is {@link ErrorCorrectionLevel#L}(=~7%).
         * @return builder instance
         */
        public Builder ecc(ErrorCorrectionLevel ecl) {
            this.mEcl = ecl;
            return this;
        }

        /**
         * @param overlay Optional,the overlay on QRCode ,like app icon or something else.
         * @return builder instance
         */
        public Builder overlay(Bitmap overlay) {
            this.mOverlay = overlay;
            return this;
        }

        /**
         * Optional
         *
         * @param context the context to get resources instance.
         * @param overlay <b>@DrawableRes</b>:the drawable overlay on QRCode, like app icon or something else.
         * @return builder instance
         */
        public Builder overlay(Context context, int overlay) {
            return overlay(BitmapFactory.decodeResource(context.getResources(), overlay));
        }

        /**
         * Optional
         *
         * @param overlaySize the overlay icon size
         * @return build instance
         */
        public Builder overlaySize(int overlaySize) {
            this.mOverlaySize = overlaySize;
            return this;
        }

        /**
         * Optional
         *
         * @param alpha the alpha of overlay bitmap, range [0..255],default is 255 (opaque)
         * @return build instance
         */
        public Builder overlayAlpha(int alpha) {
            this.mOverlayAlpha = alpha;
            return this;
        }

        /**
         * Optional
         *
         * @param xfermode xfermode to combine overlay with qr code image, default is {@link android.graphics.PorterDuff.Mode#SRC_OVER}
         * @return builder instance
         */
        public Builder overlayXfermode(PorterDuff.Mode xfermode) {
            this.mXFerMode = xfermode;
            return this;
        }

        public Builder footNote(String note) {
            this.mFootNote = note;
            return this;
        }
    }
}
