package com.handshape.justneuralnets.input;

import com.monitorjbl.xlsx.StreamingReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author joturner
 */
public class ExcelTabularInput extends FileTabularInput {

    private TreeMap<Integer, String> headers = new TreeMap<>();
    File file;

    public ExcelTabularInput(File file) throws IOException {
        super(file);
        this.file = file;
        Workbook wb = StreamingReader.builder()
                .rowCacheSize(100) // number of rows to keep in memory (defaults to 10)
                .bufferSize(4096) // buffer size to use when reading InputStream to file (defaults to 1024)
                .open(file);            // InputStream or File for XLSX file (required)
        Sheet sheet = wb.getSheetAt(0);
        for (Row header : sheet) {
            header.cellIterator().forEachRemaining((Cell cell) -> {
                int col = cell.getColumnIndex();
                if (cell.getCellType().equals(CellType.STRING)) {
                    String stringValue = cell.getStringCellValue();
                    headers.put(col, stringValue);
                }
            });
            return;
        }
        wb.close();
    }

    @Override
    public Iterator<Map<String, String>> iterator() {
        return new ExcelIterator(file);
    }

    @Override
    public Set<String> getKeys() {
        return new TreeSet<String>(headers.values());
    }

    class ExcelIterator implements Iterator<Map<String, String>> {

        private final Iterator<Row> sheeterator;
        Workbook wb;

        private ExcelIterator(File file) {
            wb = StreamingReader.builder()
                    .rowCacheSize(100) // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096) // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(file);            // InputStream or File for XLSX file (required)
            Sheet sheet = wb.getSheetAt(0);
            sheeterator = sheet.rowIterator();
            sheeterator.next(); // Skip the header row.
        }

        @Override
        public boolean hasNext() {
            if (sheeterator.hasNext()) {
                return true;
            } else {
                try {
                    wb.close();
                } catch (IOException ex) {
                    Logger.getLogger(ExcelTabularInput.class.getName()).log(Level.SEVERE, null, ex);
                }
                return false;
            }
        }

        @Override
        public Map<String, String> next() {
            Row row = sheeterator.next();
            TreeMap<String, String> map = new TreeMap<>();
            for (String key : headers.values()) {
                map.put(key, "");
            }
            for (Cell cell : row) {
                if (cell != null) {
                    if (headers.containsKey(cell.getColumnIndex())) {
                        String key = headers.get(cell.getColumnIndex());
                        switch (cell.getCellType()) {
                            case STRING:
                                map.put(key, cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                map.put(key, String.valueOf(cell.getNumericCellValue()));
                                break;
                            case BOOLEAN:
                                map.put(key, String.valueOf(cell.getBooleanCellValue()));
                                break;
                            case FORMULA:
                                if (null == cell.getCachedFormulaResultType()) {
                                    map.put(key, "");
                                } else {
                                    switch (cell.getCachedFormulaResultType()) {
                                        case STRING:
                                            map.put(key, cell.getStringCellValue());
                                            break;
                                        case NUMERIC:
                                            map.put(key, String.valueOf(cell.getNumericCellValue()));
                                            break;
                                        case BOOLEAN:
                                            map.put(key, String.valueOf(cell.getBooleanCellValue()));
                                            break;
                                        default:
                                            map.put(key, "");
                                            break;
                                    }
                                }
                                break;
                            default:
                                map.put(key, "");
                                break;
                        }
                    }
                }
            }
            return map;
        }

    }
}
