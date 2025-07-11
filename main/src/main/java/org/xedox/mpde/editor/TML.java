package org.xedox.mpde.editor;

import android.content.Context;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import java.util.HashMap;
import java.util.Map;

public class TML extends TextMateLanguage {

    private static final Map<String, String> SCOPE_MAPPING = new HashMap<>();

    static {
        SCOPE_MAPPING.put("source.txt", "text");
        SCOPE_MAPPING.put("source.json", "json");
    }

    private final String scope;
    private final Context context;

    public TML(Context context, String scopeName) {
        super(
                GrammarRegistry.getInstance().findGrammar(scopeName),
                GrammarRegistry.getInstance().findLanguageConfiguration(scopeName),
                GrammarRegistry.getInstance(),
                ThemeRegistry.getInstance(),
                true);
        this.scope = scopeName;
        this.context = context;
    }
}
