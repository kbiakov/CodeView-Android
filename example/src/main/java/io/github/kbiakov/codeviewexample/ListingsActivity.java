package io.github.kbiakov.codeviewexample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.OnCodeLineClickListener;
import io.github.kbiakov.codeview.adapters.CodeWithDiffsAdapter;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import io.github.kbiakov.codeview.highlight.ColorThemeData;
import io.github.kbiakov.codeview.highlight.Font;
import io.github.kbiakov.codeview.highlight.FontCache;
import io.github.kbiakov.codeview.views.DiffModel;

public class ListingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);

        final CodeView codeView = (CodeView) findViewById(R.id.code_view);

        /*
         * 1: set code content
         */

        // auto language recognition
        codeView.setCode(getString(R.string.listing_js));

        // specify language for code listing
        codeView.setCode(getString(R.string.listing_py), "py");

        /*
         * 2: working with options
         */

        // you can change params as follows (unsafe, initialized view only)
        codeView.getOptions()
                .withCode(R.string.listing_java)
                .withTheme(ColorTheme.MONOKAI);

        // short initialization with default params (can be expanded using with() methods)
        codeView.setOptions(Options.Default.get(this)
                .withLanguage("python")
                .withCode(R.string.listing_py)
                .withTheme(ColorTheme.MONOKAI)
                .withFont(Font.Consolas));

        // expanded form of initialization
        codeView.setOptions(new Options(
                this,                                   // context
                getString(R.string.listing_js),         // code
                "js",                                   // language
                ColorTheme.MONOKAI.theme(),             // theme (data)
                FontCache.get(this).getTypeface(this),  // font
                true,                                   // shadows
                true,                                   // shortcut
                getString(R.string.show_all),           // shortcut note
                10,                                     // max lines
                new OnCodeLineClickListener() {         // line click listener
                    @Override
                    public void onCodeLineClicked(int n, @NotNull String line) {
                        Log.i("ListingsActivity", "On " + (n + 1) + " line clicked");
                    }
                }));

        /*
         * 3: color themes
         */

        codeView.getOptions().setTheme(ColorTheme.SOLARIZED_LIGHT);

        // custom theme
        ColorThemeData myTheme = ColorTheme.SOLARIZED_LIGHT.theme()
                .withBgContent(android.R.color.black)
                .withNoteColor(android.R.color.white);

        codeView.getOptions().setTheme(myTheme);

        /*
         * 4: custom adapter with footer views
         */

        final CustomAdapter myAdapter = new CustomAdapter(this, getString(R.string.listing_md));
        codeView.setAdapter(myAdapter);
        codeView.getOptions()
                .withLanguage("md")
                .addCodeLineClickListener(new OnCodeLineClickListener() {
                    @Override
                    public void onCodeLineClicked(int n, @NotNull String line) {
                        myAdapter.addFooterEntity(n, new CustomAdapter.CustomModel("Line " + (n + 1), line));
                    }
                });

        /*
         * 5: diff adapter with footer views
         */

        final CodeWithDiffsAdapter diffsAdapter = new CodeWithDiffsAdapter(this);
        codeView.getOptions()
                .withLanguage("python")
                .setCode(getString(R.string.listing_py));
        codeView.updateAdapter(diffsAdapter);

        diffsAdapter.addFooterEntity(16, new DiffModel(getString(R.string.py_addition_16), true));
        diffsAdapter.addFooterEntity(11, new DiffModel(getString(R.string.py_deletion_11), false));

        /*
         * 6: shortcut adapter with footer views
         */

        codeView.getOptions()
                .shortcut(10, "Show all");
    }
}
