# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 8/8 (100.0%)
- **Function parity:** 22/27 matched (target 45) — 81.5%
- **Class/type parity:** 9/13 matched (target 17) — 69.2%
- **Combined symbol parity:** 31/40 matched (target 62) — 77.5%
- **Average inline-code cosine:** 0.46 (function body across 8 matched files)
- **Average documentation cosine:** 0.68 (doc text across 8 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 6 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. addrinfo

- **Target:** `dnslookup.Addrinfo`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 51306.4
- **Functions:** 5/8 matched (target 6)
- **Missing functions:** `as_addrinfo`, `from_ptr`, `drop`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `libc_c_char`, `Item`
- **Tests:** 1/1 matched

### 2. err

- **Target:** `dnslookup.Err`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 10807.2
- **Functions:** 5/6 matched (target 16)
- **Missing functions:** `gai_err_to_io_err`
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 3. types

- **Target:** `dnslookup.Types`
- **Similarity:** 0.20
- **Dependents:** 0
- **Priority Score:** 10608.0
- **Functions:** 1/2 matched (target 12)
- **Missing functions:** `eq`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_

### 4. hostname

- **Target:** `dnslookup.Hostname`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 10306.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `libc_c_char`
- **Tests:** 1/1 matched

### 5. nameinfo

- **Target:** `dnslookup.Nameinfo`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 10304.4
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `libc_c_char`
- **Tests:** 1/1 matched

### 6. lookup

- **Target:** `dnslookup.Lookup`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 603.8
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 7. win

- **Target:** `dnslookup.Win`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 107.5
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 8. lib

- **Target:** `dnslookup.Lib`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

