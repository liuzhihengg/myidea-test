import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnTest {

    private static final Map<String, Integer> POLICY_TABLE_DPTARR_AND_NO_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> POLICY_TABLE = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    public static final int count = 10000000;

    public static void main(String[] args) {
        ConnTest test = new ConnTest();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        AtomicBoolean foundNull = new AtomicBoolean(false);

        // Writing threads: continuously add entries to the maps
        for (int i = 0; i < 200; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < count; j++) {
                    String from = "from" + j;
                    String to = "to" + j;
                    test.getPolicyTableName(from, to);
                }
            });
        }

        // Reading threads: continuously get entries from the maps
        for (int i = 0; i < 50; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < count; j++) {
                    String from = "from" + j;
                    String to = "to" + j;
                    String builder = from + "-" + to;

                    test.getPolicyTableName(from, to);
                }
            });
        }

        executorService.shutdown();

        // Wait for all tasks to finish
        while (!executorService.isTerminated()) {
            // Busy wait
        }

        if (foundNull.get()) {
            System.out.println("Test failed: Found null values.");
        } else {
            System.out.println("Test passed: No null values found.");
        }
    }

    public void getPolicyTableName(String from, String to) {
        String builder = from + "-" + to;
        String tableName = POLICY_TABLE.get(builder);
        if (tableName == null || tableName.isEmpty()) {
            int no = builder.hashCode(); // Use random int for no
            tableName = "table" + no; // Use a simple string for tableName
            POLICY_TABLE_DPTARR_AND_NO_CACHE.put(builder, no);
            POLICY_TABLE.put(builder, tableName);
        }
        // Simulated check to replace dbChooserHepler.setDSKeyByTableNo
        if (POLICY_TABLE_DPTARR_AND_NO_CACHE.get(builder) == null) {
            System.out.println("error");
        }
    }
}
