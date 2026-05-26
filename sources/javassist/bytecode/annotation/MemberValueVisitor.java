package javassist.bytecode.annotation;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/annotation/MemberValueVisitor.class */
public interface MemberValueVisitor {
    void visitAnnotationMemberValue(AnnotationMemberValue annotationMemberValue);

    void visitArrayMemberValue(ArrayMemberValue arrayMemberValue);

    void visitBooleanMemberValue(BooleanMemberValue booleanMemberValue);

    void visitByteMemberValue(ByteMemberValue byteMemberValue);

    void visitCharMemberValue(CharMemberValue charMemberValue);

    void visitDoubleMemberValue(DoubleMemberValue doubleMemberValue);

    void visitEnumMemberValue(EnumMemberValue enumMemberValue);

    void visitFloatMemberValue(FloatMemberValue floatMemberValue);

    void visitIntegerMemberValue(IntegerMemberValue integerMemberValue);

    void visitLongMemberValue(LongMemberValue longMemberValue);

    void visitShortMemberValue(ShortMemberValue shortMemberValue);

    void visitStringMemberValue(StringMemberValue stringMemberValue);

    void visitClassMemberValue(ClassMemberValue classMemberValue);
}
