package io.github.kbiakov.codeviewexample;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import io.github.kbiakov.codeview.highlight.Font;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ListingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);

        final CodeView codeView = (CodeView) findViewById(R.id.code_view);

//        /*
//         * 1: set code content
//         */
//
//        // auto language recognition
//        codeView.setCode(getString(R.string.listing_js));
//
//        // specify language for code listing
//        codeView.setCode(getString(R.string.listing_py), "py");
//
//        /*
//         * 2: working with options
//         */
//
//        // you can change params as follows (unsafe, initialized view only)
//        codeView.getOptions()
//                .withCode(R.string.listing_java)
//                .withTheme(ColorTheme.MONOKAI);
//
//        // short initialization with default params (can be expanded using with() methods)
//        codeView.setOptions(Options.Default.get(this)
//                .withLanguage("python")
//                .withCode(R.string.listing_py)
//                .withTheme(ColorTheme.MONOKAI)
//                .withFont(Font.Consolas));
//
//        // expanded form of initialization
//        codeView.setOptions(new Options(
//                this,                                   // context
//                getString(R.string.listing_js),         // code
//                "js",                                   // language
//                ColorTheme.MONOKAI.theme(),             // theme (data)
//                FontCache.get(this).getTypeface(this),  // font
//                Format.Default.getCompact(),            // format
//                true,                                   // animate on highlight
//                true,                                   // shadows visible
//                true,                                   // shortcut
//                getString(R.string.show_all),           // shortcut note
//                10,                                     // max lines
//                new OnCodeLineClickListener() {         // line click listener
//                    @Override
//                    public void onCodeLineClicked(int n, @NotNull String line) {
//                        Log.i("ListingsActivity", "On " + (n + 1) + " line clicked");
//                    }
//                }));
//
//        /*
//         * 3: color themes
//         */
//
//        codeView.getOptions().setTheme(ColorTheme.SOLARIZED_LIGHT);
//
//        // custom theme
//        ColorThemeData myTheme = ColorTheme.SOLARIZED_LIGHT.theme()
//                .withBgContent(android.R.color.black)
//                .withNoteColor(android.R.color.white);
//
//        codeView.getOptions().setTheme(myTheme);
//
//        /*
//         * 4: custom adapter with footer views
//         */
//
//        final CustomAdapter myAdapter = new CustomAdapter(this, getString(R.string.listing_md));
//        codeView.setAdapter(myAdapter);
//        codeView.getOptions()
//                .withLanguage("md")
//                .addCodeLineClickListener(new OnCodeLineClickListener() {
//                    @Override
//                    public void onCodeLineClicked(int n, @NotNull String line) {
//                        myAdapter.addFooterEntity(n, new CustomAdapter.CustomModel("Line " + (n + 1), line));
//                    }
//                });
//
//        /*
//         * 5: diff adapter with footer views
//         */
//
//        final CodeWithDiffsAdapter diffsAdapter = new CodeWithDiffsAdapter(this);
//        codeView.getOptions()
//                .withLanguage("python")
//                .setCode(getString(R.string.listing_py));
//        codeView.updateAdapter(diffsAdapter);
//
//        diffsAdapter.addFooterEntity(16, new DiffModel(getString(R.string.py_addition_16), true));
//        diffsAdapter.addFooterEntity(11, new DiffModel(getString(R.string.py_deletion_11), false));
//
//        /*
//         * 6: shortcut adapter with footer views
//         */
//
//        codeView.getOptions()
//                .shortcut(10, "Show all");

        // - Playground

        codeView.setCode("" +
                "package io.github.kbiakov.codeviewexample;\n" +
                "\n" +
                "import android.os.Bundle;\n" +
                "import android.support.annotation.Nullable;\n" +
                "import android.support.v7.app.AppCompatActivity;\n" +
                "import android.util.Log;\n" +
                "\n" +
                "import org.jetbrains.annotations.NotNull;\n" +
                "\n" +
                "import io.github.kbiakov.codeview.CodeView;\n" +
                "import io.github.kbiakov.codeview.OnCodeLineClickListener;\n" +
                "import io.github.kbiakov.codeview.adapters.CodeWithDiffsAdapter;\n" +
                "import io.github.kbiakov.codeview.adapters.Options;\n" +
                "import io.github.kbiakov.codeview.highlight.ColorTheme;\n" +
                "import io.github.kbiakov.codeview.highlight.ColorThemeData;\n" +
                "import io.github.kbiakov.codeview.highlight.Font;\n" +
                "import io.github.kbiakov.codeview.highlight.FontCache;\n" +
                "import io.github.kbiakov.codeview.views.DiffModel;\n" +
                "\n" +
                "public class ListingsActivity extends AppCompatActivity {\n" +
                "\n" +
                "    @Override\n" +
                "    protected void onCreate(@Nullable Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        setContentView(R.layout.activity_listings);\n" +
                "\n" +
                "        final CodeView codeView = (CodeView) findViewById(R.id.code_view);\n" +
                "\n" +
                "        /*\n" +
                "         * 1: set code content\n" +
                "         */\n" +
                "\n" +
                "        // auto language recognition\n" +
                "        codeView.setCode(getString(R.string.listing_js));\n" +
                "\n" +
                "        // specify language for code listing\n" +
                "        codeView.setCode(getString(R.string.listing_py), \"py\");" +
                "    }\n" +
                "}", "java");
        codeView.updateOptions(new Function1<Options, Unit>() {
            @Override
            public Unit invoke(Options options) {
                options.withFont(Font.Consolas)
                        .withTheme(ColorTheme.SOLARIZED_LIGHT)
                        .withShadows()
                        .setShortcut(false);
                return null;
            }
        });
    }
}
