package org.xedox.mpde.editor;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.xedox.mpde.AppCore;
import org.xedox.utils.io.Assets;
import org.xedox.utils.io.FileX;
import org.xedox.utils.io.IFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SoraEditorManager {

    private static final String TEXTMATE_ASSET_PATH = "textmate";
    private static final String THEMES_ASSET_PATH = TEXTMATE_ASSET_PATH + "/themes";
    private static final String LANGUAGES_ASSET_PATH = TEXTMATE_ASSET_PATH + "/languages";

    public static String themesPath;
    public static String languagesPath;
    public static String textmateDirPath;
    public static String langsListFilePath;

    public static String currentTheme = "darcula"; // default

    public static void init(Context context) throws Exception {
        initializePaths(context);
        copyEditorAssets(context);
        initEditorThemes();
        generateLangsJson();
        GrammarRegistry.getInstance().loadGrammars(langsListFilePath);
        setTheme(currentTheme);
    }

    private static void initializePaths(Context context) {
        File textmateDir = AppCore.textmateDir().toFile();
        textmateDirPath = textmateDir.getAbsolutePath();
        themesPath = new File(textmateDir, "themes").getAbsolutePath();
        languagesPath = new File(textmateDir, "languages").getAbsolutePath();
        langsListFilePath = new File(textmateDir, "langs.json").getAbsolutePath();
    }

    public static File[] getThemesPathList() {
        File themesDir = new File(themesPath);
        return themesDir.exists() ? themesDir.listFiles() : new File[0];
    }

    public static File[] getLanguagePathList() {
        File langsDir = new File(languagesPath);
        return langsDir.exists() ? langsDir.listFiles() : new File[0];
    }

    private static void copyEditorAssets(Context context) throws IOException {
        Assets ass = Assets.from(context);
        ass.copyAssetsRecursive("textmate", new File(textmateDirPath));
    }

    public static void initEditorThemes() {
        FileProviderRegistry.getInstance().addFileProvider(new ResourceFileResolver());
        ThemeRegistry themeRegistry = ThemeRegistry.getInstance();

        for (File path : getThemesPathList()) {
            String name = path.getName().replaceAll(".json", "");

            try (InputStream is =
                    FileProviderRegistry.getInstance().tryGetInputStream(path.getAbsolutePath())) {
                if (is == null) {
                    throw new IOException(
                            "Could not get input stream for theme: " + path.getAbsolutePath());
                }

                IThemeSource source =
                        IThemeSource.fromInputStream(is, path.getAbsolutePath(), null);
                ThemeModel model = new ThemeModel(source, name);
                themeRegistry.loadTheme(model);
            } catch (Exception err) {
                throw new RuntimeException("Failed to load theme: " + path, err);
            }
        }
    }

    public static void setTheme(String name) {
        currentTheme = name == null ? "darcula" : name;
        ThemeRegistry.getInstance().setTheme(currentTheme);
    }

    public static String[] getLangSourcesList() {
        File[] sourceFiles = getThemesPathList();
        if (sourceFiles == null) {
            return new String[0];
        }

        List<String> validScopes = new ArrayList<>();
        Gson gson = new Gson();

        for (File path : sourceFiles) {
            if (path == null || !path.exists()) {
                continue;
            }

            try {
                String text = new FileX(path).read();
                if (text == null || text.isEmpty()) {
                    continue;
                }

                JsonObject json = gson.fromJson(text, JsonObject.class);
                if (json != null && json.has("scopeName")) {
                    String scopeName = json.get("scopeName").getAsString();
                    if (scopeName != null && !scopeName.trim().isEmpty()) {
                        validScopes.add(scopeName);
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }

        return validScopes.toArray(new String[0]);
    }

    private static String[] getThemesList() {
        File[] themeFiles = getThemesPathList();
        String[] themes = new String[themeFiles.length];
        Gson gson = new Gson();

        for (int i = 0; i < themeFiles.length; i++) {
            File path = themeFiles[i];
            try {
                String text = new FileX(path).read();
                JsonObject json = gson.fromJson(text, JsonObject.class);
                if (json != null && json.has("name")) {
                    themes[i] = json.get("name").getAsString();
                }
            } catch (Exception e) {
                themes[i] = path.getName().replace(".json", "");
            }
        }
        return themes;
    }

    private static void generateLangsJson() {
        File[] langsList = getLanguagePathList();
        if (langsList == null || langsList.length == 0) {
            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<TextmateLanguage> languages = new ArrayList<>();

        for (File langFolder : langsList) {
            try {
                String langName = langFolder.getName();
                String tmLanguageTemp = "%s.tmLanguage.json";
                String configurationTemp = "%s.configuration.json";

                IFile tmLanguageFile =
                        new FileX(langFolder, String.format(tmLanguageTemp, langName));
                IFile configurationFile =
                        new FileX(langFolder, String.format(configurationTemp, langName));

                JsonObject tmLanguageJson;
                tmLanguageJson = gson.fromJson(tmLanguageFile.read(), JsonObject.class);

                String scope = tmLanguageJson.get("scopeName").getAsString();
                String name = tmLanguageJson.get("name").getAsString();

                TextmateLanguage language = new TextmateLanguage();
                language.name = name;
                language.scopeName = scope;
                language.grammar = tmLanguageFile.getFullPath();
                language.languageConfiguration = configurationFile.getFullPath();

                languages.add(language);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to load lang from " + langFolder.getAbsolutePath(), e);
            }
        }

        JsonObject outputJson = new JsonObject();
        JsonArray languagesArray = gson.toJsonTree(languages).getAsJsonArray();
        outputJson.add("languages", languagesArray);

        try (FileWriter writer = new FileWriter(langsListFilePath)) {
            gson.toJson(outputJson, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write langs.json", e);
        }
    }

    private static class TextmateLanguage {
        public String name;
        public String scopeName;
        public String languageConfiguration;
        public String grammar;
    }
}
