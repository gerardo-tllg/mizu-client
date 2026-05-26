package javassist;

/* JADX INFO: compiled from: ClassPoolTail.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/ClassPathList.class */
final class ClassPathList {
    ClassPathList next;
    ClassPath path;

    ClassPathList(ClassPath p, ClassPathList n) {
        this.next = n;
        this.path = p;
    }
}
