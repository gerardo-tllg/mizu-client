package javassist;

import javassist.CtField;

/* JADX INFO: compiled from: CtClassType.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/FieldInitLink.class */
class FieldInitLink {
    FieldInitLink next = null;
    CtField field;
    CtField.Initializer init;

    FieldInitLink(CtField f, CtField.Initializer i) {
        this.field = f;
        this.init = i;
    }
}
