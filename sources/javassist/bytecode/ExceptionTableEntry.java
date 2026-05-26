package javassist.bytecode;

/* JADX INFO: compiled from: ExceptionTable.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/ExceptionTableEntry.class */
class ExceptionTableEntry {
    int startPc;
    int endPc;
    int handlerPc;
    int catchType;

    ExceptionTableEntry(int start, int end, int handle, int type) {
        this.startPc = start;
        this.endPc = end;
        this.handlerPc = handle;
        this.catchType = type;
    }
}
