package org.mian.gitnex.helpers.codeeditor.markwon;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.mian.gitnex.helpers.codeeditor.theme.Theme;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;

public class MarkwonHighlighter extends AbstractMarkwonPlugin {
	@NonNull
	public static MarkwonHighlighter create(
		Context context,
		@NonNull Theme theme) {
		return create(context, theme, null);
	}

	@NonNull
	public static MarkwonHighlighter create(
		Context context,
		@NonNull Theme theme,
		@Nullable String fallbackLanguage) {
		return new MarkwonHighlighter(context, theme, fallbackLanguage);
	}

	private final Theme theme;
	private final Context context;
	private final String fallbackLanguage;

	public MarkwonHighlighter(
		Context context,
		@NonNull Theme theme,
		@Nullable String fallbackLanguage) {
		this.theme = theme;
		this.context = context;
		this.fallbackLanguage = fallbackLanguage;
	}

	@Override
	public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
		builder
			.codeTextColor(context.getResources().getColor(theme.getDefaultColor(), null))
			.codeBackgroundColor(context.getResources().getColor(theme.getBackgroundColor(), null));
	}

	@Override
	public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
		builder.syntaxHighlight(SyntaxHighlighter.create(context, theme, fallbackLanguage));
	}
}
