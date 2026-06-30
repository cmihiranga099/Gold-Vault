package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CsvImportRowResult {
    private int     rowNumber;
    private boolean success;
    private String  ticketNumber;   // populated if success
    private String  customerNic;
    private String  errorMessage;   // populated if failure
}