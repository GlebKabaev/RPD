package com.team.rpd_project.service;

import com.team.rpd_project.dto.RtdTableDto;
import org.springframework.stereotype.Service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class WordTableService {
    /**
     * Заполняет шаблон Word значениями из DTO и сохраняет результат в указанный файл.
     *
     * @param dto         DTO с данными для заполнения.
     * @param templatePath Путь к шаблону (например, "template/2.1.docx").
     * @param outputPath  Путь для сохранения заполненного файла (например, "output/filled_2.1.docx").
     * @throws IOException Если возникла ошибка при работе с файлами.
     */
    public void fillAndSave(RtdTableDto dto, String templatePath, String outputPath) throws IOException {
        // Загружаем шаблон
        try (FileInputStream fis = new FileInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            // Подготавливаем карту замен: плейсхолдер -> значение
            Map<String, String> replacements = prepareReplacements(dto);

            // Заменяем плейсхолдеры в таблицах
            replaceInTables(document, replacements);

            // Заменяем плейсхолдеры в основном теле документа (если они есть вне таблиц)
            replaceInBody(document, replacements);

            // Сохраняем заполненный документ
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }
        }
    }

    /**
     * Подготавливает карту замен на основе DTO.
     */
    private Map<String, String> prepareReplacements(RtdTableDto dto) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("${semester}", format(dto.getSemester()));
        replacements.put("${totalClassroomHours}", format(dto.getTotalClassroomHours()));
        replacements.put("${lectureHours}", format(dto.getLectureHours()));
        replacements.put("${laboratoryHours}", format(dto.getLaboratoryHours()));
        replacements.put("${seminarAndPracticalHours}", format(dto.getSeminarAndPracticalHours()));
        replacements.put("${supervisedIndependentWorkHours}", format(dto.getSupervisedIndependentWorkHours()));
        replacements.put("${intermediateAssessmentHours}", format(dto.getIntermediateAssessmentHours()));
        replacements.put("${independentWork}", format(dto.getIndependentWork()));
        replacements.put("${courseWorkHours}", format(dto.getCourseWorkHours()));
        replacements.put("${theoreticalMaterialStudyHours}", format(dto.getTheoreticalMaterialStudyHours()));
        replacements.put("${individualAssignmentsHours}", format(dto.getIndividualAssignmentsHours()));
        replacements.put("${abstractHours}", format(dto.getAbstractHours()));
        replacements.put("${currentAssessmentPreparationHours}", format(dto.getCurrentAssessmentPreparationHours()));
        replacements.put("${examPreparationHours}", format(dto.getExamPreparationHours()));
        replacements.put("${totalWorkloadHours}", format(dto.getTotalWorkloadHours()));
        replacements.put("${totalContactWorkHours}", format(dto.getTotalContactWorkHours()));
        replacements.put("${totalWorkloadCredits}", format(dto.getTotalWorkloadCredits()));
        replacements.put("${control}", dto.getControl() != null ? dto.getControl() : "-");
        return replacements;
    }

    /**
     * Заменяет плейсхолдеры в таблицах документа.
     */
    private void replaceInTables(XWPFDocument document, Map<String, String> replacements) {
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        replaceInParagraph(paragraph, replacements);
                    }
                }
            }
        }
    }

    /**
     * Заменяет плейсхолдеры в основном теле документа (параграфы вне таблиц).
     */
    private void replaceInBody(XWPFDocument document, Map<String, String> replacements) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            replaceInParagraph(paragraph, replacements);
        }
    }

    /**
     * Заменяет плейсхолдеры в одном параграфе.
     */
    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> replacements) {
        // Собираем полный текст параграфа
        StringBuilder textBuilder = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            String runText = run.getText(0);
            if (runText != null) {
                textBuilder.append(runText);
            }
        }

        String fullText = textBuilder.toString();
        String originalText = fullText; // Для проверки изменений

        // Проходим по всем плейсхолдерам и заменяем
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            fullText = fullText.replace(entry.getKey(), entry.getValue());
        }

        // Если текст изменился, обновляем параграф
        if (!fullText.equals(originalText)) {
            // Очищаем существующие runs
            while (!paragraph.getRuns().isEmpty()) {
                paragraph.removeRun(0);
            }
            // Создаём новый run с обновлённым текстом
            XWPFRun newRun = paragraph.createRun();
            newRun.setText(fullText);
        }
    }

    /**
     * Форматирует BigDecimal: убирает trailing zeros, если null — возвращает "-".
     */
    private String format(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        return value.stripTrailingZeros().toPlainString();
    }


}
