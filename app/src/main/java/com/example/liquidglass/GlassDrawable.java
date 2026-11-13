package com.example.liquidglass;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;

public class GlassDrawable extends Drawable {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private final float radius;
    private final float rimWidth;

    public GlassDrawable(Context c) {
        this(c.getResources().getDisplayMetrics().density);
    }

    private GlassDrawable(float d) {
        density = d;
        radius = 22 * d;
        rimWidth = 1.2f * d;
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(rimWidth);
    }

    @Override protected void onBoundsChange(Rect b) {
        float h = rimWidth / 2f;
        rect.set(b.left + h, b.top + h, b.right - h, b.bottom - h);
        fill.setShader(new LinearGradient(0, b.top, 0, b.bottom,
                0x40FFFFFF, 0x1AFFFFFF, Shader.TileMode.CLAMP));
        glow.setShader(new RadialGradient(b.left + b.width() * 0.2f, b.top,
                Math.max(b.width(), 1) * 0.7f,
                0x55FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        rim.setShader(new LinearGradient(b.left, b.top, b.right, b.bottom,
                new int[]{0xCCFFFFFF, 0x20FFFFFF, 0x20FFFFFF, 0x80FFFFFF},
                new float[]{0f, 0.35f, 0.65f, 1f}, Shader.TileMode.CLAMP));
    }

    @Override public void draw(Canvas c) {
        c.drawRoundRect(rect, radius, radius, fill);
        c.drawRoundRect(rect, radius, radius, glow);
        c.drawRoundRect(rect, radius, radius, rim);
    }

    @Override public ConstantState getConstantState() {
        final float d = density;
        return new ConstantState() {
            @Override public Drawable newDrawable() { return new GlassDrawable(d); }
            @Override public int getChangingConfigurations() { return 0; }
        };
    }

    @Override public void setAlpha(int a) { fill.setAlpha(a); glow.setAlpha(a); rim.setAlpha(a); }
    @Override public void setColorFilter(ColorFilter f) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
