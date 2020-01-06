package io.virtdata.docsys.api;

/**
 * At runtime, any instances of this service will be used to find
 * paths to be hosted as static content.
 */
public interface DocsysStaticManifest {
    DocNameSpacesBinder getDocs();
}
