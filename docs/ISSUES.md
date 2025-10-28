# Roya Framework - Issues

**Last Updated**: January 2025

This document tracks problems we're experiencing, blockers, and items needing clarification. Once resolved, issues are moved to MILESTONES with resolution notes.

---

## Template for Issue Entries

```markdown
### [Issue Title]
**ID**: ISSUE-XXX  
**Category**: [Bug / Blocker / Design Question / Clarification Needed]  
**Priority**: [P0-Critical / P1-High / P2-Medium / P3-Low]  
**Opened**: [Date]  
**Opened By**: [Name]  
**Status**: [Open / Investigating / Blocked / Resolved]  
**Assigned To**: [Name or Unassigned]

**Description**:
What is the problem?

**Impact**:
How does this affect the project?

**Steps to Reproduce** (if bug):
1. Step 1
2. Step 2

**Expected Behavior**:
What should happen?

**Actual Behavior**:
What actually happens?

**Possible Solutions**:
- Option 1
- Option 2

**Related**:
- Links to roadmap items
- Links to other issues

**Updates**:
- [Date]: Update 1
- [Date]: Update 2

**Resolution** (when closed):
How was this resolved?
```

---

## Open Issues

### Virtual Thread Pinning with Synchronized Blocks
**ID**: ISSUE-001  
**Category**: Design Question  
**Priority**: P1-High  
**Opened**: January 13, 2025  
**Opened By**: Core Team  
**Status**: Open  
**Assigned To**: Unassigned

**Description**:
Virtual threads can "pin" to platform threads when they hit `synchronized` blocks or native calls, reducing concurrency benefits. We need to decide how to handle this in middleware and plugins.

**Impact**:
Could limit the 1M+ concurrent connection goal if not handled properly.

**Expected Behavior**:
Virtual threads should never pin, maintaining full concurrency.

**Actual Behavior**:
Any middleware using `synchronized` will pin virtual threads.

**Possible Solutions**:
- Option 1: Use `ReentrantLock` instead of `synchronized` everywhere
- Option 2: Document this and let plugin authors decide
- Option 3: Provide utilities that detect and warn about pinning
- Option 4: Accept some pinning in non-critical paths

**Related**:
- Roadmap: Phase 1 (Core Abstractions)
- Roadmap: Phase 4 (Plugin System)

**Updates**:
- *Needs investigation and decision*

---

### ScopedValue Availability in Java 21
**ID**: ISSUE-002  
**Category**: Clarification Needed  
**Priority**: P2-Medium  
**Opened**: January 13, 2025  
**Opened By**: Core Team  
**Status**: Open  
**Assigned To**: Unassigned

**Description**:
Need to verify ScopedValue API status in Java 21 LTS. Is it preview, finalized, or still incubating?

**Impact**:
If still preview, we'd need `--enable-preview` flag, which affects adoption.

**Expected Behavior**:
ScopedValue is finalized and stable in Java 21.

**Actual Behavior**:
Unknown - needs verification.

**Possible Solutions**:
- Research JEP status
- If preview: decide if we use it anyway or wait for Java 22+
- If unstable: fallback to ThreadLocal with virtual thread-safe cleanup

**Related**:
- Roadmap: Phase 1 (Core Abstractions)
- Architecture: Request context propagation

**Updates**:
- *Needs research*

---

### Helidon Níma Maturity
**ID**: ISSUE-003  
**Category**: Blocker (Potential)  
**Priority**: P1-High  
**Opened**: January 13, 2025  
**Opened By**: Core Team  
**Status**: Open  
**Assigned To**: Unassigned

**Description**:
Helidon Níma is relatively new (Helidon 4.x). Need to verify production-readiness, stability, and community support.

**Impact**:
If Níma is unstable or abandoned, we'd need to switch HTTP servers (major work).

**Expected Behavior**:
Níma is stable, actively maintained, and production-ready.

**Actual Behavior**:
Unknown - needs investigation.

**Possible Solutions**:
- Research Helidon 4.x adoption and stability
- Check GitHub activity and issue tracker
- Test Níma with load testing
- If unstable: create abstraction layer allowing server swap
- Fallback options: Netty, Jetty, custom server

**Related**:
- Roadmap: Phase 1 (Core Abstractions)
- Architecture: Runtime foundation

**Updates**:
- *Needs investigation*

---

### JSON Serialization Library Choice
**ID**: ISSUE-004  
**Category**: Design Question  
**Priority**: P2-Medium  
**Opened**: January 13, 2025  
**Opened By**: Core Team  
**Status**: Open  
**Assigned To**: Unassigned

**Description**:
Need to decide on JSON library: Jackson, Gson, or custom?

**Impact**:
Affects performance, ease of use, and record support.

**Options**:
1. **Jackson** - Industry standard, fast, great record support
2. **Gson** - Simple, but slower and less record support
3. **Custom** - Maximum control, but maintenance burden

**Considerations**:
- Record serialization support (critical for our design)
- Performance (we're targeting 50K RPS)
- Customization (AI structured outputs need schema generation)
- Size (want minimal dependencies)

**Possible Solutions**:
- Benchmark all three with records
- Consider making it pluggable (user can choose)

**Related**:
- Roadmap: Phase 3 (Essential Middleware - JSON)
- Roadmap: Phase 6 (AI - Structured outputs)

**Updates**:
- *Needs benchmarking*

---

## Blocked Issues

*No blocked issues currently*

---

## Resolved Issues

*Resolved issues will be moved here with resolution notes*

### Example: Issue Title
**ID**: ISSUE-XXX  
**Resolved**: [Date]  
**Resolution**: Description of how it was resolved  
**Moved to**: MILESTONES.md (if applicable)

---

## Issue Statistics

| Status | Count |
|--------|-------|
| Open | 4 |
| Investigating | 0 |
| Blocked | 0 |
| Resolved | 0 |

| Priority | Count |
|----------|-------|
| P0-Critical | 0 |
| P1-High | 2 |
| P2-Medium | 2 |
| P3-Low | 0 |

| Category | Count |
|----------|-------|
| Bug | 0 |
| Blocker | 1 |
| Design Question | 2 |
| Clarification Needed | 1 |

---

## Issue Workflow

```
New Issue → Open → Investigating → [Blocked] → Resolved → Moved to MILESTONES
```

**States:**
- **Open**: Issue identified, needs investigation
- **Investigating**: Actively researching or working on solution
- **Blocked**: Cannot proceed due to external dependency
- **Resolved**: Issue is solved
- **Moved to MILESTONES**: Documented as part of milestone completion

---

**This document is updated as issues arise and are resolved. Critical issues (P0) are escalated immediately.**
