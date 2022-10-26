package com.emlogis.common;

import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Startup
@Singleton
public class ResourcesBundle {

    private final static String FILE_NAME_EXTENSION = ".properties";
    private final static String DEFAULT_LOCALE_FOLDER = "i18n";

    private Map<String, Properties> propertiesMap = new HashMap<>();

    public String getMessage(String locale, String code, Object... params) {
        Properties properties = propertiesMap.get(locale);

        if (properties == null) {
            try {
                properties = loadLocale(locale);

                propertiesMap.put(locale, properties);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Can not load properties for locale %s", locale), e);
            }
        }

        String message = (String) properties.get(code);
        if (params != null && params.length > 0) {
            return String.format(message, params);
        } else {
            return message;
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
