package com.example.projectfinal;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Validates the existing financial form and calculates reference figures outside the model. */
public final class SavingsProfile {
    public static String validateAmount(String input, boolean allowZero) {
        String value = input.trim().replace(",", "");
        if (!value.matches("\\d{1,9}(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException("กรอกจำนวนเงินเป็นตัวเลข ไม่เกิน 2 ตำแหน่งทศนิยม");
        }
        BigDecimal amount = new BigDecimal(value);
        if (amount.compareTo(new BigDecimal("100000000")) > 0
                || amount.signum() < 0 || (!allowZero && amount.signum() == 0)) {
            throw new IllegalArgumentException("กรอกจำนวนเงิน" + (allowZero ? "ตั้งแต่ 0" : "มากกว่า 0") + " ถึง 100,000,000 บาท");
        }
        return amount.stripTrailingZeros().toPlainString();
    }

    public static String validateDays(String input) {
        if (!input.trim().matches("\\d{1,5}")) {
            throw new IllegalArgumentException("กรอกจำนวนวันเป็นจำนวนเต็มตั้งแต่ 1 ถึง 36,500");
        }
        int days = Integer.parseInt(input.trim());
        if (days < 1 || days > 36500) {
            throw new IllegalArgumentException("กรอกจำนวนวันตั้งแต่ 1 ถึง 36,500");
        }
        return Integer.toString(days);
    }

    public static String context(String expenses, String goal, String days) {
        BigDecimal daily = new BigDecimal(validateAmount(goal, false))
                .divide(new BigDecimal(validateDays(days)), 2, RoundingMode.CEILING);
        return "ข้อมูลผู้ใช้ที่ตรวจสอบแล้ว:\n"
                + "ค่าใช้จ่ายต่อเดือน: " + validateAmount(expenses, true) + " บาท\n"
                + "เป้าหมายเงินออม: " + validateAmount(goal, false) + " บาท\n"
                + "ระยะเวลา: " + validateDays(days) + " วัน\n"
                + "เงินที่ต้องออมต่อวัน (ปัดขึ้น 2 ตำแหน่ง): " + daily.toPlainString() + " บาท\n"
                + "ประมาณการเงินออมต่อ 30 วัน: " + daily.multiply(new BigDecimal("30")).toPlainString() + " บาท\n"
                + "ยังไม่ทราบรายได้ เงินออมเดิม หรือภาระหนี้ของผู้ใช้";
    }
}
