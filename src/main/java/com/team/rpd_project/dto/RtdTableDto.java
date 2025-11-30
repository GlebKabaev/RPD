package com.team.rpd_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class RtdTableDto {
    //Под "Семестры (часы)
    private BigDecimal semester;
    //Справа от "Контактная работа, в том числе:"
    // Аудиторные занятия (всего)
    private BigDecimal totalClassroomHours;

    // Занятия лекционного типа
    private BigDecimal lectureHours;

    // Лабораторные занятия
    private BigDecimal laboratoryHours;

    // Занятия семинарского типа (семинары, практические занятия)
    private BigDecimal seminarAndPracticalHours;

    // Иная контактная работа:

    // Контроль самостоятельной работы (КСР)
    private BigDecimal supervisedIndependentWorkHours;

    // Промежуточная аттестация (ИКР)
    private BigDecimal intermediateAssessmentHours;

    // Самостоятельная работа, в том числе:
    private BigDecimal IndependentWork;

    // Курсовая работа
    private BigDecimal courseWorkHours;

    // Проработка учебного (теоретического) материала
    private BigDecimal theoreticalMaterialStudyHours;

    // Выполнение индивидуальных заданий (подготовка сообщений, презентаций)
    private BigDecimal individualAssignmentsHours;

    // Реферат
    private BigDecimal abstractHours;

    // Подготовка к текущему контролю
    private BigDecimal currentAssessmentPreparationHours;

    //Контроль
    private String control;

    //Подготовка к экзамену
    private BigDecimal examPreparationHours;

    // Общая трудоёмкость (в часах)
    private BigDecimal totalWorkloadHours;

    // Общая трудоёмкость, в том числе контактная работа (в часах)
    private BigDecimal totalContactWorkHours;

    // Общая трудоёмкость в зачётных единицах
    private BigDecimal totalWorkloadCredits;
}
