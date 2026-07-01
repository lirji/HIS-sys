package com.lrj.his.billing.service;

import com.lrj.his.api.event.RefundRequestedEvent;
import com.lrj.his.api.event.RefundReviewedEvent;
import com.lrj.his.billing.domain.Invoice;
import com.lrj.his.billing.domain.InvoiceRepository;
import com.lrj.his.billing.domain.InvoiceStatus;
import com.lrj.his.billing.domain.RefundRequest;
import com.lrj.his.billing.domain.RefundRequestRepository;
import com.lrj.his.billing.domain.RefundStatus;
import com.lrj.his.billing.event.RefundEventPublisher;
import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 退费审批流。收费员对已缴费账单发起申请 → 审批人(ADMIN)通过/驳回。
 * 通过时本地强一致地把账单置 REFUNDED,提交后发审批结果事件(通知申请人 + 推动 outpatient 撤销医嘱)。
 */
@Service
public class RefundService {

    private final InvoiceRepository invoiceRepository;
    private final RefundRequestRepository refundRepository;
    private final RefundEventPublisher publisher;

    public RefundService(InvoiceRepository invoiceRepository,
                         RefundRequestRepository refundRepository,
                         RefundEventPublisher publisher) {
        this.invoiceRepository = invoiceRepository;
        this.refundRepository = refundRepository;
        this.publisher = publisher;
    }

    @Transactional
    public RefundRequest request(Long invoiceId, String reason, CurrentUser applicant) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> BusinessException.of(ResultCode.INVOICE_NOT_FOUND));
        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw BusinessException.of(ResultCode.REFUND_NOT_ALLOWED, "仅已缴费账单可申请退费");
        }
        RefundRequest req = RefundRequest.open(invoiceId, invoice.getEncounterId(),
                invoice.getTotalAmount(), reason, applicant.userId(), applicant.username());
        req = refundRepository.save(req);
        publisher.publishRequested(new RefundRequestedEvent(req.getId(), invoiceId,
                invoice.getEncounterId(), invoice.getTotalAmount(), reason,
                applicant.userId(), applicant.username()));
        return req;
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> listPending() {
        return refundRepository.findByStatusOrderByCreatedAtAsc(RefundStatus.REQUESTED);
    }

    @Transactional
    public RefundRequest approve(Long refundRequestId, CurrentUser reviewer) {
        RefundRequest req = loadRequest(refundRequestId);
        Invoice invoice = invoiceRepository.findById(req.getInvoiceId())
                .orElseThrow(() -> BusinessException.of(ResultCode.INVOICE_NOT_FOUND));
        req.approve(reviewer.username());
        invoice.refund();
        refundRepository.save(req);
        invoiceRepository.save(invoice);
        publisher.publishReviewed(new RefundReviewedEvent(req.getId(), invoice.getId(),
                invoice.getEncounterId(), true, null, req.getApplicantUserId(), invoice.orderIds()));
        return req;
    }

    @Transactional
    public RefundRequest reject(Long refundRequestId, String opinion, CurrentUser reviewer) {
        RefundRequest req = loadRequest(refundRequestId);
        req.reject(reviewer.username(), opinion);
        refundRepository.save(req);
        publisher.publishReviewed(new RefundReviewedEvent(req.getId(), req.getInvoiceId(),
                req.getEncounterId(), false, opinion, req.getApplicantUserId(), List.of()));
        return req;
    }

    private RefundRequest loadRequest(Long id) {
        return refundRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ResultCode.REFUND_REQUEST_NOT_FOUND));
    }
}
