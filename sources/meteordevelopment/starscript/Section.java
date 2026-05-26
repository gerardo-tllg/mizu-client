package meteordevelopment.starscript;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/Section.class */
public class Section {
    private static final ThreadLocal<StringBuilder> SB = ThreadLocal.withInitial(StringBuilder::new);
    public final int index;
    public final String text;
    public Section next;

    public Section(int index, String text) {
        this.index = index;
        this.text = text;
    }

    public String toString() {
        StringBuilder sb = SB.get();
        sb.setLength(0);
        Section section = this;
        while (true) {
            Section s = section;
            if (s != null) {
                sb.append(s.text);
                section = s.next;
            } else {
                return sb.toString();
            }
        }
    }
}
