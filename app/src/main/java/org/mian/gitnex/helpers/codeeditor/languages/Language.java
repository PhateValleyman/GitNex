package org.mian.gitnex.helpers.codeeditor.languages;

import android.content.Context;
import android.content.res.Resources;
import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.CodeView;
import org.mian.gitnex.helpers.codeeditor.theme.Theme;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author qwerty287
 */

public abstract class Language {

	private static HashMap<String, Language> languages = null;

	private static void initializeMap() {
		if(languages == null) {
			languages = new HashMap<>();
			languages.put("JAVA", new JavaLanguage());
			languages.put("PYTHON", new PythonLanguage());
			languages.put("GO", new GoLanguage());
			languages.put("PHP", new PhpLanguage());
			languages.put("XML", new XmlLanguage());
			languages.put("HTML", new HtmlLanguage());
		}
	}

	public static Language fromName(String name) {
		initializeMap();

		return isValid(name) ? languages.get(name.toUpperCase()) : new UnknownLanguage();
	}

	public static boolean isValid(String name) {
		initializeMap();

		return languages.containsKey(name.toUpperCase());
	}

	public abstract Pattern getPattern(LanguageElement element);

	public abstract Set<Character> getIndentationStarts();

	public abstract Set<Character> getIndentationEnds();

	public abstract String[] getKeywords();

	public abstract List<Code> getCodeList();

	public abstract String getName();

	public void applyTheme(Context context, CodeView codeView, Theme theme) {
		codeView.resetSyntaxPatternList();
		codeView.resetHighlighter();

		Resources resources = context.getResources();

		// View Background
		codeView.setBackgroundColor(resources.getColor(theme.getBackgroundColor(), null));

		// Syntax Colors
		for(LanguageElement e : Objects.requireNonNull(LanguageElement.class.getEnumConstants())) {
			Pattern p = getPattern(e);
			if(p != null) {
				codeView.addSyntaxPattern(p, resources.getColor(theme.getColor(e), null));
			}
		}

		// Default Color
		codeView.setTextColor(resources.getColor(theme.getDefaultColor(), null));

		codeView.reHighlightSyntax();
	}

}
