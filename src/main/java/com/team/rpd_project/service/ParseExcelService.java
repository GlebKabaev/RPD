package com.team.rpd_project.service;

import com.team.rpd_project.dto.RtdTableDto;
import lombok.Getter;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class ParseExcelService {
    private static final String[] SEMESTER_COLUMNS = {"Экза мен", "Зачет", "Зачет с оц.", "КП", "КР"};
    private static final String[] EXPECTED_SEMESTER_COLUMNS = {"з.е.", "Лек", "Лаб", "Пр", "КСР", "КРП", "ИКР", "СР", "Конт роль"};
    private static final int HEADER_ROW_INDEX = 2;
    private static final int SEMESTER_ROW_INDEX = 1;
    @Value("${app.upload-service.file-path}")
    private String filePath;
    public RtdTableDto parseExcelSheetPlane(String indexName) throws IOException {
        Path filePath=getFile();
        try(InputStream inputStream=Files.newInputStream(filePath)){
            Workbook workbook=WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheet("План");

            return parseDiscipline(sheet,indexName);
        }
    }
    private RtdTableDto parseDiscipline(Sheet sheet,String indexName) throws IOException {
        Row headerRow=sheet.getRow(HEADER_ROW_INDEX);
        Row semesterRow=sheet.getRow(SEMESTER_ROW_INDEX);

        Row rowDiscipline=findDisciplineRow(sheet,indexName);

        Integer semester=findSemester(sheet,rowDiscipline);

        int semesterStartColumn = findSemesterStartColumn(semesterRow, semester);

        SemesterColumnRange semesterRange=findSemesterColumnsRange(headerRow,semesterStartColumn);

        Map<String,String> semesterData=getSemesterData(rowDiscipline,semesterRange);

        return buildRtdTableDto(semesterData,semester,sheet,rowDiscipline,headerRow);
    }

    private RtdTableDto buildRtdTableDto(Map<String,String> semesterData,Integer semester,Sheet sheet, Row rowDiscipline,Row headerRow) throws IOException {
        RtdTableDto dto =new RtdTableDto();
        setBasicData(dto,semester,semesterData);
        setClassroomHours(dto);
        setStaticFields(dto);
        setAdditionalData(dto,sheet,rowDiscipline);
        setAttestationForm(dto,sheet,rowDiscipline,headerRow);
        return dto;
    }


    private void setAttestationForm(RtdTableDto dto, Sheet sheet, Row rowDiscipline,Row headerRow) throws IOException {
        dto.setFormIntermediateCertificationFirst(findAttestation(sheet,rowDiscipline).getFirst());
        if(findAttestation(sheet,rowDiscipline).size()==1){
            dto.setFormIntermediateCertificationSecond(findAttestation(sheet,rowDiscipline).getFirst());
        }
        else{
            dto.setFormIntermediateCertificationSecond(findAttestation(sheet,rowDiscipline).get(1));
        }
    }

    private void setStaticFields(RtdTableDto dto){
        dto.setTheoreticalMaterialStudyHours(BigDecimal.valueOf(2));
        dto.setIndividualAssignmentsHours(BigDecimal.valueOf(5));
        dto.setAbstractHours(null);
        dto.setCurrentAssessmentPreparationHours(BigDecimal.valueOf(3));
    }

    private void setAdditionalData(RtdTableDto dto,Sheet sheet,Row disciplineRow) throws IOException {
        int planColumn = findColumnIndex(sheet, "По плану");
        String planValue = getCellValueAsString(disciplineRow, planColumn);
        dto.setTotalWorkloadHours(parseBigDecimal(planValue));

        int kontRabColumn = findColumnIndex(sheet, "Конт. раб.");
        String kontRabValue = getCellValueAsString(disciplineRow, kontRabColumn);
        dto.setTotalContactWorkHours(parseBigDecimal(kontRabValue));
    }

    private void setClassroomHours(RtdTableDto dto){
        BigDecimal totalClassroom = BigDecimal.ZERO;
        if (dto.getLectureHours() != null) totalClassroom = totalClassroom.add(dto.getLectureHours());
        if (dto.getLaboratoryHours() != null) totalClassroom = totalClassroom.add(dto.getLaboratoryHours());
        if (dto.getSeminarAndPracticalHours() != null) totalClassroom = totalClassroom.add(dto.getSeminarAndPracticalHours());
        dto.setTotalClassroomHours(totalClassroom);
    }

    private void setBasicData(RtdTableDto dto,Integer semester,Map<String,String> semesterData){
        dto.setSemester(BigDecimal.valueOf(semester));
        dto.setLectureHours(parseBigDecimal(semesterData.get("Лек")));
        dto.setLaboratoryHours(parseBigDecimal(semesterData.get("Лаб")));
        dto.setSeminarAndPracticalHours(parseBigDecimal(semesterData.get("Пр")));
        dto.setSupervisedIndependentWorkHours(parseBigDecimal(semesterData.get("КСР")));
        dto.setIntermediateAssessmentHours(parseBigDecimal(semesterData.get("ИКР")));
        dto.setIndependentWork(parseBigDecimal(semesterData.get("СР")));
        dto.setCourseWorkHours(parseBigDecimal(semesterData.get("КРП")));
        dto.setExamPreparationHours(parseBigDecimal(semesterData.get("Конт роль")));
        dto.setTotalWorkloadCredits(parseBigDecimal(semesterData.get("з.е.")));
    }


    private  Row findDisciplineRow(Sheet sheet,String indexName) throws IOException {
        int indexColumn=findColumnIndex(sheet,"Индекс");
        return sheet.getRow(findRowIndexByColumnValue(sheet,indexColumn,indexName));
    }

    private Map<String, String> getSemesterData(Row dataRow, SemesterColumnRange semesterRange) {
        Map<String, String> semesterData = new HashMap<>();
        for (Map.Entry<String, Integer> entry : semesterRange.getColumnMapping().entrySet()) {
            String columnName = entry.getKey();
            int columnIndex = entry.getValue();
            String value = getCellValueAsString(dataRow, columnIndex);
            semesterData.put(columnName, value);
        }
        return semesterData;
    }

    private List<String> findAttestation(Sheet sheet,Row dataRow) throws IOException {
        List<String> attestation=new ArrayList<>();
        for(String columnName:SEMESTER_COLUMNS){
            int columnIndex=findColumnIndex(sheet,columnName);
            String cellValue=getCellValueAsString(dataRow, columnIndex);
            if (cellValue != null && !cellValue.trim().isEmpty()) {
                attestation.add(columnName);
            }
        }
        return attestation;
    }
    private Integer findSemester(Sheet sheet,Row dataRow) throws IOException {
        //TODO:добавить обработку нескольких семестров
        for(String columnName:SEMESTER_COLUMNS){
            int columnIndex=findColumnIndex(sheet,columnName);
            String cellValue=getCellValueAsString(dataRow, columnIndex);
            if (cellValue != null && !cellValue.trim().isEmpty()) {
                try {
                    return Integer.parseInt(cellValue.trim());
                } catch (NumberFormatException e) {

                }
            }
        }
        return null;
    }
    private int findColumnIndex(Sheet sheet, String columnName) throws IOException {
        Row headerRow= sheet.getRow(HEADER_ROW_INDEX);
        for(Cell cell:headerRow){
            String currentHeader=cell.getStringCellValue();
            if(columnName.equalsIgnoreCase(currentHeader.trim())){
                return cell.getColumnIndex();
            }
        }
        throw new IOException("Столбец " + columnName + " не найден");
    }

    private SemesterColumnRange findSemesterColumnsRange(Row headerRow,int startColumn){
        Map<String,Integer> columnMapping = new HashMap<>();
        int currentCol=startColumn;
        for (String expectedColumn : EXPECTED_SEMESTER_COLUMNS) {
            System.out.println("headerRow = " + currentCol + ", startColumn = " + startColumn);
            if (currentCol > headerRow.getLastCellNum()) {
                break;
            }
            String currentHeader=getCellValueAsString(headerRow,currentCol);
            if (currentHeader != null && currentHeader.contains(expectedColumn)) {
                columnMapping.put(expectedColumn, currentCol);
                currentCol++;
            }
            else{
                currentCol++;
            }
        }

        return new SemesterColumnRange(startColumn, currentCol - 1, columnMapping);
    }

    private int findRowIndexByColumnValue(Sheet sheet,int columnIndex,String searchValue) throws IOException {
        System.out.println("sheet = " + sheet + ", columnIndex = " + columnIndex + ", searchValue = " + searchValue);
        for(int i=0;i<=sheet.getLastRowNum();i++){
            Row row =sheet.getRow(i);
            Cell cell=row.getCell(columnIndex);
            String cellValue = cell.getStringCellValue();
            if(searchValue.equals(cellValue)){
                return i;
            }
        }
        throw new IOException("Строка со значением '" + searchValue + "' не найдена в столбце " + columnIndex);
    }
    private int findSemesterStartColumn(Row headerRow, Integer semester) throws IOException {
        for (Cell cell : headerRow) {
            String headerValue = getCellValueAsString(headerRow, cell.getColumnIndex());
            if (headerValue != null && headerValue.contains("Семестр " + semester)) {
                return cell.getColumnIndex();
            }
        }
        throw new IOException("Не найден стартовый столбец для семестра " + semester);
    }
    private Path getFile() throws IOException {
        Path directoryPath=Paths.get(filePath);
        try(Stream<Path> files=Files.list(directoryPath)){
            return files.filter(Files::isRegularFile)
                    .findFirst()
                    .orElseThrow(()->new FileNotFoundException("В директории " +directoryPath + " нет файлов"));
        }
    }
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    @Getter
    private static class SemesterColumnRange {
        private final int startColumn;
        private final int endColumn;
        private final Map<String, Integer> columnMapping;

        public SemesterColumnRange(int startColumn, int endColumn, Map<String, Integer> columnMapping) {
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.columnMapping = columnMapping;
        }
    }
    private String getCellValueAsString(Row row, int columnIndex) {
        if (row == null || columnIndex < 0) return null;

        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == (int) value) {
                        return String.valueOf((int) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return "";
        }
    }
}
