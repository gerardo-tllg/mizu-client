package org.reflections.scanners;

import java.util.List;
import javassist.bytecode.ClassFile;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/scanners/ResourcesScanner.class */
@Deprecated
public class ResourcesScanner extends AbstractScanner {
    @Override // org.reflections.scanners.AbstractScanner, org.reflections.scanners.Scanner
    public /* bridge */ /* synthetic */ List scan(ClassFile classFile) {
        return super.scan(classFile);
    }

    @Override // org.reflections.scanners.AbstractScanner, org.reflections.scanners.Scanner
    public /* bridge */ /* synthetic */ String index() {
        return super.index();
    }

    @Deprecated
    public ResourcesScanner() {
        super(Scanners.Resources);
    }
}
