package com.lrj.his.api.event;

import java.io.Serializable;

/**
 * 处方审核结果事件。药师通过/驳回处方后发出,notify 据此通知开方医生。
 */
public record RxReviewedEvent(
        Long encounterId,
        Long patientId,
        Long doctorId,
        boolean passed,
        String opinion,
        String pharmacistName) implements Serializable {
}
