package io.github.kbiakov.codeviewexample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import io.github.kbiakov.codeview.adapters.CodeWithNotesAdapter;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.OnCodeLineClickListener;

public class ListingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);

        int myColor = ContextCompat.getColor(this, R.color.code_num);

        CodeView codeView = (CodeView) findViewById(R.id.code_view);

        /**
         * 1: Default adapter with chaining build flow
         */

        // use chaining to build view with default adapter
        codeView.highlightCode("js")
                .setColorTheme(ColorTheme.DEFAULT.withBgContent(myColor))
                .setCodeContent(getString(R.string.listing_js));

        /**
         * 2: Updating built view
         */

        // do not use chaining for built view
        // (you can, but it should be performed sequentially)
        codeView.setCodeContent(getString(R.string.listing_java));
        codeView.setColorTheme(ColorTheme.SOLARIZED_LIGHT);
        codeView.highlightCode("java");
        codeView.setCodeListener(new OnCodeLineClickListener() {
            @Override
            public void onCodeLineClicked(int n, @NotNull String line) {
                Log.i("ListingsActivity", "On " + (n + 1) + " line clicked");
            }
        });

        /**
         * 3: Custom adapter with footer views
         */

        final CustomAdapter adapter = new CustomAdapter(this, getString(R.string.listing_py));

        codeView.setAdapter(adapter);
        codeView.setColorTheme(ColorTheme.MONOKAI);
        codeView.highlightCode("python");
        codeView.setCodeListener(new OnCodeLineClickListener() {
            @Override
            public void onCodeLineClicked(int n, @NotNull String line) {
                adapter.addFooterEntity(n, new CustomAdapter.CustomModel("Line " + (n + 1), line));
            }
        });
    }
}
