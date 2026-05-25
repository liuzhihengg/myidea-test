import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class Test {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String Object2Json(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T Json2Object(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        LocalDate localDate = LocalDate.now();
        LocalDate localDate1 = localDate.plusDays(-1);
        int beforeDepDate = (int) ChronoUnit.DAYS.between(LocalDate.now(), localDate1);
        System.out.println(LocalDate.now().getDayOfWeek().getValue());
        System.out.println(beforeDepDate);
    }

    private void test(Data data, long globalVersion) {
        if (data.version != globalVersion || (!data.valid && System.currentTimeMillis() > data.allowTime)) {
            System.out.println("fail");
        } else {
            System.out.println("success");
        }
    }

    private static class Data {
        private long version;

        private Map<String, String> map;

        private boolean valid;

        private long allowTime;

        public Data(long version, Map<String, String> map, boolean valid, long allowTime) {
            this.version = version;
            this.map = map;
            this.valid = valid;
            this.allowTime = allowTime;
        }

        public long getVersion() {
            return version;
        }

        public void setVersion(long version) {
            this.version = version;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public long getAllowTime() {
            return allowTime;
        }

        public void setAllowTime(long allowTime) {
            this.allowTime = allowTime;
        }
    }
}
