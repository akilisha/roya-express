# Contributing to Roya Framework

Thank you for your interest in contributing to Roya! This guide will help you get started.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Style](#code-style)
- [Submitting Changes](#submitting-changes)
- [Areas We Need Help](#areas-we-need-help)
- [Community](#community)

---

## Getting Started

### Prerequisites

- **Java 21+** (LTS required for virtual threads)
- **Maven** or **Gradle** (your choice)
- **Git**
- **IDE**: IntelliJ IDEA (recommended) or VS Code with Java extensions

### Fork and Clone

```bash
# Fork the repository on GitHub
# Then clone your fork
git clone https://github.com/YOUR_USERNAME/roya-framework.git
cd roya-framework

# Add upstream remote
git remote add upstream https://github.com/original/roya-framework.git
```

---

## Development Setup

### Phase 1: Currently Design Phase (Q1 2025)

We're currently in the design phase. The best way to contribute right now is:

1. **Review the architecture documents**
   - Read [WHITEPAPER.md](WHITEPAPER.md)
   - Study [ARCHITECTURE.md](ARCHITECTURE.md)
   - Provide feedback in [GitHub Discussions](https://github.com/yourusername/roya-framework/discussions)

2. **Suggest improvements**
   - API design feedback
   - Performance considerations
   - Use case scenarios
   - Express compatibility concerns

3. **Spread the word**
   - Star the repo ⭐
   - Share on social media
   - Write blog posts about the vision

### Phase 2: Prototype Development (Starting Q1 2025)

Once we start coding:

```bash
# Build the project
./mvnw clean install

# Run tests
./mvnw test

# Run example application
cd roya-examples/hello-world
./mvnw exec:java
```

---

## Code Style

### Java Style Guide

We follow **Google Java Style Guide** with minor modifications:

- **Indentation**: 4 spaces (not 2)
- **Line length**: 120 characters (not 100)
- **Braces**: Always use braces, even for single-line blocks

### Key Principles

1. **Use modern Java features**
   ```java
   // Good: Records for DTOs
   record User(int id, String name, String email) {}
   
   // Bad: Traditional class with boilerplate
   public class User {
       private final int id;
       // ... getters, setters, equals, hashCode, toString
   }
   ```

2. **Use var for local variables** (when type is obvious)
   ```java
   // Good
   var users = db.query("SELECT * FROM users").list(User.class);
   
   // Also good (when clarity needed)
   List<User> users = db.query("SELECT * FROM users").list(User.class);
   ```

3. **Functional style preferred**
   ```java
   // Good
   var activeUsers = users.stream()
       .filter(User::isActive)
       .toList();
   
   // Avoid (unless performance-critical)
   var activeUsers = new ArrayList<User>();
   for (var user : users) {
       if (user.isActive()) {
           activeUsers.add(user);
       }
   }
   ```

4. **Immutability by default**
   ```java
   // Good
   record Config(String host, int port) {}
   
   // Avoid
   class Config {
       private String host;
       private int port;
       public void setHost(String host) { ... }
   }
   ```

### Express Compatibility

When implementing Express-compatible APIs, **match the naming exactly**:

```java
// Good - matches Express
app.get("/users/:id", (req, res, next) -> {
    res.json(user);
});

// Bad - deviates from Express
app.route(Method.GET, "/users/:id", (request, response) -> {
    response.sendJson(user);
});
```

---

## Submitting Changes

### Workflow

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write code
   - Add tests
   - Update documentation

3. **Test thoroughly**
   ```bash
   ./mvnw test
   ./mvnw verify
   ```

4. **Commit with meaningful messages**
   ```bash
   git commit -m "feat: add regex path matching support
   
   - Implement PatternPathMatcher
   - Support Express-style patterns (/ab?cd, /ab+cd)
   - Add tests for character classes
   
   Closes #123"
   ```

5. **Push and create PR**
   ```bash
   git push origin feature/your-feature-name
   ```
   
   Then create a Pull Request on GitHub.

### Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process, tooling, dependencies

**Examples:**
```
feat(routing): add support for regex paths

Implements PathMatcher interface with support for:
- Static paths (/users)
- Parameterized paths (/users/:id)
- Pattern paths (/ab?cd)
- Full regex (Pattern.compile("..."))

Closes #42
```

```
fix(middleware): prevent duplicate header writes

Middleware was calling res.header() multiple times for
the same header, causing duplicate header warnings.

Fixes #58
```

### Pull Request Guidelines

**Before submitting:**
- [ ] Code compiles without warnings
- [ ] All tests pass
- [ ] New features have tests
- [ ] Documentation is updated
- [ ] Follows code style guidelines
- [ ] Commit messages follow convention

**PR Title:**
Use the same format as commit messages: `feat: add feature name`

**PR Description:**
```markdown
## What does this PR do?
Brief description of the change.

## Why is this change needed?
Context and motivation.

## How was this tested?
Description of testing done.

## Checklist
- [ ] Tests pass
- [ ] Documentation updated
- [ ] No breaking changes (or clearly documented)
```

---

## Areas We Need Help

### High Priority (Phase 1)

- [ ] **Architecture feedback** - Review design documents
- [ ] **Express API compatibility** - Verify we're matching Express correctly
- [ ] **Performance benchmarking** - Design benchmark suite
- [ ] **Documentation** - Improve READMEs, guides, examples

### Core Development (Phase 2)

- [ ] **HTTP Server** - Helidon Níma integration
- [ ] **Routing** - Path matching, regex support
- [ ] **Middleware Pipeline** - Chain execution, error handling
- [ ] **Request/Response** - API implementation
- [ ] **Testing Framework** - Unit and integration tests

### Plugin Development (Phase 2-3)

- [ ] **Database Plugin** - JOOQ integration, connection pooling
- [ ] **AI Plugin** - OpenAI client, streaming, structured output
- [ ] **Vector Store** - Qdrant, Pinecone, embedded options
- [ ] **Auth Plugin** - JWT, OAuth2, session management
- [ ] **Observability** - Metrics, logs, traces

### Tooling (Phase 3)

- [ ] **CLI Tool** - `roya new`, `roya dev`, `roya build`
- [ ] **Migration Tool** - Express → Roya AST transformation
- [ ] **IDE Plugins** - IntelliJ, VS Code
- [ ] **GraalVM Native** - Native compilation support

### Documentation (Ongoing)

- [ ] **Tutorials** - Step-by-step guides
- [ ] **API Reference** - Javadoc-based docs site
- [ ] **Examples** - Real-world applications
- [ ] **Video Content** - YouTube tutorials

---

## Community

### Communication Channels

- **GitHub Discussions** - Questions, ideas, feedback
- **Discord** (Coming soon) - Real-time chat
- **Twitter** - [@RoyaFramework](https://twitter.com/RoyaFramework) (Coming soon)

### Getting Help

**Stuck? Have questions?**

1. Check [ARCHITECTURE.md](ARCHITECTURE.md) first
2. Search [existing issues](https://github.com/yourusername/roya-framework/issues)
3. Ask in [GitHub Discussions](https://github.com/yourusername/roya-framework/discussions)
4. Tag maintainers in your issue/PR if urgent

### Code of Conduct

**Be respectful. Be kind. Be collaborative.**

- Respect differing viewpoints and experiences
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards other community members

We have zero tolerance for harassment, discrimination, or abusive behavior.

---

## Recognition

Contributors will be recognized in:

- **README.md** - Contributors section
- **Release notes** - Thank you mentions
- **Docs site** - Contributors page

Significant contributors may be invited to join the core team.

---

## Development Roadmap

See [WHITEPAPER.md](WHITEPAPER.md) for detailed roadmap.

**Current Phase**: Design & Architecture (Q1 2025)

**Next Milestone**: Prototype (Q1 2025)
- HTTP server working
- Express-compatible routing
- Basic middleware pipeline
- Performance benchmarks

---

## Questions?

Don't hesitate to ask! We're building this together.

**Thank you for contributing to Roya!** 🚀

---

*Last updated: January 2025*
