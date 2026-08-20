package io.entry.catalog;

/** 팝업 매장 4곳(성수/더현대/합정/문래). 어느 매장 QR로 들어왔는지에 따라 패스포트 발급 정보가 갈린다. */
public record Store(String storeId, String storeName, String issuedPlace, String popupId) {
}
