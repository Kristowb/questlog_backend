package com.questlog.backend.service;

import com.questlog.backend.dto.DietLogRequest;
import com.questlog.backend.dto.DietLogResponse;
import java.time.LocalDate;
import java.util.List;

public interface DietService {
    DietLogResponse addDietLog(DietLogRequest request);
    List<DietLogResponse> getDailyDiet(Long userId, LocalDate date);
}
