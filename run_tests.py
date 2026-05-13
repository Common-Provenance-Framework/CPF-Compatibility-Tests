#!/usr/bin/env python3
"""
Run PROV-JSON compatibility tests across W3C Schema, Java Schema, and Python ProvPython.
Optionally test against running Java CPF-Store API.

Usage:
    python3 run_tests.py                              # Test schemas + Python only
    python3 run_tests.py --java-api http://localhost:8081  # Also test Java API
"""

import json
import os
import sys
import argparse
import subprocess

try:
    import prov.model as prov_model
except ImportError:
    print("ERROR: prov library not installed. Run: pip3 install prov")
    sys.exit(1)

try:
    import jsonschema
except ImportError:
    print("ERROR: jsonschema library not installed. Run: pip3 install jsonschema")
    sys.exit(1)


def load_schema(path):
    with open(path) as f:
        return json.load(f)


def test_schema(doc, schema):
    try:
        jsonschema.validate(doc, schema)
        return "PASS"
    except jsonschema.ValidationError:
        return "FAIL"


def test_python(doc):
    try:
        prov_doc = prov_model.ProvDocument.deserialize(
            content=json.dumps(doc), format="json"
        )
        return "PASS"
    except Exception:
        return "FAIL"


def test_java_api(doc_path, api_url, org_id, key_path):
    try:
        import base64
        with open(doc_path, "rb") as f:
            b64_doc = base64.b64encode(f.read()).decode("ascii")

        sig_result = subprocess.run(
            ["openssl", "dgst", "-sha256", "-sign", key_path, doc_path],
            capture_output=True,
        )
        b64_sig = base64.b64encode(sig_result.stdout).decode("ascii")

        import urllib.request

        payload = json.dumps({
            "organizationIdentifier": org_id,
            "document": b64_doc,
            "documentFormat": "JSON",
            "signature": b64_sig,
            "createdOn": "1234567890",
        }).encode("utf-8")

        req = urllib.request.Request(
            f"{api_url}/api/v1/documents",
            data=payload,
            headers={"Content-Type": "application/json", "Accept": "application/json"},
        )
        response = urllib.request.urlopen(req)
        return "PASS"
    except Exception as e:
        error_msg = str(e)
        if "500" in error_msg:
            return "FAIL*"  # passed schema, failed ProvToolbox
        elif "400" in error_msg or "422" in error_msg:
            return "FAIL"  # schema rejected
        return "FAIL"


def main():
    parser = argparse.ArgumentParser(description="Run PROV-JSON compatibility tests")
    parser.add_argument("--java-api", help="Java CPF-Store API URL (e.g. http://localhost:8081)")
    parser.add_argument("--org-id", default="testorg", help="Organization ID for Java API tests")
    parser.add_argument("--key-path", help="Path to signing key for Java API tests")
    args = parser.parse_args()

    test_dir = os.path.dirname(os.path.abspath(__file__))

    # Find schemas (look in sibling CPF-Storage repo)
    storage_dir = os.path.join(os.path.dirname(test_dir), "CPF-Storage")
    w3c_schema_path = os.path.join(storage_dir, "src/main/resources/prov-json-schama-original.json")
    java_schema_path = os.path.join(storage_dir, "src/main/resources/prov-json-schema.json")

    if not os.path.exists(w3c_schema_path) or not os.path.exists(java_schema_path):
        print(f"ERROR: Schema files not found. Expected CPF-Storage repo at {storage_dir}")
        sys.exit(1)

    w3c_schema = load_schema(w3c_schema_path)
    java_schema = load_schema(java_schema_path)

    # Find all issue folders
    issue_dirs = sorted(
        [d for d in os.listdir(test_dir) if d.startswith("issue") and os.path.isdir(os.path.join(test_dir, d))]
    )

    header = f"{'Issue':<30} {'Variant':<35} {'W3C':<6} {'Java Sch':<9} {'Python':<8}"
    if args.java_api:
        header += f" {'Java API':<9}"
    print(header)
    print("=" * len(header))

    for issue_dir in issue_dirs:
        full_dir = os.path.join(test_dir, issue_dir)
        json_files = sorted([f for f in os.listdir(full_dir) if f.endswith(".json")])

        for fname in json_files:
            path = os.path.join(full_dir, fname)
            with open(path) as f:
                doc = json.load(f)

            w3c = test_schema(doc, w3c_schema)
            java_s = test_schema(doc, java_schema)
            py = test_python(doc)

            variant = fname.replace(".json", "").replace(issue_dir.split("_")[0] + "_", "")
            line = f"{issue_dir:<30} {variant:<35} {w3c:<6} {java_s:<9} {py:<8}"

            if args.java_api:
                java_api = test_java_api(path, args.java_api, args.org_id, args.key_path)
                line += f" {java_api:<9}"

            print(line)

        print()


if __name__ == "__main__":
    main()
