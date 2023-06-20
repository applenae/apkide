package com.apkide.language.impl.javascript;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apkide.language.api.Language;
import com.apkide.language.api.Highlighter;
import com.apkide.language.api.DefaultHighlighter;

public class JavaScriptLanguage implements Language {
	@NonNull
	@Override
	public String getName() {
		return "Java Script";
	}
	
	private Highlighter highlighter;
	
	@Nullable
	@Override
	public Highlighter getHighlighter() {
		if (highlighter == null)
			highlighter = new DefaultHighlighter(new JavaScriptLexer());
		return highlighter;
	}
	
	@NonNull
	@Override
	public String[] getDefaultFilePatterns() {
		return new String[]{".js"};
	}
}
