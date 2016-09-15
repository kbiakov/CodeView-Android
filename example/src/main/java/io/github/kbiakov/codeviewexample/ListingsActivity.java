package io.github.kbiakov.codeviewexample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.OnCodeLineClickListener;
import io.github.kbiakov.codeview.adapters.CodeWithDiffsAdapter;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import io.github.kbiakov.codeview.views.DiffModel;

public class ListingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);

        int myColor = ContextCompat.getColor(this, R.color.code_num);

        final CodeView codeView = (CodeView) findViewById(R.id.code_view);

        /**
         * 1: default adapter with chaining build flow
         */

        // use chaining to build view with default adapter
        codeView.colorTheme(ColorTheme.DEFAULT.withBgContent(myColor))
                .codeContent(getString(R.string.listing_js))
                .highlight("js");

        /**
         * 2: updating built view
         */

        // do not use chaining for built view
        // (you can, but it should be performed sequentially)
        codeView.codeContent(getString(R.string.listing_java))
                .colorTheme(ColorTheme.SOLARIZED_LIGHT)
                .highlight("java");

        codeView.codeListener(new OnCodeLineClickListener() {
            @Override
            public void onCodeLineClicked(int n, @NotNull String line) {
                Log.i("ListingsActivity", "On " + (n + 1) + " line clicked");
            }
        });

        /**
         * 3: custom adapter with footer views
         */

        final CustomAdapter adapter = new CustomAdapter(this, getString(R.string.listing_md));

        codeView.setAdapter(adapter);
        codeView.colorTheme(ColorTheme.MONOKAI)
                .codeListener(new OnCodeLineClickListener() {
                    @Override
                    public void onCodeLineClicked(int n, @NotNull String line) {
                        adapter.addFooterEntity(n, new CustomAdapter.CustomModel("Line " + (n + 1), line));
                    }
                })
                .highlight("md");

        /**
         * 4: diff adapter with footer views
         */

        final CodeWithDiffsAdapter diffsAdapter = new CodeWithDiffsAdapter(this, getString(R.string.listing_py),
                ColorTheme.SOLARIZED_LIGHT.theme());
        codeView.setAdapter(diffsAdapter);
        codeView.highlight("python");
        codeView.removeCodeListener();

        diffsAdapter.addFooterEntity(16, new DiffModel(getString(R.string.py_addition_16), true));
        diffsAdapter.addFooterEntity(11, new DiffModel(getString(R.string.py_deletion_11), false));
    }
}
