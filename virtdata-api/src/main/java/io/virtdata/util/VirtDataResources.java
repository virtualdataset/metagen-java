/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.virtdata.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Collectors;

public class VirtDataResources {
    public final static String DATA_DIR = "data";
    private final static Logger logger = LoggerFactory.getLogger(VirtDataResources.class);

    public static CharBuffer readDataFileToCharBuffer(String basename) {
        return loadFileToCharBuffer(basename, DATA_DIR);
    }

    public static List<String> readDataFileLines(String basename) {
        return readFileLines(basename, DATA_DIR);
    }

    public static String readDataFileString(String basename) {
        return readFileString(basename, DATA_DIR);
    }

    public static InputStream findRequiredStreamOrFile(String basename, String extension, String... searchPaths) {
        Optional<InputStream> optionalStreamOrFile = findOptionalStreamOrFile(basename, extension, searchPaths);
        return optionalStreamOrFile.orElseThrow(() -> new RuntimeException(
                "Unable to find " + basename + " with extension " + extension + " in file system or in classpath, with"
                        + " search paths: " + Arrays.stream(searchPaths).collect(Collectors.joining(","))
        ));
    }

    public static Reader findRequiredReader(String basename, String extension, String... searchPaths) {
        Optional<Reader> optionalReader = findOptionalReader(basename, extension, searchPaths);
        return optionalReader.orElseThrow(() -> new RuntimeException(
                "Unable to find " + basename + " with extension " + extension + " in file system or in classpath, with"
                        + " search paths: " + Arrays.stream(searchPaths).collect(Collectors.joining(","))
        ));
    }

    public static Optional<Reader> findOptionalReader(String basename, String extenion, String... searchPaths) {
        return findOptionalStreamOrFile(basename, extenion, searchPaths)
                .map(InputStreamReader::new)
                .map(BufferedReader::new);
    }

    public static Optional<Path> findOptionalDirPath(String pathName) {
        URL systemResource = ClassLoader.getSystemResource(pathName);

        if (systemResource != null) {
            try {
                return Optional.of(Path.of(systemResource.toURI()));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.empty();
    }

    public static Optional<InputStream> findOptionalStreamOrFile(String basename, String extension, String... searchPaths) {

        boolean needsExtension = (extension != null && !extension.isEmpty() && !basename.endsWith("." + extension));
        String filename = basename + (needsExtension ? "." + extension : "");

        ArrayList<String> paths = new ArrayList<String>() {{
            add(filename);
            if (!isRemote(basename)) {
                addAll(Arrays.stream(searchPaths).map(s -> s + File.separator + filename)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }
        }};

        for (String path : paths) {
            Optional<InputStream> stream = getInputStream(path);
            if (stream.isPresent()) {
                return stream;
            }
        }

        return Optional.empty();
    }

    private static boolean isRemote(String path) {
        return (path.toLowerCase().startsWith("http:")
                || path.toLowerCase().startsWith("https:"));
    }

    public static Optional<InputStream> getInputStream(String path) {

        // URLs, if http: or https:
        if (isRemote(path)) {
            Optional<InputStream> inputStream = getInputStreamForUrl(path);
            if (inputStream != null) return inputStream;
        }

        // Files
        try {
            InputStream stream = new FileInputStream(path);
            return Optional.of(stream);
        } catch (FileNotFoundException ignored) {
        }

        // Classpath
        ClassLoader classLoader = VirtDataResources.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(path);
        if (stream != null) {
            return Optional.of(stream);
        }

        return Optional.empty();
    }

    public static Optional<InputStream> getInputStreamForUrl(String path) {
        URL url;
        try {
            url = new URL(path);
            InputStream inputStream = url.openStream();
            if (inputStream != null) {
                return Optional.of(inputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public static List<String> readFileLines(String basename, String... searchPaths) {
        InputStream requiredStreamOrFile = findRequiredStreamOrFile(basename, "", DATA_DIR);
        try (BufferedReader buffer = new BufferedReader((new InputStreamReader(requiredStreamOrFile)))) {
            List<String> collected = buffer.lines().collect(Collectors.toList());
            return collected;
        } catch (IOException ioe) {
            throw new RuntimeException("Error while reading required file to string", ioe);
        }
    }

    public static String readFileString(String basename, String... searchPaths) {
        InputStream requiredStreamOrFile = findRequiredStreamOrFile(basename, "", searchPaths);
        try (BufferedReader buffer = new BufferedReader((new InputStreamReader(requiredStreamOrFile)))) {
            String filedata = buffer.lines().collect(Collectors.joining("\n"));
            return filedata;
        } catch (IOException ioe) {
            throw new RuntimeException("Error while reading required file to string", ioe);
        }
    }

    public static CSVParser readFileCSV(String basename, String... searchPaths) {
        Reader reader = findRequiredReader(basename, "csv", searchPaths);
        CSVFormat format = CSVFormat.newFormat(',').withFirstRecordAsHeader();
        try {
            CSVParser parser = new CSVParser(reader, format);
            return parser;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static CharBuffer loadFileToCharBuffer(String filename, String... searchPaths) {
        InputStream stream = findRequiredStreamOrFile(filename, "", searchPaths);

        CharBuffer linesImage;
        try {
            InputStreamReader isr = new InputStreamReader(stream);
            linesImage = CharBuffer.allocate(1024 * 1024);
            while (isr.read(linesImage) > 0) {
            }
            isr.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        linesImage.flip();
        return linesImage.asReadOnlyBuffer();
    }

    /**
     * <p>Look in all the provided path specifiers for an extant Path, and return
     * the first one found.</p>
     *
     * <p>If the final character of any path specifier is the default file
     * separator, then the request is for a directory. During searching,
     * if a directory is found when a file is requested, or vice-versa, then
     * an error is thrown withouth looking further.</p>
     *
     * <p>The locations that are searched include:</p>
     * <OL>
     * <LI>URLs. If the path specifier is a URI, then it is checked for a positive response
     * before the path is returned. URLs can not be used for directories.</LI>
     * <LI>The local filesystem, starting from the current directory of the process.</LI>
     * <LI>The class path.</LI>
     * </OL>
     *
     * @param pathspecs A specifier for a URL, a directory with a trailing slash, or a file
     *                  with no trailing slash.
     * @return A Path
     * @throws RuntimeException if none of the specified paths is found in any of the locations
     */
    public static Path findPathIn(String... pathspecs) {

        for (String pathspec : pathspecs) {
            Path foundPath = null;

            if (isRemote(pathspec)) {
                try {
                    Optional<InputStream> inputStreamForUrl = getInputStreamForUrl(pathspec);
                    if (inputStreamForUrl.isPresent()) {
                        foundPath = Path.of(URI.create(pathspec));
                        logger.debug("Found accessible remote file at " + foundPath.toString());
                    }
                } catch (Exception ignored) {
                }
            } else {
                boolean wantsADirectory = pathspec.endsWith(FileSystems.getDefault().getSeparator());
                String candidatePath = wantsADirectory ? pathspec.substring(0, pathspec.length() - 1) : pathspec;
                Path candidate = Path.of(candidatePath);
                try {
                    FileSystemProvider provider = candidate.getFileSystem().provider();
                    provider.checkAccess(candidate, AccessMode.READ);
                    BasicFileAttributes attrs = provider.readAttributes(candidate, BasicFileAttributes.class);
                    boolean foundADirectory = attrs.isDirectory();
                    if (wantsADirectory != foundADirectory) {
                        throw new RuntimeException("for path " + pathspec + ", user wanted a " +
                                (wantsADirectory ? "directory" : "file") + ", but found a " +
                                (foundADirectory ? "directory" : "file") + " while searching paths " +
                                Arrays.toString(pathspecs));
                    }
                    foundPath = candidate;
                } catch (Exception ignored) {
                }
                if (foundPath == null) {
                    try {
                        URL url = ClassLoader.getSystemResource(candidatePath);
                        if (url != null) {
                            URI uri = URI.create(url.toExternalForm());
                            foundPath = getPathInFilesystem(uri);
                            logger.debug("Found path in classpath: " + candidatePath + ": " + foundPath.toString());
                        }
                    } catch (Exception e) {
                        logger.trace("Error while looking in classpath for " + e.getMessage(), e);
                    }

                }
            }

            if (foundPath != null) {
                return foundPath;
            }
        }
        throw new RuntimeException("Unable to find path in " + Arrays.toString(pathspecs));
    }

    private synchronized static Path getPathInFilesystem(URI uri) {
        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException ignored) {
            try {
                fileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Path.of(uri);
    }

    public static CSVParser readDelimFile(String basename, char delimiter, String... searchPaths) {
        Reader reader = findRequiredReader(basename, "csv", searchPaths);
        CSVFormat format = CSVFormat.newFormat(delimiter).withFirstRecordAsHeader();
        try {
            CSVParser parser = new CSVParser(reader, format);
            return parser;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
