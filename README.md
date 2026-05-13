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
