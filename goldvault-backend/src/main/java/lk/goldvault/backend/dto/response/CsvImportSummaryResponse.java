package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CsvImportSummaryResponse {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<CsvImportRowResult> results;
}