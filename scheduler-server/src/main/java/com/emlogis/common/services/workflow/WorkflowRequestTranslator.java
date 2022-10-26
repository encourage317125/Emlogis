package com.emlogis.common.services.workflow;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by user on 22.07.15.
 */
@Startup
@Singleton
public class WorkflowRequestTranslator {

    private final static String FILE_NAME_EXTENSION = ".properties";
    private final static String DEFAULT_LOCALE_FOLDER = "i18n";

    private Map<String, Properties> propertiesMap = new HashMap<>();

    public String getMessage(Locale locale, String code, TranslationParam[] params) {
        Properties properties = propertiesMap.get(locale);

        if (properties == null) {
            try {
                properties = loadLocale(locale.getLanguage());
                propertiesMap.put(locale.getLanguage(), properties);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Can not load properties for locale %s", locale), e);
            }
        }

        String template = properties.getProperty(code);
        if (params != null) {
            Map<String, String> valueMap = new HashMap<>();
            for (TranslationParam param : params) {
                valueMap.put(param.name(), param.value());
            }
            return StrSubstitutor.replace(template, valueMap);
        } else {
            return template;
        }
    }

    private Properties loadLocale(String locale) throws IOException {
        Properties result = new Properties();

        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(DEFAULT_LOCALE_FOLDER + "/" + locale);
        if (url != null) {
            URLConnection conn = url.openConnection();
            VirtualFile virtualFolder = (VirtualFile) conn.getContent();

            List<VirtualFile> children = virtualFolder.getChildren(new VirtualFileFilter() {
                @Override
                public boolean accepts(VirtualFile virtualFile) {
                    return virtualFile.getName().endsWith(FILE_NAME_EXTENSION);
                }
            });

            for (VirtualFile file : children) {
                Properties properties = new Properties();
                properties.load(file.openStream());
                result.putAll(properties);
            }
        }

        return result;
    }

}
