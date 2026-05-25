package javabase;

public class MonitorNameGenerate {
    public static void main(String[] args) {
//        String str = "canal_entry_insert_delay";
//        String basePre = "s.flight.f_tts_agentdog.f_tts_agentdog_";
//        String dbsyncBasePre = "s.flight.f_tts_dbsync.nq.";
//
//        String normalBase = "aliasSub(" + basePre +"%s_monitor_long_range*_[[des]]_Count,'s.flight.f_tts_dbsync.nq.%s_monitor_long_range(.*)_[[des]]_Count','\\1')";
//        System.out.println(String.format(normalBase, str, str));
//        String timeBase = "aliasSub(" + basePre + "%s_[[des]]_*,'s.flight.f_tts_dbsync.nq.%s_[[des]]_(.*)','\\1')";
//        System.out.println(String.format(timeBase, str, str));
//        String P100TimeBase = "aliasSub(" + basePre +"%s_*_1Min_P100,'s.flight.f_tts_dbsync.nq.%s_([a-zA-Z0-9]+)_1Min_P100','\\1')";
//        System.out.println(String.format(P100TimeBase, str, str));

        testCanal1to1to4();
//        testCanal1to0to19();
    }

    public static void testCanal1to1to4() {
        String str = "test_canal_byteDeserializer";
        String basePre = "s.flight.f_tts_agentdog.f_tts_agentdog_";
        String dbsyncBasePre = "s.flight.f_tts_dbsync.nq.";

        String normalBase = "aliasSub(s.flight.f_tts_agentdog.nq.%s_monitor_long_range*_policydb9_Count,'s.flight.f_tts_agentdog.nq.%s_monitor_long_range(.*)_policydb9_Count','\\1')";
        System.out.println(String.format(normalBase, str, str));
        String timeBase = "aliasSub(s.flight.f_tts_agentdog.nq.%s_policydb9_*,'s.flight.f_tts_agentdog.nq.%s_policydb9_(.*)','\\1')";
//        System.out.println(String.format(timeBase, str, str));
        String temp = "aliasSub(   exclude(s.flight.f_tts_agentdog.nq.%s_policydb9_*, \"second_\"),   \"^.*policydb9_(.*)\",   \"\\1\" )";
        System.out.println(String.format(temp, str));
        String P100TimeBase = "aliasSub(" + basePre +"%s_*_1Min_P100,'s.flight.f_tts_dbsync.nq.%s_policydb9_1Min_P100','\\1')";
        System.out.println(String.format(P100TimeBase, str, str));
    }

    public static void testCanal1to0to19() {
        String str = "test_canal_byteDuration";
        String basePre = "s.flight.f_tts_agentdog.f_tts_agentdog_";
        String dbsyncBasePre = "s.flight.f_tts_dbsync.nq.";

        String normalBase = "aliasSub(s.flight.f_tts_agentdog.nq.%s_monitor_long_range*_policydb9_second_Count,'s.flight.f_tts_agentdog.nq.%s_monitor_long_range(.*)_policydb9_second_Count','\\1')";
        System.out.println(String.format(normalBase, str, str));
        String timeBase = "aliasSub(s.flight.f_tts_agentdog.nq.%s_policydb9_second_*,'s.flight.f_tts_agentdog.nq.%s_policydb9_second_(.*)','\\1')";
        System.out.println(String.format(timeBase, str, str));
        String temp = "aliasSub(   exclude(s.flight.f_tts_agentdog.f_tts_agentdog_%s_policydb9_second_*, \"second_\"),   \"^.*policydb9_(.*)\",   \"\\1\" )";
//        System.out.println(String.format(temp, str));
        String P100TimeBase = "aliasSub(" + basePre +"%s_*_1Min_P100,'s.flight.f_tts_dbsync.nq.%s_policydb9_1Min_P100','\\1')";
        System.out.println(String.format(P100TimeBase, str, str));
    }
}
