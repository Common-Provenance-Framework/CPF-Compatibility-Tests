package org.commonprovenance.compatibility;

import java.util.Map;

final class SelectedExpectations {
    private static final Map<String, Boolean> W3C_EXPECTED = Map.of(
            "issue01_prefix_placement/issue01_v1_top_level_only.json", true,
            "issue05_arrays_and_numbers/issue05_v1_all_in_arrays.json", true,
            "issue10_timestamps/issue10_v2_utc.json", true,
            "issue13_qualified_names/issue13_v3_default_unprefixed.json", false
    );

    private static final Map<String, Boolean> PROV_EXPECTED = Map.of(
            "issue01_prefix_placement/issue01_v1_top_level_only.json", false,
            "issue01_prefix_placement/issue01_v2_inside_bundle_only.json", false,
            "issue01_prefix_placement/issue01_v3_both_places.json", false,
            "issue04_default_namespace/issue04_v1_default_in_prefix.json", false,
            "issue02_implicit_prov_xsd/issue02_v2_prov_xsd_not_declared.json", false
    );

    private SelectedExpectations() {
    }

    static Boolean expectedW3c(String displayName) {
        return W3C_EXPECTED.get(displayName);
    }

    static Boolean expectedProv(String displayName) {
        return PROV_EXPECTED.get(displayName);
    }
}