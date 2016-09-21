package io.github.kbiakov.codeviewexample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.Highlighter;
import io.github.kbiakov.codeview.OnCodeLineClickListener;
import io.github.kbiakov.codeview.adapters.CodeWithDiffsAdapter;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import io.github.kbiakov.codeview.views.DiffModel;

public class ListingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);

        final CodeView codeView = (CodeView) findViewById(R.id.code_view);

        /**
         * 1: default adapter with chaining build flow
         */

        // use chaining to build view with default adapter
        new Highlighter(this)
                .code(R.string.listing_js)
                .language("js")
                .theme(ColorTheme.DEFAULT.withBgContent(Color.WHITE))
                .highlight(codeView);


        /**
         * 2: updating built view
         */

        // do not use chaining for built view
        // (you can, but it should be performed sequentially)
        final Highlighter h = new Highlighter(this)
                .code(R.string.listing_java)
                .language("java")
                .theme(ColorTheme.SOLARIZED_LIGHT);

        h.setLineClickListener(new OnCodeLineClickListener() {
            @Override
            public void onLineClicked(int n, @NotNull String line) {
                Log.i("ListingsActivity", "On " + (n + 1) + " line clicked");
            }
        });

        codeView.init(h);

        /**
         * 3: custom adapter with footer views
         */

        final CustomAdapter adapter = new CustomAdapter(this, getString(R.string.listing_md));
        codeView.init(adapter);

        codeView.update(new Highlighter(this)
                .theme(ColorTheme.MONOKAI)
                .lineClickListener(new OnCodeLineClickListener() {
                    @Override
                    public void onLineClicked(int n, @NotNull String line) {
                        adapter.addFooterEntity(n, new CustomAdapter.CustomModel("Line " + (n + 1), line));
                    }
                })
                .language("md"));

        /**
         * 4: diff adapter with footer views
         */

        final CodeWithDiffsAdapter diffsAdapter = new CodeWithDiffsAdapter(ListingsActivity.this,
                getString(R.string.listing_py), ColorTheme.SOLARIZED_LIGHT.theme());

        diffsAdapter.getHighlighter().language("python");

        codeView.removeLineClickListener();

        codeView.init(diffsAdapter);

        diffsAdapter.addFooterEntity(16, new DiffModel(getString(R.string.py_addition_16), true));
        diffsAdapter.addFooterEntity(11, new DiffModel(getString(R.string.py_deletion_11), false));


        /**
         * 5: shortcut adapter with footer views
         */
        new Highlighter(this).code(R.string.listing_py)
                .shortcut(true)
                .language("python")
                .maxLines(3)
                .shortcutNote("Show All")
                .highlight(codeView);
    }
}
