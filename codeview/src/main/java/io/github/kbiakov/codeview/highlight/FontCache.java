package io.github.kbiakov.codeview.highlight;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Map;
import java.util.WeakHashMap;

import io.github.kbiakov.codeview.adapters.AbstractCodeAdapter;

/**
 * Font cache.
 *
 * @see AbstractCodeAdapter
 * @author Kirill Biakov
 */
public class FontCache {

    private static volatile FontCache instance;

    public static FontCache get(Context context) {
        FontCache localInstance = instance;
        if (localInstance == null) {
            synchronized (FontCache.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new FontCache(context);
                }
            }
        }
        return localInstance;
    }

    private Map<String, Typeface> fonts;

    private FontCache(final Context context) {
        this.fonts = new WeakHashMap<String, Typeface>() {{
            String fontPath = getLocalFontPath(Font.Companion.getDefault());
            put(fontPath, loadFont(context, fontPath));
        }};
    }

    private static String getLocalFontPath(Font font) {
        return String.format("%s.ttf", getLocalFontPath(font.name()));
    }

    private static String getLocalFontPath(String fontName) {
        return String.format("fonts/%s", fontName);
    }

    private static Typeface loadFont(Context context, String fontPath) {
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    // - Public methods

    public Typeface getTypeface(Context context) {
        return getTypeface(context, Font.Companion.getDefault());
    }

    public Typeface getTypeface(Context context, Font font) {
        return getTypeface(context, getLocalFontPath(font));
    }

    public Typeface getLocalTypeface(Context context, String fontPath) {
        return getTypeface(context, getLocalFontPath(fontPath));
    }

    public Typeface getTypeface(Context context, String fontPath) {
        Typeface font = fonts.get(fontPath);
        if (font != null) {
            return font;
        } else {
            font = loadFont(context, fontPath);
            fonts.put(fontPath, font);
            return font;
        }
    }

    public void saveTypeface(Typeface fontTypeface) {
        fonts.put(fontTypeface.toString(), fontTypeface);
    }
}
