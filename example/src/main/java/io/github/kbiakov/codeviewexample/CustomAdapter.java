package io.github.kbiakov.codeviewexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import io.github.kbiakov.codeview.adapters.AbstractCodeAdapter;
import io.github.kbiakov.codeview.highlight.ColorTheme;

public class CustomAdapter extends AbstractCodeAdapter<CustomAdapter.CustomModel> {

    public CustomAdapter(@NotNull Context context, @NotNull String content) {
        super(context, content, ColorTheme.DEFAULT.theme(), true, 10, context.getString(R.string.show_all), null);
    }

    @NotNull
    @Override
    public View createFooter(@NotNull Context context, CustomModel entity, boolean isFirst) {
        View footerView = LayoutInflater.from(context).inflate(R.layout.custom_footer, null);
        ((TextView) footerView.findViewById(R.id.tv_footer_title)).setText(entity.firstName);
        ((TextView) footerView.findViewById(R.id.tv_footer_description)).setText(entity.lastName);
        return footerView;
    }

    public static class CustomModel {
        private String firstName;
        private String lastName;

        public CustomModel(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }
}
