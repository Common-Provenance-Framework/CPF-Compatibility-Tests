# CPF PROV-JSON Compatibility Tests

Test inputs for verifying compatibility between the PROV-JSON specification, Java CPF-Store (ProvToolbox), and Python prov-storage (ProvPython).

## Structure

```
issue01_prefix_placement/       8 variants - where prefix is declared
issue02_implicit_prov_xsd/      5 variants - prov/xsd explicit vs implicit
issue03_bundle_id/              5 variants - @id property in bundles
issue04_default_namespace/      5 variants - "default" vs "defaultNamespace"
issue05_arrays_and_numbers/     5 variants - bare vs array values, native vs string numbers
issue08_blank_namespace/        4 variants - blank# vs blank/, _: vs blank:
issue09_prov_type_format/       5 variants - typed object vs bare strings for prov:type
issue10_timestamps/             4 variants - timezone, milliseconds, microseconds
issue12_top_level_properties/   4 variants - entities/activities at top level vs inside bundle
issue13_qualified_names/        3 variants - full URI, prefixed, unprefixed identifiers
results/                        Detailed test results per issue
jan_schema_test_valid.json      Jan's valid schema test document (reference)
jan_deserializer_test_doc.json  Jan's deserializer test document (reference)
base_document.json              CPM document adapted from CPF-Toolbox (reference)
```

## How to test

### Against Python (ProvPython)
```bash
python3 run_tests.py
```

### Against Java (JUnit only)
This repository also contains a minimal Java test setup that checks only:

1. whether each `issue*/**/*.json` input is valid against the W3C schema
2. whether each `issue*/**/*.json` input can be deserialized by `ProvToolbox (org.openprovenance.prov)`

It does not call the CPF-Store API, does not validate against the CPF-Store schema, and does not write any result tables.

```bash
mvn test
```

By default, the Java tests load the W3C schema from the sibling `CPF-Storage` repository at:

```bash
../CPF-Storage/src/main/resources/prov-json-schama-original.json
```

You can override that location if needed:

```bash
mvn test -Dprov.w3c.schema.path=/absolute/path/to/prov-json-schama-original.json
```

#### Regression assertions (`SelectedExpectations`)

The class `SelectedExpectations` in the Java test sources defines a subset of files for which the test outcome is **strictly asserted** (rather than just reported). This is useful for pinning known-happy or known-error behaviour as a regression guard.

Each entry maps a file path (relative to the issue folder root, e.g. `issue01_prefix_placement/issue01_v1_top_level_only.json`) to an expected outcome:

- `true` — the file **must** pass (schema-valid or deserializable); the test fails if it does not.
- `false` — the file **must** fail; the test fails if it unexpectedly passes. If test fails as expected, `[ASSERTION]` message is printed with details (why test fails).

There are two independent maps — one for W3C schema validation (`W3C_EXPECTED`) and one for ProvToolbox deserialization (`PROV_EXPECTED`). A file can appear in both, in one, or in neither.

To add a new assertion, edit `SelectedExpectations.java`:

```java
// W3C schema must validate
"issue05_arrays_and_numbers/issue05_v1_all_in_arrays.json", true,

// ProvToolbox deserialization must fail
"issue01_prefix_placement/issue01_v1_top_level_only.json", false,
```

Files not listed in `SelectedExpectations` are still executed and reported, but never cause the build to fail.

### Against Java CPF-Store (Docker)
```bash
cd ../CPF-Storage
export STORE_URL=http://localhost:8081/api/v1/
docker compose up --build --detach
# Then:
python3 run_tests.py --java-api http://localhost:8081
```

## Test matrix columns

| Column | What it tests |
|--------|--------------|
| W3C Schema | Original PROV-JSON schema from spec authors |
| Java Schema | Java CPF-Store's modified schema |
| Java API | Full end-to-end: schema → preprocessing → ProvToolbox deserialization |
| Python | ProvPython library deserialization + round-trip |
