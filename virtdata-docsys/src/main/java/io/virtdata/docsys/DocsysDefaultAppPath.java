package io.virtdata.docsys;

import io.virtdata.docsys.api.Docs;
import io.virtdata.docsys.api.DocNameSpacesBinder;
import io.virtdata.docsys.api.DocsysStaticManifest;

//@Service(DocsysStaticManifest.class) loaded conditionally by explicit reference
public class DocsysDefaultAppPath implements DocsysStaticManifest {

    @Override
    public DocNameSpacesBinder getDocs() {
        return new Docs().namespace("docsys-default-app").addFirstFoundPath(
                "virtdata-docsys/src/main/resources/docsys-guidebook/",
                "docsys-guidebook/")
                .setEnabledByDefault(true)
                .asDocsInfo();
    }
}
