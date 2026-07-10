package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.PaymentSubmission;
import lk.goldvault.backend.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentSubmissionRepository extends JpaRepository<PaymentSubmission, Long> {

    List<PaymentSubmission> findByTicketIdOrderBySubmittedAtDesc(Long ticketId);

    List<PaymentSubmission> findByCustomerIdOrderBySubmittedAtDesc(Long customerId);

    List<PaymentSubmission> findByStatusAndTicket_Shop_IdOrderBySubmittedAtAsc(
            SubmissionStatus status, Long shopId);

    long countByStatusAndTicket_Shop_Id(SubmissionStatus status, Long shopId);
}