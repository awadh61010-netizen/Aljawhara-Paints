package com.aljawhara.paints;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.InputStream;

public class MainActivity extends Activity {

    private PaintVisualizerView visualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(247, 243, 236));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        visualizer = new PaintVisualizerView();
        setContentView(visualizer);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1401);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1401 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                visualizer.loadHouse(uri);
            }
        }
    }

    class PaintVisualizerView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int cream = Color.rgb(247, 243, 236);
        private final int navy = Color.rgb(28, 43, 58);
        private final int gold = Color.rgb(181, 138, 58);
        private Bitmap jawhara, ffa, house;
        private int selected = 0;
        private final int[] colors = {
            Color.rgb(239,232,219), Color.rgb(222,209,188),
            Color.rgb(204,190,165), Color.rgb(177,169,153),
            Color.rgb(142,144,139), Color.rgb(113,115,101),
            Color.rgb(78,88,95), Color.rgb(196,169,126),
            Color.rgb(151,125,91), Color.rgb(93,79,64)
        };

        PaintVisualizerView() {
            super(MainActivity.this);
            setBackgroundColor(cream);
            jawhara = BitmapFactory.decodeResource(getResources(), R.drawable.jawhara_logo);
            ffa = BitmapFactory.decodeResource(getResources(), R.drawable.ffa_logo);
            text.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        }

        void loadHouse(Uri uri) {
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                house = BitmapFactory.decodeStream(in);
                invalidate();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "تعذر فتح الصورة", Toast.LENGTH_SHORT).show();
            }
        }

        private void rounded(Canvas c, float l, float t, float r, float b, float radius, int color) {
            p.setColor(color);
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(l, t, r, b, radius, radius, p);
        }

        private void label(Canvas c, String s, float x, float y, float size, int color, Paint.Align align, boolean bold) {
            text.setTextSize(size);
            text.setColor(color);
            text.setTextAlign(align);
            text.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
            c.drawText(s, x, y, text);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            float m = w * .055f;

            // Header
            float headerH = h * .145f;
            rounded(c, m, h*.02f, w-m, headerH, 26, Color.WHITE);

            // jawhara logo
            RectF jr = new RectF(m+18, h*.03f, w*.48f, headerH-12);
            drawFit(c, jawhara, jr);

            // divider
            p.setColor(Color.rgb(219,202,170));
            p.setStrokeWidth(2);
            c.drawLine(w*.52f, h*.04f, w*.52f, headerH-h*.015f, p);

            // FFA
            RectF fr = new RectF(w*.56f, h*.035f, w-m-16, headerH-h*.02f);
            drawFit(c, ffa, fr);
            label(c, "الوكيل في اليمن", w*.73f, headerH-h*.012f, 18, gold, Paint.Align.CENTER, true);

            // Title
            label(c, "معاينة الألوان", w-m, headerH+h*.045f, 30, navy, Paint.Align.RIGHT, true);
            label(c, "شاهد منزلك بالألوان قبل التنفيذ", w-m, headerH+h*.073f, 18, Color.DKGRAY, Paint.Align.RIGHT, false);

            // Image card
            float imgTop = headerH+h*.095f;
            float imgBottom = h*.60f;
            rounded(c, m, imgTop, w-m, imgBottom, 28, Color.WHITE);

            RectF img = new RectF(m+12, imgTop+12, w-m-12, imgBottom-12);
            p.setColor(Color.rgb(233, 228, 219));
            c.drawRoundRect(img, 22, 22, p);

            if (house != null) {
                drawCrop(c, house, img);
                // simple color preview tint over whole photo for version 1
                p.setColor(colors[selected]);
                p.setAlpha(48);
                c.drawRoundRect(img, 22, 22, p);
                p.setAlpha(255);
            } else {
                label(c, "اختر صورة المنزل", w/2, (imgTop+imgBottom)/2-8, 28, navy, Paint.Align.CENTER, true);
                label(c, "من معرض الصور في التابلت", w/2, (imgTop+imgBottom)/2+26, 18, Color.GRAY, Paint.Align.CENTER, false);
            }

            // Upload button
            float btnW = w*.46f, btnH = h*.065f;
            float btnL = w/2-btnW/2, btnT = imgBottom-h*.085f;
            rounded(c, btnL, btnT, btnL+btnW, btnT+btnH, 22, navy);
            label(c, "اختيار صورة المنزل", w/2, btnT+btnH*.63f, 21, Color.WHITE, Paint.Align.CENTER, true);

            // Palette title
            float palTop = h*.635f;
            label(c, "اختر لون الدهان", w-m, palTop, 28, navy, Paint.Align.RIGHT, true);
            label(c, "اختر الدرجة المناسبة للمعاينة", w-m, palTop+30, 17, Color.GRAY, Paint.Align.RIGHT, false);

            // Swatches
            float gap = w*.022f;
            float sw = (w - 2*m - gap*4)/5f;
            float y1 = palTop+55;
            for (int i=0; i<10; i++) {
                int row = i/5, col = i%5;
                float l = m + col*(sw+gap);
                float t = y1 + row*(sw*.70f + gap + 24);
                rounded(c, l, t, l+sw, t+sw*.70f, 18, colors[i]);
                if (i == selected) {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(5);
                    p.setColor(gold);
                    c.drawRoundRect(l-4, t-4, l+sw+4, t+sw*.70f+4, 20, 20, p);
                    p.setStyle(Paint.Style.FILL);
                }
                label(c, "JW " + String.format("%04d", 1001+i), l+sw/2, t+sw*.70f+22, 14, navy, Paint.Align.CENTER, false);
            }

            // Footer
            rounded(c, m, h*.925f, w-m, h*.985f, 26, Color.WHITE);
            label(c, "الرئيسية", w*.78f, h*.962f, 17, gold, Paint.Align.CENTER, true);
            label(c, "الألوان", w*.55f, h*.962f, 17, navy, Paint.Align.CENTER, false);
            label(c, "المفضلة", w*.32f, h*.962f, 17, navy, Paint.Align.CENTER, false);
            label(c, "الإعدادات", w*.13f, h*.962f, 17, navy, Paint.Align.CENTER, false);
        }

        private void drawFit(Canvas c, Bitmap b, RectF dst) {
            if (b == null) return;
            float s = Math.min(dst.width()/b.getWidth(), dst.height()/b.getHeight());
            float nw = b.getWidth()*s, nh = b.getHeight()*s;
            RectF d = new RectF(dst.centerX()-nw/2, dst.centerY()-nh/2,
                                dst.centerX()+nw/2, dst.centerY()+nh/2);
            c.drawBitmap(b, null, d, p);
        }

        private void drawCrop(Canvas c, Bitmap b, RectF dst) {
            if (b == null) return;
            float srcRatio = (float)b.getWidth()/b.getHeight();
            float dstRatio = dst.width()/dst.height();
            Rect src;
            if (srcRatio > dstRatio) {
                int newW = (int)(b.getHeight()*dstRatio);
                int left = (b.getWidth()-newW)/2;
                src = new Rect(left,0,left+newW,b.getHeight());
            } else {
                int newH = (int)(b.getWidth()/dstRatio);
                int top = (b.getHeight()-newH)/2;
                src = new Rect(0,top,b.getWidth(),top+newH);
            }
            c.save();
            Path path = new Path();
            path.addRoundRect(dst, 22,22, Path.Direction.CW);
            c.clipPath(path);
            c.drawBitmap(b, src, dst, p);
            c.restore();
        }

        @Override
        public boolean onTouchEvent(android.view.MotionEvent e) {
            if (e.getAction() != MotionEvent.ACTION_UP) return true;
            float x=e.getX(), y=e.getY(), w=getWidth(), h=getHeight(), m=w*.055f;

            float imgBottom = h*.60f;
            float btnW = w*.46f, btnH=h*.065f;
            float btnL=w/2-btnW/2, btnT=imgBottom-h*.085f;
            if (x>=btnL && x<=btnL+btnW && y>=btnT && y<=btnT+btnH) {
                chooseImage();
                return true;
            }

            float palTop=h*.635f, gap=w*.022f;
            float sw=(w-2*m-gap*4)/5f, y1=palTop+55;
            for(int i=0;i<10;i++){
                int row=i/5,col=i%5;
                float l=m+col*(sw+gap);
                float t=y1+row*(sw*.70f+gap+24);
                if(x>=l && x<=l+sw && y>=t && y<=t+sw*.70f){
                    selected=i;
                    invalidate();
                    return true;
                }
            }
            return true;
        }
    }
}
