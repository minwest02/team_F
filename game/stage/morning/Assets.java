package game.stage.morning;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.InputStream;
import java.util.*;

public class Assets {

    private static final Map<String, Image> cache = new HashMap<>();
    private static final Set<String> missingLogged = new HashSet<>();

    public static Image get(String key) {
        if (key == null || key.isBlank()) return null;

        Image cached = cache.get(key);
        if (cached != null) return cached;

        Image img = tryLoad(key);
        if (img != null) cache.put(key, img);
        return img;
    }

    private static Image tryLoad(String key) {
        List<String> keyVariants = buildKeyVariants(key);

        String[] bases = new String[] {
                "/assets/images/morning/",
                "/assets/images/",
                "/assets/",
                "/game/stage/morning/",
                "/"
        };

        String[] exts = new String[] { ".png", ".jpg", ".jpeg" };

        for (String kv : keyVariants) {
            for (String base : bases) {
                for (String ext : exts) {
                    String path = base + kv + ext;
                    try (InputStream in = Assets.class.getResourceAsStream(path)) {
                        if (in == null) continue;
                        return ImageIO.read(in);
                    } catch (Exception ignored) {}
                }
            }
        }

        if (missingLogged.add(key)) {
            System.out.println("[Assets] missing image for key=" + key
                    + " (searched: " + summarize(keyVariants) + ")");
        }
        return null;
    }

    private static List<String> buildKeyVariants(String key) {
        List<String> out = new ArrayList<>();
        String k = normalize(key);

        addUnique(out, k);
        addUnique(out, k.toLowerCase(Locale.ROOT));

        int slash = k.lastIndexOf('/');
        if (slash >= 0 && slash < k.length() - 1) {
            String tail = k.substring(slash + 1);
            addUnique(out, tail);
            addUnique(out, tail.toLowerCase(Locale.ROOT));
            addUnique(out, capitalizeFirst(tail));
        }

        addUnique(out, capitalizeFirst(k));
        return out;
    }

    private static String normalize(String key) {
        String k = key;
        if (k.startsWith("/")) k = k.substring(1);

        k = stripExt(k, ".png");
        k = stripExt(k, ".jpg");
        k = stripExt(k, ".jpeg");
        return k;
    }

    private static String stripExt(String s, String ext) {
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.endsWith(ext)) return s.substring(0, s.length() - ext.length());
        return s;
    }

    private static void addUnique(List<String> list, String v) {
        if (v == null || v.isBlank()) return;
        if (!list.contains(v)) list.add(v);
    }

    private static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        char c = s.charAt(0);
        if (Character.isUpperCase(c)) return s;
        return Character.toUpperCase(c) + s.substring(1);
    }

    private static String summarize(List<String> keys) {
        int n = Math.min(keys.size(), 6);
        return keys.subList(0, n).toString() + (keys.size() > n ? "..." : "");
    }
}
