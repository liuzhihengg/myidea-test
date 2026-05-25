import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelMerger {
    public static final String BASE = "/Users/tiaojiheng/去哪儿/需求/政策也能叠加代理费FD-248676/";

    public static void main(String[] args) {
        String fileAPath = BASE + "单程库存政策导入格式样例无代理费版本.xlsx";
        String fileBPath = BASE + "singleStockPolicy.xlsx";
        String outputFilePath = BASE + "path_to_output_file.xlsx";

        try (FileInputStream fisA = new FileInputStream(fileAPath);
             FileInputStream fisB = new FileInputStream(fileBPath);
             Workbook workbookA = new XSSFWorkbook(fisA);
             Workbook workbookB = new XSSFWorkbook(fisB)) {

            Sheet sheetA = workbookA.getSheetAt(0);
            Sheet sheetB = workbookB.getSheetAt(0);

            Map<String, Integer> columnMapping = new HashMap<>();

            // Create column mapping based on header row in sheetA
            Row headerA = sheetA.getRow(0);
            for (Cell cell : headerA) {
                columnMapping.put(cell.getStringCellValue(), cell.getColumnIndex());
            }

            // Iterate over sheetB rows and copy data to sheetA
            for (int i = 1; i <= sheetB.getLastRowNum(); i++) {
                Row rowB = sheetB.getRow(i);
                Row rowA = sheetA.createRow(sheetA.getLastRowNum() + 1);

                for (Cell cellB : rowB) {
                    String columnName = sheetB.getRow(0).getCell(cellB.getColumnIndex()).getStringCellValue();
                    if (columnMapping.containsKey(columnName)) {
                        int columnIndexA = columnMapping.get(columnName);
                        Cell cellA = rowA.createCell(columnIndexA);
                        copyCellValue(cellB, cellA);
                    }
                }
            }

            // Save the merged data into a new file
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                workbookA.write(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyCellValue(Cell sourceCell, Cell targetCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                targetCell.setCellValue(sourceCell.getNumericCellValue());
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            default:
                break;
        }
    }
}

