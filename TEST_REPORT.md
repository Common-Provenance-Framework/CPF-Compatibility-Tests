# CPF Compatibility Test Report

**Date:** 2026-06-20
**Java CPF-Store:** latest main (Docker)
**Python:** ProvPython (prov library)
**Schemas:** W3C original + Java CPF-Store custom

## Summary

| Status | Count | Meaning |
|--------|-------|---------|
| **PASS** | 29 (60%) | System behaves correctly |
| **CHECK** | 17 (35%) | Known design difference -- team decides |
| **FAIL** | 2 (4%) | Needs fix |

## Test Results

| # | Test | W3C | Java | Python | Status | Reason |
|---|------|-----|------|--------|--------|--------|
| 1 | issue01_v1_top_level_only | accept | reject | accept | **CHECK** | Java rejects top-level prefix by design |
| 2 | issue01_v2_inside_bundle_only | accept | accept | accept | **PASS** |  |
| 3 | issue01_v3_both_places | accept | reject | accept | **CHECK** | Java rejects top-level prefix |
| 4 | issue01_v4_only_bundle_id_prefix_inside | accept | reject | accept | **CHECK** | Jan's suggestion -- Java schema rejects |
| 5 | issue01_v5_no_prefix_anywhere | accept | accept | reject | **CHECK** | Python can't resolve without prefix |
| 6 | issue01_v6_jan_schema_format | accept | accept | accept | **PASS** |  |
| 7 | issue01_v7_jan_deserializer_format | accept | reject | accept | **CHECK** | Jan's deserializer format rejected by his schema |
| 8 | issue01_v8_jan_preprocessed_format | accept | reject | reject | **FAIL** | Both reject spec-valid doc (@id + prefix) |
| 9 | issue02_v1_prov_xsd_explicit | accept | accept | accept | **PASS** |  |
| 10 | issue02_v2_prov_xsd_not_declared | accept | accept | accept | **PASS** |  |
| 11 | issue02_v3_only_prov_declared | accept | accept | accept | **PASS** |  |
| 12 | issue02_v4_only_xsd_declared | accept | accept | accept | **PASS** |  |
| 13 | issue02_v5_neither_declared_nor_used | accept | accept | accept | **PASS** |  |
| 14 | issue03_v1_no_id | accept | accept | accept | **PASS** |  |
| 15 | issue03_v2_id_correct | accept | reject | reject | **CHECK** | @id in spec but both reject |
| 16 | issue03_v3_id_wrong | accept | reject | reject | **PASS** | Wrong @id correctly rejected |
| 17 | issue03_v4_top_level_prefix_no_id | accept | reject | accept | **CHECK** | Java rejects top-level prefix |
| 18 | issue03_v5_two_bundles_no_id | accept | reject | accept | **CHECK** | Java limits to 1 bundle |
| 19 | issue04_v1_default_in_prefix | accept | accept | accept | **PASS** |  |
| 20 | issue04_v2_defaultNamespace_top_level | reject | reject | reject | **PASS** | Invalid per spec, all reject |
| 21 | issue04_v3_both_default_forms | reject | reject | reject | **PASS** | Invalid per spec, all reject |
| 22 | issue04_v4_default_resolves_unprefixed | accept | accept | accept | **PASS** |  |
| 23 | issue04_v5_no_default_all_explicit | accept | accept | accept | **PASS** |  |
| 24 | issue05_v1_all_in_arrays | accept | accept | accept | **PASS** |  |
| 25 | issue05_v2_bare_string | accept | reject | accept | **CHECK** | Spec allows, Java requires arrays |
| 26 | issue05_v3_bare_typed | accept | reject | accept | **CHECK** | Spec allows, Java requires arrays |
| 27 | issue05_v4_all_bare | accept | reject | accept | **CHECK** | Python output format -- Java rejects |
| 28 | issue05_v5_native_number | accept | reject | accept | **CHECK** | MAY use native nums -- both spec-valid |
| 29 | issue08_v1_blank_hash | accept | accept | accept | **PASS** |  |
| 30 | issue08_v2_blank_slash | accept | accept | accept | **PASS** |  |
| 31 | issue08_v3_no_blank_prefix | accept | accept | accept | **PASS** |  |
| 32 | issue08_v4_underscore_blank_node | accept | accept | reject | **CHECK** | Python rejects _: as entity ID |
| 33 | issue09_v1_correct_typed | accept | accept | accept | **PASS** |  |
| 34 | issue09_v2_bare_strings | accept | accept | accept | **FAIL** | Silent semantic corruption -- two strings instead of typed object |
| 35 | issue09_v3_single_string_type | accept | accept | accept | **PASS** |  |
| 36 | issue09_v4_multiple_types | accept | accept | accept | **PASS** |  |
| 37 | issue09_v5_bare_type_not_array | accept | reject | accept | **CHECK** | Spec allows, Java requires array |
| 38 | issue10_v1_timezone_offset | accept | accept | accept | **PASS** |  |
| 39 | issue10_v2_utc | accept | accept | accept | **PASS** |  |
| 40 | issue10_v3_no_milliseconds | accept | accept | accept | **PASS** |  |
| 41 | issue10_v4_microseconds | accept | accept | accept | **PASS** |  |
| 42 | issue12_v1_only_bundle | accept | accept | accept | **PASS** |  |
| 43 | issue12_v2_entity_at_top | accept | reject | accept | **CHECK** | CPF-Store restricts to bundle only |
| 44 | issue12_v3_activity_at_top | accept | reject | accept | **CHECK** | CPF-Store restricts to bundle only |
| 45 | issue12_v4_relation_at_top | accept | reject | accept | **CHECK** | CPF-Store restricts to bundle only |
| 46 | issue13_v1_full_uri | accept | accept | accept | **PASS** |  |
| 47 | issue13_v2_prefixed | accept | accept | accept | **PASS** |  |
| 48 | issue13_v3_default_unprefixed | accept | accept | accept | **PASS** |  |

## FAIL Details (2 tests)

### issue01_v8: @id + top-level prefix
Both Java schema and Python reject a document that has @id inside the bundle AND prefix at top level.
The W3C spec allows both. This is a cross-implementation incompatibility.

### issue09_v2: prov:type semantic corruption
Python outputs `["cpm:forwardConnector", "prov:QUALIFIED_NAME"]` (two strings) instead of
`[{"$": "cpm:forwardConnector", "type": "prov:QualifiedName"}]` (one typed object).
Both schemas accept it because syntactically it's a valid array of strings.
But the data MEANS something different. This is a silent semantic error.

## CHECK Summary (17 tests -- team decisions needed)

| Category | Count | Tests |
|----------|-------|-------|
| Java schema stricter than spec (arrays, strings) | 5 | #05 v2-v5, #09 v5 |
| Java rejects top-level prefix (design) | 5 | #01 v1,v3,v4,v7, #03 v4 |
| CPF-Store design decisions | 4 | #03 v5, #12 v2-v4 |
| @id handling (ProvToolbox vs spec) | 1 | #03 v2 |
| Python limitations | 2 | #01 v5, #08 v4 |

