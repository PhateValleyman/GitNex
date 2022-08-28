package org.mian.gitnex.helpers.codeeditor.markwon;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.apache.commons.lang3.EnumUtils;
import org.mian.gitnex.core.MainGrammarLocator;
import org.mian.gitnex.helpers.codeeditor.LanguageName;
import org.mian.gitnex.helpers.codeeditor.languages.Language;
import org.mian.gitnex.helpers.codeeditor.languages.LanguageElement;
import org.mian.gitnex.helpers.codeeditor.theme.Theme;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.noties.markwon.syntax.SyntaxHighlight;

/**
 * @author qwerty287
 */

public class SyntaxHighlighter implements SyntaxHighlight {

	@NonNull
	public static SyntaxHighlighter create(
		Context context,
		@NonNull Theme theme) {
		return new SyntaxHighlighter(context, theme, null);
	}

	@NonNull
	public static SyntaxHighlighter create(
		Context context,
		@NonNull Theme theme,
		@Nullable String fallback) {
		return new SyntaxHighlighter(context, theme, fallback);
	}

	private final Theme theme;
	private final Context context;
	private final String fallback;

	protected SyntaxHighlighter(
		Context context,
		@NonNull Theme theme,
		@Nullable String fallback) {
		this.context = context;
		this.theme = theme;
		this.fallback = fallback;
	}

	@NonNull
	@Override
	public CharSequence highlight(@Nullable String info, @NonNull String code) {
		if(code.isEmpty()) {
			return code;
		}

		if (info == null) {
			info = fallback;
		}

		if(info != null) {
			info = MainGrammarLocator.fromExtension(info).toUpperCase();
		}

		Editable highlightedCode = new SpannableStringBuilder(code);

		Language l;
		if(EnumUtils.isValidEnum(LanguageName.class, info)) {
			l = LanguageName.valueOf(info).getLanguage();
		}
		else {
			return code;
		}

		for(LanguageElement e : Objects.requireNonNull(LanguageElement.class.getEnumConstants())) {
			Pattern p = l.getPattern(e);
			if(p != null) {
				Matcher matcher = p.matcher(highlightedCode);
				while (matcher.find()) {
					highlightedCode.setSpan(new ForegroundColorSpan(context.getResources().getColor(theme.getColor(e), null)),
						matcher.start(), matcher.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		return highlightedCode;
	}

}
