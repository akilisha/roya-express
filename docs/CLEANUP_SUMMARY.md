# Cleanup Summary: roya-plugins:ai

## Analysis Results

### ✅ What `roya-plugins:agentic` Needs from `roya-plugins:ai`
- `AI` interface - **MUST STAY** (core abstraction)
- `AIOptions` - **MUST STAY** (used by AI interface methods)

**Conclusion**: Nothing to relocate. Both are essential to the AI plugin itself.

---

## 🗑️ Legacy/Dead Code to Remove

### 1. `AIServiceImpl` (Legacy Implementation)
- **Status**: Only used by `createLegacyAI()` method
- **Location**: `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/AIServiceImpl.java`
- **Size**: ~650 lines
- **Dependencies**: Uses `providers/OpenAIClient`, `providers/LLMProvider`, `LLMResponse`
- **Action**: Remove if `UnifiedAIService` is the primary path (which it is)

### 2. `providers/` Package (Legacy Provider Pattern)
- **Files**:
  - `LLMProvider.java` - Interface for legacy provider abstraction
  - `OpenAIClient.java` - Implementation using old OpenAI SDK
- **Status**: Only used by `AIServiceImpl`
- **Action**: Remove with `AIServiceImpl`

### 3. `LLMResponse` (Legacy Response Wrapper)
- **Status**: Only used by `AIServiceImpl` and `providers/` package
- **Location**: `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/LLMResponse.java`
- **Action**: Remove (replaced by `AIResponse<T>` in new architecture)

### 4. `Document` (Unused)
- **Status**: Defined but never imported/used anywhere
- **Location**: `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/Document.java`
- **Action**: Remove or relocate to vectors/RAG module if needed in future

### 5. `createLegacyAI()` Method
- **Status**: Defined in `AIPlugin` but never called
- **Action**: Remove with `AIServiceImpl`

---

## Current Architecture (UnifiedAIService Path)

The primary path uses:
- `UnifiedAIService` - Main AI service implementation
- `LangChainAdapter` - LangChain4j integration
- `LangGraphAdapter` - LangGraph4j integration  
- `GoogleADKAdapter` - Google ADK integration (optional)
- `AILibraryConfig` - Configuration for all libraries
- `AILibraryFactory` - Factory for creating library adapters

**This is the clean, library-first architecture.**

---

## Recommended Cleanup Actions

1. ✅ **Remove `AIServiceImpl.java`** (~650 lines)
2. ✅ **Remove `providers/` package** (2 files)
3. ✅ **Remove `LLMResponse.java`**
4. ✅ **Remove `Document.java`** (or relocate if needed for RAG/vectors)
5. ✅ **Remove `createLegacyAI()` from `AIPlugin.java`**

**Estimated cleanup**: ~700+ lines of dead code removed

---

## Verification

Before removing, verify:
- [ ] No external code references `AIServiceImpl` directly
- [ ] No external code uses `LLMProvider` interface
- [ ] All tests pass (if any test legacy code)
- [ ] `UnifiedAIService` is fully functional

---

## Next Steps

1. Check if `createLegacyAI` is called anywhere (search entire codebase)
2. Check for any tests using legacy classes
3. Remove legacy code
4. Verify compilation
5. Update documentation if needed

