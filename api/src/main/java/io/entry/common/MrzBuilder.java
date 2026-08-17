package io.entry.common;

/**
 * 여권 기계판독영역(MRZ) 2행 문자열을 서버에서 조립한다. 프론트는 이 문자열을 그대로 표시만 한다.
 */
public final class MrzBuilder {

    private static final int WIDTH = 44;

    private MrzBuilder() {
    }

    public static String[] build(String zoneId, long savedCount, String comparisonAxis, Market originMarket, Market targetMarket) {
        String line1 = pad("ENTRY<<SEOUL<SEONGSU<<" + orDefault(zoneId, "NONE"));
        String marketSegment = targetMarket != null && targetMarket != originMarket
                ? originMarket + ">" + targetMarket
                : originMarket.name();
        String line2 = pad(String.format("SAVED%02d<<INTENT<%s<<MKT<%s", savedCount, orDefault(comparisonAxis, "NONE"), marketSegment));
        return new String[]{line1, line2};
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String pad(String value) {
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < WIDTH) sb.append('<');
        return sb.substring(0, WIDTH);
    }
}
