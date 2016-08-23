package io.github.kbiakov.codeview.highlight;

import android.content.Context;
import android.graphics.Typeface;

/**
 * @class MonoFontCache
 *
 * Simple font cache.
 *
 * @see io.github.kbiakov.codeview.CodeContentAdapter
 * @author Kirill Biakov
 */
public class MonoFontCache {

    private static final String FONT_NAME = "DroidSansMonoSlashed";

    private static volatile MonoFontCache instance;

    public static MonoFontCache getInstance(Context context) {
        MonoFontCache localInstance = instance;
        if (localInstance == null) {
            synchronized (MonoFontCache.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new MonoFontCache(context);
                }
            }
        }
        return localInstance;
    }

    private MonoFontCache(Context context) {
        this.fontTypeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s.ttf", FONT_NAME));
    }

    private Typeface fontTypeface;

    public Typeface getTypeface() {
        return fontTypeface;
    }
}
