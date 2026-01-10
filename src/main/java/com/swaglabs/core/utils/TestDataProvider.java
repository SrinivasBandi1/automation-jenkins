package com.swaglabs.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Enterprise-level test data provider utility
 * Supports multiple data sources: Excel, JSON, CSV, and Properties
 */
public class TestDataProvider {
    private static final Logger logger = LogManager.getLogger(TestDataProvider.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private TestDataProvider() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Generic data provider that reads from Excel files
     */
    @DataProvider(name = "excelDataProvider")
    public static Object[][] getExcelData(Method method) {
        String testName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        String excelFile = String.format("testdata/%s.xlsx", className);
        
        logger.info("Loading test data from Excel file: {} for test: {}", excelFile, testName);
        
        try (FileInputStream fis = new FileInputStream(getResourcePath(excelFile));
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet(testName);
            if (sheet == null) {
                logger.warn("Sheet '{}' not found in Excel file: {}", testName, excelFile);
                return new Object[0][0];
            }
            
            return readExcelSheet(sheet);
            
        } catch (IOException e) {
            logger.error("Failed to read Excel file: {}", excelFile, e);
            return new Object[0][0];
        }
    }
    
    /**
     * Read data from Excel sheet and convert to Object[][]
     */
    private static Object[][] readExcelSheet(Sheet sheet) {
        int rowCount = sheet.getPhysicalNumberOfRows();
        int colCount = sheet.getRow(0).getPhysicalNumberOfCells();
        
        Object[][] data = new Object[rowCount - 1][colCount];
        
        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < colCount; j++) {
                Cell cell = row.getCell(j);
                data[i - 1][j] = getCellValue(cell);
            }
        }
        
        return data;
    }
    
    /**
     * Get cell value based on cell type
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * JSON data provider for complex test data
     */
    @DataProvider(name = "jsonDataProvider")
    public static Object[][] getJsonData(Method method) {
        String testName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        String jsonFile = String.format("testdata/%s.json", className);
        
        logger.info("Loading test data from JSON file: {} for test: {}", jsonFile, testName);
        
        try (InputStream is = TestDataProvider.class.getClassLoader().getResourceAsStream(jsonFile)) {
            if (is == null) {
                logger.warn("JSON file not found: {}", jsonFile);
                return new Object[0][0];
            }
            
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Map.class);
            List<Map<String, Object>> dataList = objectMapper.readValue(is, listType);
            
            // Filter data by test name if needed
            List<Map<String, Object>> filteredData = filterDataByTestName(dataList, testName);
            
            return convertToObjectArray(filteredData);
            
        } catch (IOException e) {
            logger.error("Failed to read JSON file: {}", jsonFile, e);
            return new Object[0][0];
        }
    }
    
    /**
     * Filter data by test name
     */
    private static List<Map<String, Object>> filterDataByTestName(List<Map<String, Object>> dataList, String testName) {
        List<Map<String, Object>> filteredData = new ArrayList<>();
        
        for (Map<String, Object> data : dataList) {
            String dataTestName = (String) data.get("testName");
            if (dataTestName == null || dataTestName.equals(testName)) {
                filteredData.add(data);
            }
        }
        
        return filteredData;
    }
    
    /**
     * Convert List<Map> to Object[][]
     */
    private static Object[][] convertToObjectArray(List<Map<String, Object>> dataList) {
        if (dataList.isEmpty()) {
            return new Object[0][0];
        }
        
        Object[][] result = new Object[dataList.size()][1];
        for (int i = 0; i < dataList.size(); i++) {
            result[i][0] = dataList.get(i);
        }
        
        return result;
    }
    
    /**
     * CSV data provider
     */
    @DataProvider(name = "csvDataProvider")
    public static Object[][] getCsvData(Method method) {
        String testName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        String csvFile = String.format("testdata/%s.csv", className);
        
        logger.info("Loading test data from CSV file: {} for test: {}", csvFile, testName);
        
        try (InputStream is = TestDataProvider.class.getClassLoader().getResourceAsStream(csvFile)) {
            if (is == null) {
                logger.warn("CSV file not found: {}", csvFile);
                return new Object[0][0];
            }
            
            return readCsvFile(is);
            
        } catch (IOException e) {
            logger.error("Failed to read CSV file: {}", csvFile, e);
            return new Object[0][0];
        }
    }
    
    /**
     * Read CSV file and convert to Object[][]
     */
    private static Object[][] readCsvFile(InputStream is) throws IOException {
        Scanner scanner = new Scanner(is);
        List<String[]> rows = new ArrayList<>();
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");
            rows.add(values);
        }
        
        scanner.close();
        
        if (rows.isEmpty()) {
            return new Object[0][0];
        }
        
        Object[][] result = new Object[rows.size() - 1][rows.get(0).length];
        for (int i = 1; i < rows.size(); i++) {
            result[i - 1] = rows.get(i);
        }
        
        return result;
    }
    
    /**
     * Dynamic data provider that automatically detects data source
     */
    @DataProvider(name = "dynamicDataProvider")
    public static Object[][] getDynamicData(Method method) {
        String testName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        
        // Try different data sources in order of preference
        String[] dataSources = {
            String.format("testdata/%s.xlsx", className),
            String.format("testdata/%s.json", className),
            String.format("testdata/%s.csv", className)
        };
        
        for (String dataSource : dataSources) {
            if (resourceExists(dataSource)) {
                logger.info("Found data source: {}", dataSource);
                return loadDataFromSource(dataSource, testName);
            }
        }
        
        logger.warn("No data source found for test: {}. Using empty data provider.", testName);
        return new Object[0][0];
    }
    
    /**
     * Check if resource exists
     */
    private static boolean resourceExists(String resourcePath) {
        return TestDataProvider.class.getClassLoader().getResource(resourcePath) != null;
    }
    
    /**
     * Load data from the specified source
     */
    private static Object[][] loadDataFromSource(String dataSource, String testName) {
        if (dataSource.endsWith(".xlsx")) {
            return getExcelDataForTest(dataSource, testName);
        } else if (dataSource.endsWith(".json")) {
            return getJsonDataForTest(dataSource, testName);
        } else if (dataSource.endsWith(".csv")) {
            return getCsvDataForTest(dataSource);
        }
        
        return new Object[0][0];
    }
    
    /**
     * Helper methods for loading specific data types
     */
    private static Object[][] getExcelDataForTest(String excelFile, String testName) {
        try (FileInputStream fis = new FileInputStream(getResourcePath(excelFile));
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet(testName);
            if (sheet == null) {
                return new Object[0][0];
            }
            
            return readExcelSheet(sheet);
            
        } catch (IOException e) {
            logger.error("Failed to read Excel file: {}", excelFile, e);
            return new Object[0][0];
        }
    }
    
    private static Object[][] getJsonDataForTest(String jsonFile, String testName) {
        try (InputStream is = TestDataProvider.class.getClassLoader().getResourceAsStream(jsonFile)) {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Map.class);
            List<Map<String, Object>> dataList = objectMapper.readValue(is, listType);
            List<Map<String, Object>> filteredData = filterDataByTestName(dataList, testName);
            return convertToObjectArray(filteredData);
            
        } catch (IOException e) {
            logger.error("Failed to read JSON file: {}", jsonFile, e);
            return new Object[0][0];
        }
    }
    
    private static Object[][] getCsvDataForTest(String csvFile) {
        try (InputStream is = TestDataProvider.class.getClassLoader().getResourceAsStream(csvFile)) {
            return readCsvFile(is);
        } catch (IOException e) {
            logger.error("Failed to read CSV file: {}", csvFile, e);
            return new Object[0][0];
        }
    }
    
    /**
     * Get resource path
     */
    private static String getResourcePath(String resourceName) {
        return TestDataProvider.class.getClassLoader().getResource(resourceName).getPath();
    }
    
    /**
     * Generate random test data
     */
    public static Map<String, Object> generateRandomTestData() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("firstName", "Test" + System.currentTimeMillis());
        testData.put("lastName", "User" + System.currentTimeMillis());
        testData.put("email", "test" + System.currentTimeMillis() + "@example.com");
        testData.put("phone", "+1" + (1000000000 + new Random().nextInt(900000000)));
        return testData;
    }
} 