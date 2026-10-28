package studio.am.hamzaban.data;

public final class Entry {

    public final long id;
    public final String src;
    public final String tgt;
    public final String body;
    public final String out;
    public final long ts;
    public final boolean fav;

    public Entry(long id, String src, String tgt, String body, String out, long ts, boolean fav) {
        this.id = id;
        this.src = src;
        this.tgt = tgt;
        this.body = body;
        this.out = out;
        this.ts = ts;
        this.fav = fav;
    }
}
