package com.emlogis.common;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.jboss.vfs.VirtualFile;

public class FileUtil {

    private final static Logger logger = Logger.getLogger(FileUtil.class);

    public static File getFileFromVirtualFileSystem(String fileName) {
        File retFile = null;
        try {
            URL url = new Object() {
            }.getClass().getClassLoader().getResource(fileName);
            URLConnection conn = url.openConnection();
            VirtualFile virtualFile = (VirtualFile) conn.getContent();
            retFile = virtualFile.getPhysicalFile();
        } catch (Exception error) {
            logger.error("Error while reading file from WILDLFY Virtual file system", error);

        }

        return retFile;
    }

    public static String readFileContentByName(String fileName) throws URISyntaxException, IOException {
        try {
            StringBuilder result = new StringBuilder("");
            File file = getFileFromVirtualFileSystem(fileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
            return new String(result.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("Error while reading file contents", exception);
            throw new RuntimeException("Error while reading file contents");
        }
    }

    public static String readPhysicialFileContentByName(String fileName) throws URISyntaxException, IOException {
        StringBuilder result = new StringBuilder("");
        File file = new File(fileName);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {

            logger.error("Error while reading templates for notifications", e);
        }
        return new String(result.toString());
    }

    public static File[] getDirectories(File file) {
        return file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    public static File[] getFiles(File file) {
        return file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
    }

}
