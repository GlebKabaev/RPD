package com.team.rpd_project.service;

import com.team.rpd_project.dto.RtdTableDto;
import com.team.rpd_project.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RpdService {
    private final WordTableService wordTableService;
    private final ParseExcelService parseExcelService;

    public void createRpd(String index) {

        try {
            RtdTableDto rtdTableDto = parseExcelService.parseExcelSheetPlane(index);
            wordTableService.fillAndSave(rtdTableDto, "src/main/resources/template/2.1.docx",
                    "output/filled_2.1_" + System.currentTimeMillis() + ".docx");
        } catch (IOException e) {
            throw new BusinessException("File", "NotFound");
        }
    }
}
