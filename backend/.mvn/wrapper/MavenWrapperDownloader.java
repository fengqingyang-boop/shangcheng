/*
 * Copyright 2007-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.util.Properties;

public class MavenWrapperDownloader {

    private static final String WRAPPER_VERSION = "3.2.0";
    private static final String DEFAULT_MAVEN_VERSION = "3.9.5";
    private static final String MAVEN_USER_HOME = System.getProperty("user.home") + "/.m2";
    private static final String MAVEN_REPO = System.getProperty("maven.repo.local") != null
            ? System.getProperty("maven.repo.local")
            : (MAVEN_USER_HOME + "/repository");

    private static final String DISTRIBUTION_URL_PROPERTY = "distributionUrl";
    private static final String MAVEN_WRAPPER_PROPERTIES_PATH = ".mvn/wrapper/maven-wrapper.properties";

    public static void main(String[] args) {
        try {
            String mavenVersion = DEFAULT_MAVEN_VERSION;
            String distributionUrl = null;
            String wrapperJar = "maven-wrapper.jar";
            File propertiesFile = new File(MAVEN_WRAPPER_PROPERTIES_PATH);
            if (propertiesFile.exists()) {
                Properties properties = new Properties();
                try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                    properties.load(fis);
                }
                distributionUrl = properties.getProperty(DISTRIBUTION_URL_PROPERTY);
                if (distributionUrl != null && !distributionUrl.isEmpty()) {
                    System.out.println("- Distributin URL: " + distributionUrl);
                }
            }

            String baseUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/"
                    + WRAPPER_VERSION + "/maven-wrapper-" + WRAPPER_VERSION + ".jar";

            File jarFile = new File(MAVEN_REPO, "org/apache/maven/wrapper/maven-wrapper/" + WRAPPER_VERSION + "/maven-wrapper-" + WRAPPER_VERSION + ".jar");
            if (!jarFile.exists()) {
                jarFile.getParentFile().mkdirs();
                System.out.println("Downloading maven-wrapper from: " + baseUrl);
                downloadFile(baseUrl, jarFile);
            } else {
                System.out.println("Maven wrapper already exists: " + jarFile.getAbsolutePath());
            }

            File destFile = new File(".mvn/wrapper/maven-wrapper.jar");
            destFile.getParentFile().mkdirs();
            try (
                FileInputStream fis = new FileInputStream(jarFile);
                FileOutputStream fos = new FileOutputStream(destFile)
            ) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
            System.out.println("Maven wrapper installed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void downloadFile(String urlString, File destination) throws Exception {
        URL url = new URL(urlString);
        try (
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(destination)
        ) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }
}
