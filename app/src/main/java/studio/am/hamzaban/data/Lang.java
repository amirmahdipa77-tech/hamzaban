package studio.am.hamzaban.data;

public final class Lang {

    public final String code;
    public final String nativeName;
    public final String fa;
    public final String en;

    public Lang(String code, String nativeName, String fa, String en) {
        this.code = code;
        this.nativeName = nativeName;
        this.fa = fa;
        this.en = en;
    }

    public String name(boolean persian) {
        return persian ? fa : en;
    }

    public String upperCode() {
        return code.toUpperCase(java.util.Locale.US);
    }
}
