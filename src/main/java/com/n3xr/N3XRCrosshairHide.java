package com.n3xr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keputusan "crosshair vanilla disembunyikan atau tidak", dipanggil dari
 * mixin/CrosshairHideMixin. Dipisah dari mixin supaya bisa nyimpan state
 * dan nulis log (class di package mixin tidak boleh punya static field).
 *
 * Aturan: begitu modul Crosshair (custom) dinyalakan, crosshair vanilla
 * selalu disembunyikan -- termasuk kalau grid-nya masih kosong.
 *
 * Tiap kali statusnya berubah (dan sekali saat pertama jalan) ditulis
 * satu baris ke latest.log: "[N3XR] crosshair vanilla di-hide = true/false".
 * Kalau baris itu TIDAK pernah muncul di log, berarti mixin-nya tidak
 * jalan di build yang sedang dipakai.
 */
public final class N3XRCrosshairHide {

        private static final Logger LOGGER = LoggerFactory.getLogger("n3xr");
        private static Boolean last = null;

        private N3XRCrosshairHide() {}

        public static boolean shouldHideVanilla() {
                boolean hide = N3XRConfig.customCrosshairEnabled;
                if (last == null || last != hide) {
                        last = hide;
                        LOGGER.info("[N3XR] crosshair vanilla di-hide = {}", hide);
                }
                return hide;
        }
}
