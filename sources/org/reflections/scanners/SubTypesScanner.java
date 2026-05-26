package org.reflections.scanners;

import java.util.List;
import java.util.Map;
import javassist.bytecode.ClassFile;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/scanners/SubTypesScanner.class */
@Deprecated
public class SubTypesScanner extends AbstractScanner {
    @Override // org.reflections.scanners.AbstractScanner, org.reflections.scanners.Scanner
    public /* bridge */ /* synthetic */ String index() {
        return super.index();
    }

    @Deprecated
    public SubTypesScanner() {
        super(Scanners.SubTypes);
    }

    @Deprecated
    public SubTypesScanner(boolean excludeObjectClass) {
        super(excludeObjectClass ? Scanners.SubTypes : Scanners.SubTypes.filterResultsBy(s -> {
            return true;
        }));
    }

    @Override // org.reflections.scanners.AbstractScanner, org.reflections.scanners.Scanner
    public List<Map.Entry<String, String>> scan(ClassFile cls) {
        return this.scanner.scan(cls);
    }
}
