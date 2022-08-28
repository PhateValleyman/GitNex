package org.mian.gitnex.helpers.codeeditor;

import org.mian.gitnex.helpers.codeeditor.languages.*;
import java.util.HashMap;

/**
 * @author AmrDeveloper
 * @author M M Arif
 */

public enum LanguageName {
	UNKNOWN, // no language is specified or app currently does not support the mentioned language
	JAVA, // java
	PYTHON,
	GO, // go lang
	PHP, // php
	XML, // xml
	HTML; // html

	private static final HashMap<LanguageName, Language> languages = new HashMap<>();
	static {
		languages.put(JAVA, new JavaLanguage());
		languages.put(PYTHON, new PythonLanguage());
		languages.put(GO, new GoLanguage());
		languages.put(PHP, new PhpLanguage());
		languages.put(XML, new XmlLanguage());
		languages.put(HTML, new HtmlLanguage());
	}

	public Language getLanguage() {
		return languages.get(this);
	}
}
