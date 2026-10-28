package studio.am.hamzaban.core;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * Read-only, memory-mapped bilingual index bundled with the app.
 * Format: 40-byte header, records "key\0value\n" sorted by FNV-1a hash,
 * then a sparse anchor table (hash, offset) every 8 records.
 */
public final class Lexicon {

    private static final String ASSET = "lexicon.bin";
    private static final String FILE = "lexicon.bin";
    private static final int HEADER = 40;
    private static final int SCAN_LIMIT = 96;

    private static volatile Lexicon instance;

    private ByteBuffer map;
    private int recordCount;
    private int sparseCount;
    private int sparseOffset;
    private long size;

    public static Lexicon get(Context c) throws IOException {
        if (instance == null) {
            synchronized (Lexicon.class) {
                if (instance == null) {
                    Lexicon lex = new Lexicon();
                    lex.open(c.getApplicationContext());
                    instance = lex;
                }
            }
        }
        return instance;
    }

    public static Lexicon ready() {
        return instance;
    }

    public static boolean isReady() {
        return instance != null;
    }

    public int recordCount() {
        return recordCount;
    }

    public long sizeBytes() {
        return size;
    }

    private void open(Context c) throws IOException {
        File file = new File(c.getFilesDir(), FILE);
        if (!file.exists() || file.length() < 4096) {
            extract(c, file);
        }
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        try {
            FileChannel channel = raf.getChannel();
            size = channel.size();
            map = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            map.order(ByteOrder.LITTLE_ENDIAN);
        } finally {
            raf.close();
        }
        byte[] magic = new byte[4];
        for (int i = 0; i < 4; i++) magic[i] = map.get(i);
        if (!"HZB1".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IOException("bad lexicon header");
        }
        recordCount = map.getInt(4);
        sparseCount = map.getInt(8);
        sparseOffset = map.getInt(16);
        if (recordCount <= 0 || sparseCount <= 0 || sparseOffset <= HEADER) {
            throw new IOException("bad lexicon index");
        }
    }

    private void extract(Context c, File target) throws IOException {
        File tmp = new File(target.getAbsolutePath() + ".tmp");
        InputStream raw = null;
        FileOutputStream out = null;
        try {
            raw = c.getAssets().open(ASSET);
            out = new FileOutputStream(tmp);
            byte[] buf = new byte[1 << 16];
            int n;
            while ((n = raw.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.flush();
        } finally {
            closeQuietly(raw);
            closeQuietly(out);
        }
        if (target.exists() && !target.delete()) {
            // the rename below reports the real failure
        }
        if (!tmp.renameTo(target)) {
            throw new IOException("cannot install lexicon");
        }
    }

    private static void closeQuietly(java.io.Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable ignored) {
        }
    }

    public String lookup(String key) {
        if (key == null || key.isEmpty() || map == null) return null;
        int h = hash(key);
        int lo = 0;
        int hi = sparseCount - 1;
        int anchor = 0;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int hv = map.getInt(sparseOffset + mid * 8);
            if (Integer.compareUnsigned(hv, h) <= 0) {
                anchor = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        int off = map.getInt(sparseOffset + anchor * 8 + 4);
        for (int i = 0; i < SCAN_LIMIT; i++) {
            if (off < HEADER || off >= size) return null;
            String k = readKey(off);
            int sep = indexOf(off, (byte) 0);
            if (sep < 0) return null;
            int end = indexOf(sep + 1, (byte) 10);
            if (end < 0) return null;
            int hk = hash(k);
            int cmp = Integer.compareUnsigned(hk, h);
            if (cmp > 0) return null;
            if (cmp == 0 && k.equals(key)) {
                return new String(slice(sep + 1, end), StandardCharsets.UTF_8);
            }
            off = end + 1;
        }
        return null;
    }

    private String readKey(int off) {
        int sep = indexOf(off, (byte) 0);
        if (sep < 0) return "";
        return new String(slice(off, sep), StandardCharsets.UTF_8);
    }

    private int indexOf(int from, byte value) {
        int limit = (int) Math.min(size, (long) from + 4096L);
        for (int i = from; i < limit; i++) {
            if (map.get(i) == value) return i;
        }
        return -1;
    }

    private byte[] slice(int from, int to) {
        byte[] out = new byte[Math.max(0, to - from)];
        for (int i = 0; i < out.length; i++) out[i] = map.get(from + i);
        return out;
    }

    public static int hash(String s) {
        int h = 0x811C9DC5;
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            h ^= (b & 0xFF);
            h *= 0x01000193;
        }
        return h;
    }
}
