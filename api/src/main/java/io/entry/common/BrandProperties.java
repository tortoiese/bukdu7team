package io.entry.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 브랜드 종속 값(팝업 ID, 발급 장소 등)은 코드에 하드코딩하지 않고 설정에서 읽는다(CLAUDE.md R7).
 * 프론트의 brand/mcm.json과 짝을 이루는 백엔드 쪽 설정이다.
 */
@Component
@ConfigurationProperties(prefix = "entry.brand")
public class BrandProperties {

    private String popupId = "MCM-SEONGSU-2026";
    private String issuedPlace = "SEOUL / SEONGSU";
    private String originStoreId = "KR-SEONGSU";
    private String originStoreName = "성수 팝업";

    public String getPopupId() {
        return popupId;
    }

    public void setPopupId(String popupId) {
        this.popupId = popupId;
    }

    public String getIssuedPlace() {
        return issuedPlace;
    }

    public void setIssuedPlace(String issuedPlace) {
        this.issuedPlace = issuedPlace;
    }

    public String getOriginStoreId() {
        return originStoreId;
    }

    public void setOriginStoreId(String originStoreId) {
        this.originStoreId = originStoreId;
    }

    public String getOriginStoreName() {
        return originStoreName;
    }

    public void setOriginStoreName(String originStoreName) {
        this.originStoreName = originStoreName;
    }
}
