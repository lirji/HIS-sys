package com.lrj.his.billing.service;

import com.lrj.his.api.event.InvoicePaidEvent;
import com.lrj.his.api.event.OrderPlacedEvent;
import com.lrj.his.billing.domain.Invoice;
import com.lrj.his.billing.domain.InvoiceRepository;
import com.lrj.his.billing.event.InvoicePaidPublisher;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoicePaidPublisher publisher;

    public BillingService(InvoiceRepository invoiceRepository, InvoicePaidPublisher publisher) {
        this.invoiceRepository = invoiceRepository;
        this.publisher = publisher;
    }

    /**
     * 据医嘱事件生成待缴费账单。以 encounterId 唯一保证幂等:
     * Kafka 重复投递时不会重复建单。
     */
    @Transactional
    public void createFromOrderPlaced(OrderPlacedEvent event) {
        if (invoiceRepository.existsByEncounterId(event.encounterId())) {
            log.info("账单已存在, 跳过 encounterId={}", event.encounterId());
            return;
        }
        Invoice invoice = Invoice.create(event.encounterId(), event.patientId(), event.deptCode());
        for (OrderPlacedEvent.OrderLine line : event.items()) {
            invoice.addItem(line.orderId(), line.itemCode(), line.itemName(), line.quantity(), line.amount());
        }
        invoiceRepository.save(invoice);
        log.info("生成账单 encounterId={} 金额={}", event.encounterId(), invoice.getTotalAmount());
    }

    /** 缴费:本地强一致事务置 PAID,提交后回发支付事件推进医嘱执行。 */
    @Transactional
    public Invoice pay(Long invoiceId, String payMethod) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> BusinessException.of(ResultCode.INVOICE_NOT_FOUND));
        List<Long> orderIds = invoice.pay(payMethod);
        invoiceRepository.save(invoice);
        publisher.publish(new InvoicePaidEvent(invoice.getId(), invoice.getEncounterId(), orderIds));
        return invoice;
    }

    @Transactional(readOnly = true)
    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ResultCode.INVOICE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Invoice getByEncounter(Long encounterId) {
        return invoiceRepository.findByEncounterId(encounterId)
                .orElseThrow(() -> BusinessException.of(ResultCode.INVOICE_NOT_FOUND));
    }
}
