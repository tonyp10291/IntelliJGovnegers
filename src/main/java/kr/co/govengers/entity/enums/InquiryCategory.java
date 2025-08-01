package kr.co.govengers.entity.enums;

public enum InquiryCategory {
    상품문의("상품문의"),
    배송문의("배송문의"),
    결제문의("결제문의"),
    회원문의("회원문의"),
    기타문의("기타문의");

    private final String displayName;

    InquiryCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static InquiryCategory fromString(String category) {
        for (InquiryCategory ic : InquiryCategory.values()) {
            if (ic.name().equals(category) || ic.displayName.equals(category)) {
                return ic;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + category);
    }
}