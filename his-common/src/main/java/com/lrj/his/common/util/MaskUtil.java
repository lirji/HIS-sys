package com.lrj.his.common.util;

/**
 * 敏感字段脱敏。对外 DTO 用,避免明文身份证/手机号外泄。
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /** 身份证: 保留前3后4,中间星号。 */
    public static String idCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }

    /** 手机号: 保留前3后4。 */
    public static String phone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
