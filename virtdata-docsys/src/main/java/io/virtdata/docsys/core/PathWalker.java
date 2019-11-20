package io.virtdata.docsys.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;

public class PathWalker {
    private final static Logger logger = LoggerFactory.getLogger(PathWalker.class);

    public static void walk(Path p, PathVisitor v) {
        walk(p,v,PathWalker.WALK_ALL);
    }

    public static List<Path> findAll(Path p) {
        Collect fileCollector = new Collect(true, false);
        walk(p, fileCollector);
        return fileCollector.get();

    }

    public static void walk(Path p, PathVisitor v, DirectoryStream.Filter<Path> filter) {
        try {
            FileSystemProvider provider = p.getFileSystem().provider();
            DirectoryStream<Path> paths = provider.newDirectoryStream(p, (Path r) -> true);
            List<Path> pathlist = new ArrayList<>();
            for (Path path : paths) {
                pathlist.add(path);
            }
            for (Path path : pathlist) {
                if (path.getFileSystem().provider().readAttributes(path, BasicFileAttributes.class).isDirectory()) {
                    v.preVisitDir(path);
                    walk(path, v, filter);
                    v.postVisitDir(path);
                } else if (filter.accept(path)) {
                    v.preVisitFile(path);
                    v.visit(path);
                    v.postVisitFile(path);

                } else {
                    logger.error("error");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface PathVisitor {
        void visit(Path p);
        default void preVisitFile(Path path) {}
        default void postVisitFile(Path path) {}
        default void preVisitDir(Path path) {}
        default void postVisitDir(Path path) {}
    }

    public static DirectoryStream.Filter<Path> WALK_ALL = entry -> true;

    public static class Collect implements PathVisitor {
        private final List<Path> listing = new ArrayList<>();
        private final boolean collectFiles;
        private final boolean collectDirectories;

        public Collect(boolean collectFiles, boolean collectDirectories) {

            this.collectFiles = collectFiles;
            this.collectDirectories = collectDirectories;
        }

        public List<Path> get() {
            return listing;
        }

        @Override
        public void visit(Path p) {
        }

        @Override
        public void preVisitFile(Path path) {
            if (this.collectFiles) {
                listing.add(path);
            }
        }

        @Override
        public void preVisitDir(Path path) {
            if (this.collectDirectories) {
                listing.add(path);
            }
        }
    }

}
