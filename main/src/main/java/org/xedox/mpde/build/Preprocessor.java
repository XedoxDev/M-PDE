package org.xedox.mpde.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xedox.utils.io.FileX;

public class Preprocessor {

    public static Pattern importPattern =
            Pattern.compile("^import\\s+(static\\s+)?[a-zA-Z\\d_\\.]+\\s*;$", Pattern.MULTILINE);
    public static Pattern packagePattern = Pattern.compile("^package\\s+[\\d_\\.a-zA-Z]+\\s*;$", Pattern.MULTILINE);

    private String code;

    public String preprocess(String code, String className) {
        this.code = code;
        String packageStr = getPackage(code);
        List<String> imports = getImports(code);
        return buildJavaCode(code, imports, packageStr, className);
    }
    
    public String preprocess(FileX file) throws IOException {
        return preprocess(file.read(), file.getNameNoExtension());
    }
    
    private String buildJavaCode(
            String code, List<String> imports, String packageStr, String className) {
        StringBuilder java = new StringBuilder();
        if (packageStr != null) {
            java.append(packageStr).append("\n\n");
        }
        imports.add("import processing.core.*;");
        for (String importStr : imports) {
            java.append(importStr).append("\n");
        }
        java.append("\n");

        java.append("public class ").append(className).append(" extends PApplet {\n");
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (!importPattern.matcher(line).matches() && 
                !(packageStr != null && packagePattern.matcher(line).matches())) {
                java.append("\t").append(line).append("\n");
            }
        }
        java.append("}");
        return java.toString();
    }

    private List<String> getImports(String code) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = importPattern.matcher(code);
        while (matcher.find()) {
            imports.add(matcher.group().trim());
        }
        return imports;
    }

    private String getPackage(String code) {
        Matcher matcher = packagePattern.matcher(code);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }
}