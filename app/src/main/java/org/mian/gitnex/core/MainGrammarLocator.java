package org.mian.gitnex.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Set;
import io.noties.prism4j.GrammarLocator;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.PrismBundle;

/**
 * @author opyale
 */

@PrismBundle(
	includeAll = true,
	grammarLocatorClassName = ".DefaultGrammarLocator"
)
public class MainGrammarLocator implements GrammarLocator {

	public static final String DEFAULT_FALLBACK_LANGUAGE = "clike";

	private static final DefaultGrammarLocator defaultGrammarLocator = new DefaultGrammarLocator();
	private static volatile MainGrammarLocator instance;

	private MainGrammarLocator() {}

	public String fromExtension(String extension) {

		switch(extension.toLowerCase()) {

			case "b":
			case "bf":
				return "brainfuck";

			case "c":
			case "h":
			case "hdl":
				return "c";

			case "clj":
			case "cljs":
			case "cljc":
			case "edn":
				return "clojure";

			case "cc":
			case "cpp":
			case "cxx":
			case "c++":
			case "hh":
			case "hpp":
			case "hxx":
			case "h++":
				return "cpp";

			case "cs":
			case "csx":
				return "csharp";

			case "groovy":
			case "gradle":
			case "gvy":
			case "gy":
			case "gsh":
				return "groovy";

			case "js":
			case "cjs":
			case "mjs":
				return "javascript";

			case "kt":
			case "kts":
			case "ktm":
				return "kotlin";

			case "md":
				return "markdown";

			case "xml":
			case "html":
			case "htm":
			case "mathml":
			case "svg":
				return "markup";

			case "py":
			case "pyi":
			case "pyc":
			case "pyd":
			case "pyo":
			case "pyw":
			case "pyz":
				return "python";

			case "scala":
			case "sc":
				return "scala";

			case "yaml":
			case "yml":
				return "yaml";

		}

		return extension;

	}

	@Nullable
	@Override
	public Prism4j.Grammar grammar(@NotNull Prism4j prism4j, @NotNull String language) {

		return defaultGrammarLocator.grammar(prism4j, language);
	}

	@NotNull
	@Override
	public Set<String> languages() {

		return defaultGrammarLocator.languages();
	}

	public static MainGrammarLocator getInstance() {

		if(instance == null) {
			synchronized(MainGrammarLocator.class) {
				if(instance == null) {
					instance = new MainGrammarLocator();
				}
			}
		}

		return instance;

	}

}
