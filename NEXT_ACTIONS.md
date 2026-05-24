# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/8 (25.0%)
- **Function parity:** 2/27 matched (target 24) — 7.4%
- **Class/type parity:** 6/13 matched (target 9) — 46.2%
- **Combined symbol parity:** 8/40 matched (target 33) — 20.0%
- **Average inline-code cosine:** 0.14 (function body across 2 matched files)
- **Average documentation cosine:** 0.76 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 2 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. err

- **Target:** `dnslookup.Err`
- **Similarity:** 0.07
- **Dependents:** 0
- **Priority Score:** 50809.3
- **Functions:** 1/6 matched (target 12)
- **Missing functions:** `new`, `kind`, `error_num`, `from`, `gai_err_to_io_err`
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 2. types

- **Target:** `dnslookup.Types`
- **Similarity:** 0.20
- **Dependents:** 0
- **Priority Score:** 10608.0
- **Functions:** 1/2 matched (target 12)
- **Missing functions:** `eq`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

