import com.qrlib.QRCodeGenerator;
import com.qrlib.QRCodeGeneratorBuilder;
import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.config.VersionSelector;

import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Benchmark harness for FastQRGenerator. Measures {@code generate()} per QR version, with the
 * payload filling each version's byte-mode capacity. The iteration count is calibrated adaptively
 * so fast and slow versions each run for a similar wall-time budget. Timing comes from
 * {@link System#nanoTime()} and per-call allocation from {@link ThreadMXBean}.
 *
 * <pre>
 *   java -cp target/classes:&lt;dir&gt; Benchmark [versions] [targetMillis] [format]
 *   java -cp target/classes:&lt;dir&gt; Benchmark 1,5,10,20,40 1500 table
 *   java -cp target/classes:&lt;dir&gt; Benchmark 1,10,40 2000 csv
 * </pre>
 *
 * <p>{@code format} is {@code table} (default, human-readable) or {@code csv} (machine-readable).
 * Progress notes go to stderr so stdout stays clean for piping.
 */
public class Benchmark {

    private static final ThreadMXBean THREAD_MX = (ThreadMXBean) ManagementFactory.getThreadMXBean();
    private static final String SEED = "FastQRGenerator ";
    private static final int WARMUP_ITERATIONS = 200;
    private static final int CALIBRATION_ITERATIONS = 20;
    private static final long MIN_ITERATIONS = 30;
    private static final long MAX_ITERATIONS = 50_000;

    private static final String[] HEADERS = { "Version", "Matrix", "Time/op", "Throughput", "Alloc/op", "Payload" };
    private static final String CSV_HEADER = "version,modules,ns_per_op,ops_per_sec,kb_per_op,payload_bytes";

    // Consume results so the JIT cannot optimize the generate() calls away.
    private static long blackhole = 0;

    public static void main(String[] args) {
        int[] versions = { 1, 5, 10, 20, 40 };
        long targetMillis = 1500;
        String format = "table";

        if (args.length >= 1 && !args[0].isEmpty()) {
            String[] parts = args[0].split(",");
            versions = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                versions[i] = Integer.parseInt(parts[i].trim());
            }
        }
        if (args.length >= 2 && !args[1].isEmpty()) {
            targetMillis = Long.parseLong(args[1].trim());
        }
        if (args.length >= 3 && !args[2].isEmpty()) {
            format = args[2].trim().toLowerCase(Locale.ROOT);
        }

        List<Result> results = new ArrayList<>();
        for (int version : versions) {
            results.add(benchmark(version, targetMillis * 1_000_000L));
        }

        if ("csv".equals(format)) {
            printCsv(results);
        } else {
            printTable(results);
        }
        System.err.println("blackhole=" + blackhole);
    }

    private static Result benchmark(int versionValue, long targetNanos) {
        QRCodeVersion version = QRCodeVersion.valueOf("V" + versionValue);
        int payloadBytes = VersionSelector.byteCapacity(version, ECCLevel.M);
        String payload = payload(payloadBytes);

        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(version)
                .eccLevel(ECCLevel.M)
                .build();

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            blackhole += generate(generator, payload);
        }

        long calibrationStart = System.nanoTime();
        for (int i = 0; i < CALIBRATION_ITERATIONS; i++) {
            blackhole += generate(generator, payload);
        }
        long estimatedNsPerOp = Math.max(1, (System.nanoTime() - calibrationStart) / CALIBRATION_ITERATIONS);
        long iterations = clamp(targetNanos / estimatedNsPerOp, MIN_ITERATIONS, MAX_ITERATIONS);

        long threadId = Thread.currentThread().getId();
        long allocBefore = THREAD_MX.getThreadAllocatedBytes(threadId);
        long start = System.nanoTime();
        for (long i = 0; i < iterations; i++) {
            blackhole += generate(generator, payload);
        }
        long elapsed = System.nanoTime() - start;
        long allocAfter = THREAD_MX.getThreadAllocatedBytes(threadId);

        System.err.printf("V%d: %d iterations%n", versionValue, iterations);

        double nsPerOp = elapsed / (double) iterations;
        double kbPerOp = (allocAfter - allocBefore) / 1024.0 / iterations;
        int modules = 21 + (versionValue - 1) * 4;
        return new Result(versionValue, modules, nsPerOp, kbPerOp, payloadBytes);
    }

    private static int generate(QRCodeGenerator generator, String payload) {
        return generator.generate(payload).isDark(0, 0) ? 1 : 0;
    }

    private static String payload(int bytes) {
        StringBuilder sb = new StringBuilder(bytes + SEED.length());
        while (sb.length() < bytes) {
            sb.append(SEED);
        }
        return sb.substring(0, bytes);
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    // ======================== OUTPUT ========================

    private static void printCsv(List<Result> results) {
        System.out.println(CSV_HEADER);
        for (Result r : results) {
            System.out.printf(Locale.US, "%d,%d,%.0f,%.1f,%.1f,%d%n",
                    r.version, r.modules, r.nsPerOp, 1e9 / r.nsPerOp, r.kbPerOp, r.payloadBytes);
        }
    }

    private static void printTable(List<Result> results) {
        List<String[]> rows = new ArrayList<>();
        for (Result r : results) {
            rows.add(new String[] {
                    "V" + r.version,
                    r.modules + "x" + r.modules,
                    formatTime(r.nsPerOp),
                    String.format(Locale.US, "%,.0f ops/s", 1e9 / r.nsPerOp),
                    formatMemory(r.kbPerOp),
                    r.payloadBytes + " B",
            });
        }

        int[] widths = new int[HEADERS.length];
        for (int i = 0; i < HEADERS.length; i++) {
            widths[i] = HEADERS[i].length();
            for (String[] row : rows) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        System.out.println();
        System.out.println(formatRow(HEADERS, widths));
        String[] rule = new String[HEADERS.length];
        for (int i = 0; i < rule.length; i++) {
            rule[i] = repeat('-', widths[i]);
        }
        System.out.println(formatRow(rule, widths));
        for (String[] row : rows) {
            System.out.println(formatRow(row, widths));
        }
        System.out.println();
    }

    private static String formatRow(String[] cells, int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append("  ");
            }
            sb.append(cells[i]);
            for (int pad = cells[i].length(); pad < widths[i]; pad++) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private static String formatTime(double nsPerOp) {
        if (nsPerOp >= 1e6) {
            return String.format(Locale.US, "%.3f ms", nsPerOp / 1e6);
        }
        return String.format(Locale.US, "%,.0f ns", nsPerOp);
    }

    private static String formatMemory(double kbPerOp) {
        if (kbPerOp >= 1024) {
            return String.format(Locale.US, "%.2f MB", kbPerOp / 1024.0);
        }
        return String.format(Locale.US, "%,.1f KB", kbPerOp);
    }

    private static String repeat(char c, int count) {
        char[] buffer = new char[count];
        java.util.Arrays.fill(buffer, c);
        return new String(buffer);
    }

    private static final class Result {
        final int version;
        final int modules;
        final double nsPerOp;
        final double kbPerOp;
        final int payloadBytes;

        Result(int version, int modules, double nsPerOp, double kbPerOp, int payloadBytes) {
            this.version = version;
            this.modules = modules;
            this.nsPerOp = nsPerOp;
            this.kbPerOp = kbPerOp;
            this.payloadBytes = payloadBytes;
        }
    }
}
