package lk.goldvault.backend.util;

import lk.goldvault.backend.repository.PawnTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
@RequiredArgsConstructor
public class TicketNumberGenerator {

    private final PawnTicketRepository pawnTicketRepository;

    /** Generates a sequential ticket number like GV-2026-000001 */
    public String generate() {
        int year = Year.now().getValue();
        long count = pawnTicketRepository.count() + 1;
        return String.format("GV-%d-%06d", year, count);
    }
}