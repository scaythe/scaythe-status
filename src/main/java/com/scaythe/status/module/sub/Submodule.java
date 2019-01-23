package com.scaythe.status.module.sub;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Function;

public class Submodule<D, T> {

    // with pango 1.42.4 the spaces following fontawesome characters are displayed using fontawesome instead of the
    // usual monospace font, which breaks alignement, hence the fallback=false
    // https://gitlab.gnome.org/GNOME/pango/issues/273
    private static final String PATTERN = "{0}<span fallback=\"false\"> {1}</span>";
    private static final String COLOR_PATTERN = "<span foreground=\"{0}\">{1}</span>";

    private final String icon;
    private final Function<D, T> extractor;
    private final Function<T, String> formatter;
    private final Function<T, Optional<String>> foreground;

    public Submodule(
            String icon,
            Function<D, T> extractor,
            Function<T, String> formatter,
            Function<T, Optional<String>> foreground) {
        this.icon = icon;
        this.extractor = extractor;
        this.formatter = formatter;
        this.foreground = foreground;
    }

    public String format(D parentData) {
        T data = extractor.apply(parentData);

        String dataText = formatter.apply(data);
        Optional<String> color = foreground.apply(data);

        String submoduleText = formatSubmodule(dataText);

        return color.map(c -> formatWithColor(submoduleText, c)).orElse(submoduleText);
    }

    private String formatSubmodule(String data) {
        return MessageFormat.format(PATTERN, icon, data);
    }

    private String formatWithColor(String text, String color) {
        return MessageFormat.format(COLOR_PATTERN, color, text);
    }
}
