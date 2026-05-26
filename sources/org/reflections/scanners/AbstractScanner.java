package org.reflections.scanners;

import java.util.List;
import java.util.Map;
import javassist.bytecode.ClassFile;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/scanners/AbstractScanner.class */
@Deprecated
class AbstractScanner implements Scanner {
    protected final Scanner scanner;

    AbstractScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override // org.reflections.scanners.Scanner
    public String index() {
        return this.scanner.index();
    }

    @Override // org.reflections.scanners.Scanner
    public List<Map.Entry<String, String>> scan(ClassFile cls) {
        return this.scanner.scan(cls);
    }
}
