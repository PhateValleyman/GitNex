package org.mian.gitnex.helpers.codeeditor.languages;

import android.content.Context;
import android.content.res.Resources;
import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.CodeView;
import org.mian.gitnex.helpers.codeeditor.theme.Theme;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author qwerty287
 */

public abstract class Language {
	public abstract Pattern getPattern(LanguageElement element);
	public abstract Set<Character> getIndentationStarts();
	public abstract Set<Character> getIndentationEnds();
	public abstract String[] getKeywords();
	public abstract List<Code> getCodeList();

	public void applyTheme(Context context, CodeView codeView, Theme theme) {
		codeView.resetSyntaxPatternList();
		codeView.resetHighlighter();

		Resources resources = context.getResources();

		//View Background
		codeView.setBackgroundColor(resources.getColor(theme.getBackgroundColor(), null));

		//Syntax Colors
		for(LanguageElement e : Objects.requireNonNull(LanguageElement.class.getEnumConstants())) {
			Pattern p = getPattern(e);
			if(p != null) {
				codeView.addSyntaxPattern(p, resources.getColor(theme.getColor(e), null));
			}
		}

		//Default Color
		codeView.setTextColor(resources.getColor(theme.getDefaultColor(), null));

		codeView.reHighlightSyntax();
	}
}
