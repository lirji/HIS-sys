package com.lrj.his.api.event;

import java.io.Serializable;

/**
 * 处方待审事件。一次就诊提交后,其药品医嘱进入待审方态,发此事件触发药师待办通知。
 */
public record RxReviewRequestedEvent(
        Long encounterId,
        Long patientId,
        String deptCode,
        Long doctorId,
        String doctorName,
        int drugItemCount) implements Serializable {
}
