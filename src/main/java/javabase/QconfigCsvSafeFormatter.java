package javabase;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class QconfigCsvSafeFormatter {

    public static final String BASH_PATH = "/Users/tiaojiheng/Downloads/";

    public static void main(String[] args) {
//        processChannel();
//        processTag();
//        processRoundTag();
        processOverrideT();
    }

    private static void processOverrideT() {
        String inputFile = BASH_PATH + "agent_carrier_auth_auto.t (1).csv";
        String outputFile = BASH_PATH + "agent_carrier_auth_auto_new.t (1).csv";

        String baseFormat = "{\"files\":[\"agent_carrier_auth_auto.t\"],\"param\":\"%s/%s\",\"value\":\"%s\",\"paramType\":\"PATH\"}";

        try (CSVReader reader = new CSVReader(new FileReader(inputFile));
             CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {

            String[] row;
            boolean isFirstLine = true;

            String[] rowName = null;
            StringBuilder builder = new StringBuilder();
            while ((row = reader.readNext()) != null) {
                if (isFirstLine) {
                    writer.writeNext(row);
                    isFirstLine = false;
                    rowName = row;
                    continue;
                }


                for (int i = 1; i < rowName.length; i++) {
                    String name = rowName[i];
                    builder.append(String.format( baseFormat, row[0],name, row[i])).append("\n");
                }

                writer.writeNext(row);
            }

            System.out.println("override结果");
            System.out.println(builder);

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    private static void processChannel() {
        String inputFile = BASH_PATH + "channel_alias.t.csv";
        String outputFile = BASH_PATH + "channel_alias_new.t.csv";

        try (CSVReader reader = new CSVReader(new FileReader(inputFile));
             CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {

            String[] row;
            boolean isFirstLine = true;

            while ((row = reader.readNext()) != null) {
                if (isFirstLine) {
                    writer.writeNext(row);
                    isFirstLine = false;
                    continue;
                }

                if (row.length >= 2) {
                    row[1] = "paoding_quanr_analyse_" + row[0].toLowerCase() + ","+ row[1];
                }

                writer.writeNext(row);
            }

            System.out.println("处理完成，输出到: " + outputFile);

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    private static void processTag() {
        String inputFile = BASH_PATH + "product_package.t.csv";
        String outputFile = BASH_PATH + "channel_alias_new.t.csv";

        StringBuilder builder = new StringBuilder();
        try (CSVReader reader = new CSVReader(new FileReader(inputFile));) {

            String[] row;
            boolean isFirstLine = true;

            while ((row = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                builder.append(row[2] + ",");
            }
            builder.deleteCharAt(builder.length() - 1);

            System.out.println("处理完成，输出到: " + builder.toString());

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    private static void processRoundTag() {
        String inputFile = BASH_PATH + "twell_roundtrip_tags.csv";
        String outputFile = BASH_PATH + "channel_alias_new.t.csv";

        StringBuilder builder = new StringBuilder();
        try (CSVReader reader = new CSVReader(new FileReader(inputFile));) {

            String[] row;
            boolean isFirstLine = true;

            while ((row = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                builder.append(row[0] + "/");
            }
            builder.deleteCharAt(builder.length() - 1);

            System.out.println("处理完成，输出到: " + builder.toString());

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }
}
