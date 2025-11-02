# Load Testing Strategy for Roya Framework

**Status**: Planning Phase  
**Last Updated**: November 2025

---

## Executive Summary

This document outlines a comprehensive load testing strategy to validate Roya framework's performance claims against Express.js and Spring Boot. The strategy covers testing goals, methodology, infrastructure, metrics collection, and reporting.

**Key Performance Targets (from WHITEPAPER.md):**

| Metric                 | Target      | Benchmark                          |
|------------------------|-------------|------------------------------------|
| Requests/sec           | 50,000+ RPS | vs Express (5K), Spring (15K)      |
| Latency p99            | <20ms       | vs Express (250ms), Spring (100ms) |
| Memory baseline        | <50MB       | vs Express (400MB), Spring (250MB) |
| Concurrent connections | 1M+         | vs Express (1K), Spring (10K)      |
| Cold start             | <100ms      | vs Express (500ms), Spring (5s)    |

---

## 1. Testing Goals & Objectives

### 1.1 Primary Goals

1. **Validate performance claims** - Prove Roya achieves 10x RPS improvement over Express
2. **Identify bottlenecks** - Profile hot paths to guide optimization
3. **Stress testing** - Push framework to breaking points to understand limits
4. **Comparison benchmarking** - Apples-to-apples comparison with Express.js and Spring Boot
5. **Memory profiling** - Validate low memory footprint claims
6. **Startup time validation** - Measure cold start performance

### 1.2 Success Criteria

✅ **Quantitative:**
- 50,000+ RPS sustained for 60 seconds (95% success rate)
- P99 latency <20ms under 30,000 RPS load
- Memory footprint <100MB baseline, <200MB under load
- 1M+ concurrent connections without degradation
- Cold start <100ms (native) / <500ms (JVM)

✅ **Qualitative:**
- No memory leaks over 30-minute load test
- Graceful degradation under extreme load
- Clear observability of system health
- Reproducible, consistent results

---

## 2. Test Infrastructure

### 2.1 Test Environment Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Load Test Infrastructure                  │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────┐      ┌──────────────┐   ┌──────────────┐    │
│  │   k6 Cloud  │ ──── │  Prometheus  │   │   Grafana    │    │
│  │  (Load Gen) │      │  (Metrics)   │   │  (Dashboards)│    │
│  └─────────────┘      └──────────────┘   └──────────────┘    │
│         │                                                    │
│         │ HTTP                                               │
│         │                                                    │
│  ┌────────────────────────────────────────────────────────┐  │
│  │          Target Application (Docker Container)         │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │  Roya App / Express App / Spring Boot App        │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  │                                                        │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │  Application Metrics (JVM/Node.js native)        │  │  │
│  │  │  - CPU, Memory, GC pauses                        │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  System Metrics (cAdvisor / node_exporter)             │  │
│  │  - CPU cores, memory pressure, network I/O             │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### 2.2 Hardware Requirements

**Load Generator (k6):**
- **Production**: k6 Cloud (managed, scalable)
- **Local**: Dedicated machine or container
- **Resources**: 4 CPU cores, 8GB RAM (per target app)

**Target Application:**
- **Container**: 2 CPU cores, 1GB RAM
- **OS**: Linux (Ubuntu 22.04 or Alpine)
- **Network**: Isolated Docker bridge network

**Metrics Collection:**
- **Prometheus**: 2 CPU cores, 4GB RAM, 50GB SSD
- **Grafana**: 1 CPU core, 2GB RAM

**Total Infrastructure:**
- **Minimum**: 8 CPU cores, 16GB RAM
- **Recommended**: 16 CPU cores, 32GB RAM

### 2.3 Software Stack

**Load Testing:**
- **k6** v0.48+ (Grafana Labs) - Primary load testing tool
- **k6 Cloud** - Managed, scalable load generation
- **k6 extensions**: JSON-RPC, gRPC (optional)

**Metrics & Observability:**
- **Prometheus** v2.45+ - Time-series metrics
- **Grafana** v10+ - Visualization dashboards
- **Jaeger** v1.50+ - Distributed tracing (optional)

**Application Profiling:**
- **JVM**: AsyncProfiler, JFR (Java Flight Recorder)
- **Node.js**: Clinic.js, clinic flame (Express)
- **System**: perf (Linux), dtrace (macOS/Linux)

**Container Orchestration:**
- **Docker** v24+ with Docker Compose
- **Kubernetes** v1.28+ (optional, for scale testing)

---

## 3. Test Scenarios & Workloads

### 3.1 Baseline Performance Tests

#### Test 1.1: Simple GET Request
**Purpose**: Measure raw HTTP throughput without business logic

```javascript
// k6 script
export default function() {
  http.get('http://localhost:3101/api/health');
}
```

**Parameters:**
- **Duration**: 60 seconds
- **VUs**: 100, 500, 1,000, 2,500, 5,000, 10,000 (ramp-up)
- **Expected**: 50K+ RPS sustained

**Metrics:**
- RPS (requests per second)
- P50, P95, P99, P99.9 latency
- Error rate
- CPU utilization
- Memory usage

#### Test 1.2: JSON Serialization
**Purpose**: Measure JSON handling (common in APIs)

```javascript
export default function() {
  let response = http.get('http://localhost:3101/api/owners');
  check(response, { 'status 200': r => r.status === 200 });
  check(response, { 'is json': r => r.json() !== undefined });
}
```

**Parameters:**
- **Payload**: 1KB, 10KB, 100KB JSON responses
- **Duration**: 60 seconds
- **VUs**: 500

**Metrics:**
- Throughput per payload size
- Deserialization overhead

#### Test 1.3: POST with Body Parsing
**Purpose**: Measure request parsing performance

```javascript
export default function() {
  let payload = JSON.stringify({ name: 'Test Owner', email: 'test@example.com' });
  let params = { headers: { 'Content-Type': 'application/json' } };
  http.post('http://localhost:3101/api/owners', payload, params);
}
```

**Parameters:**
- **Payload**: 1KB, 10KB, 100KB JSON requests
- **Duration**: 60 seconds
- **VUs**: 500

**Metrics:**
- Request parsing throughput
- Latency breakdown (network vs processing)

### 3.2 Advanced Feature Tests

#### Test 2.1: Middleware Chain
**Purpose**: Measure middleware overhead

**Setup:**
- 5, 10, 15, 20 middleware in chain
- Each middleware: logging, CORS, JSON parsing, etc.

**Metrics:**
- Throughput degradation with chain length
- Latency impact per middleware

#### Test 2.2: Path Parameter Extraction
**Purpose**: Measure router performance

```javascript
export default function() {
  let id = Math.floor(Math.random() * 10000);
  http.get(`http://localhost:3101/api/owners/${id}`);
}
```

**Parameters:**
- **Routes**: 10, 100, 1,000 registered routes
- **Duration**: 60 seconds
- **VUs**: 1,000

**Metrics:**
- Routing overhead
- Path parameter extraction cost

#### Test 2.3: Concurrent Connections
**Purpose**: Validate virtual thread scaling

**Setup:**
- Start 10,000 slow-responding connections (10s delay)
- Then inject 1,000 high-frequency requests
- Measure impact of concurrent connections

**Metrics:**
- Memory per connection
- CPU utilization with high concurrency
- Throughput degradation

### 3.3 Stress & Endurance Tests

#### Test 3.1: Sustained Load
**Purpose**: Validate long-term stability

**Parameters:**
- **Duration**: 30 minutes
- **RPS**: 30,000 (80% of target)
- **Measurements**: Memory leaks, GC pauses

**Metrics:**
- Memory growth over time
- GC frequency and pause times
- Error rate trends

#### Test 3.2: Spike Test
**Purpose**: Measure responsiveness to sudden load

**Parameters:**
- **Baseline**: 1,000 RPS for 60s
- **Spike**: 50,000 RPS for 10s
- **Return**: 1,000 RPS for 60s

**Metrics:**
- Recovery time after spike
- Error rate during spike
- Latency degradation

#### Test 3.3: Breaking Point
**Purpose**: Find absolute limits

**Parameters:**
- **Strategy**: Gradually increase VUs until failure
- **Stop when**: >5% error rate or >1s p99 latency

**Metrics:**
- Maximum sustainable RPS
- Failure mode (OOM, CPU saturation, etc.)

### 3.4 Application-Specific Tests

#### Test 4.1: PetClinic Full API
**Purpose**: Real-world application benchmark

**Workload:**
```
80% GET /owners          (read-heavy)
15% POST /owners         (writes)
 5% GET /owners/:id      (lookup)
```

**Parameters:**
- **Duration**: 300 seconds
- **RPS**: 10,000 sustained

**Metrics:**
- Overall application throughput
- End-to-end latency
- Database interaction overhead (if applicable)

#### Test 4.2: AI Plugin Integration
**Purpose**: Measure AI plugin overhead

**Setup:**
- 50% simple GET requests
- 50% requests using AI plugin with caching

**Metrics:**
- Cache hit rate
- AI request latency
- Memory footprint with AI plugin

---

## 4. Metrics Collection & KPIs

### 4.1 Key Performance Indicators (KPIs)

#### Throughput Metrics
- **RPS (Requests Per Second)**: Target 50,000+
- **PPS (Pixels Per Second)**: For WebSocket/SSE tests
- **BPS (Bytes Per Second)**: Data transfer rate

#### Latency Metrics
- **Min**: Best-case latency
- **P50 (Median)**: Typical user experience
- **P95**: 95th percentile (alerting threshold)
- **P99**: 99th percentile (SLA threshold)
- **P99.9**: Worst-case outliers
- **Max**: Worst possible latency

#### Resource Metrics
- **CPU**: Average and peak utilization
- **Memory**: Baseline, peak, growth over time
- **GC Pauses**: Frequency, duration
- **Network I/O**: Bandwidth utilization

#### Quality Metrics
- **Error Rate**: Should be <1%
- **Success Rate**: Should be >99%
- **Timeout Rate**: Should be <0.1%

### 4.2 Observability Implementation

#### Prometheus Metrics
```javascript
// In Roya app
const prometheus = require('prom-client');

const httpDuration = new prometheus.Histogram({
  name: 'http_request_duration_seconds',
  help: 'Duration of HTTP requests in seconds',
  labelNames: ['method', 'route', 'status']
});

const httpRequests = new prometheus.Counter({
  name: 'http_requests_total',
  help: 'Total number of HTTP requests',
  labelNames: ['method', 'route', 'status']
});
```

#### Custom Metrics
- **Middleware execution time** (per middleware)
- **Database query latency** (if applicable)
- **JSON serialization time**
- **Route matching overhead**

#### Distributed Tracing
- **Request tracing**: Full request lifecycle
- **Middleware spans**: Time in each middleware
- **External call spans**: Database, AI API calls

### 4.3 Dashboard Design

**Grafana Dashboard Panels:**

1. **Overview Panel**
   - Current RPS (gauge)
   - Success rate (gauge)
   - Active VUs (line)

2. **Throughput Panel**
   - RPS over time (line)
   - Requests total (counter)
   - Data transfer rate (area)

3. **Latency Panel**
   - P50, P95, P99, P99.9 (line)
   - Latency heatmap (heatmap)
   - Latency distribution (histogram)

4. **Resource Panel**
   - CPU utilization (gauge)
   - Memory usage (gauge)
   - GC pause time (bar)

5. **Error Panel**
   - Error rate (line)
   - Error types (pie chart)
   - Failed requests (table)

6. **Comparison Panel** (Roya vs Others)
   - Side-by-side latency (bar)
   - Throughput comparison (bar)
   - Memory comparison (bar)

---

## 5. Implementation Roadmap

### Phase 1: Foundation (Week 1)
- [ ] Set up Docker Compose environment
- [ ] Configure Prometheus + Grafana
- [ ] Create baseline k6 test scripts
- [ ] Instrument Roya app with Prometheus
- [ ] Build first comparison app (Express.js)

### Phase 2: Baseline Tests (Week 2)
- [ ] Run simple GET request tests
- [ ] Validate infrastructure stability
- [ ] Create initial Grafana dashboards
- [ ] Document test procedures
- [ ] Share preliminary results

### Phase 3: Advanced Tests (Week 3)
- [ ] Implement middleware chain tests
- [ ] Create stress test scenarios
- [ ] Add distributed tracing
- [ ] Profile hot paths
- [ ] Identify first optimization targets

### Phase 4: Optimization & Validation (Week 4)
- [ ] Optimize identified bottlenecks
- [ ] Re-run full test suite
- [ ] Validate performance targets
- [ ] Generate comprehensive report
- [ ] Create video walkthrough

### Phase 5: Publication (Week 5)
- [ ] Write technical blog post
- [ ] Create infographic for results
- [ ] Publish raw data (JSON, CSV)
- [ ] Share Grafana dashboard exports
- [ ] Submit findings to performance communities

---

## 6. Test Procedures

### 6.1 Pre-Test Checklist

- [ ] All containers running and healthy
- [ ] Prometheus scraping metrics
- [ ] Grafana dashboards loaded
- [ ] k6 test scripts validated
- [ ] Baseline measurements collected
- [ ] System monitoring in place

### 6.2 Test Execution Flow

1. **Warm-up**: Run 10% of target load for 60 seconds
2. **Ramp-up**: Gradually increase load to target over 120 seconds
3. **Sustain**: Hold target load for 300 seconds
4. **Ramp-down**: Gradually decrease load over 60 seconds
5. **Cool-down**: Allow 60 seconds for metrics stabilization

### 6.3 Post-Test Analysis

1. **Data Validation**
   - Verify all metrics collected
   - Check for outliers or anomalies
   - Validate test integrity

2. **Statistical Analysis**
   - Calculate mean, median, percentiles
   - Identify trends and patterns
   - Compare against targets

3. **Reporting**
   - Generate summary report
   - Create visualizations
   - Document anomalies

---

## 7. Comparative Testing Strategy

### 7.1 Framework Selection

**Primary Comparisons:**
1. **Express.js** (Node.js) - Original target
2. **Spring Boot** (Java) - Java ecosystem benchmark
3. **Helidon SE** (Java) - Underlying framework baseline

**Optional Comparisons:**
4. **Fastify** (Node.js) - High-performance alternative
5. **Quarkus** (Java) - Native-first framework

### 7.2 Fair Comparison Criteria

**Environment Parity:**
- Same Docker container (JVM vs Node.js versions match)
- Same CPU cores and memory allocation
- Same network configuration
- Same OS and kernel version

**Application Parity:**
- Identical API endpoints
- Same request/response payloads
- Same middleware stack (where possible)
- Same database (if applicable)

**Test Parity:**
- Same k6 test scripts
- Same load profile and duration
- Same metrics collection method
- Same analysis methodology

### 7.3 Comparison Matrix

| Feature | Express | Spring Boot | Helidon SE | Roya |
|---------|---------|-------------|------------|------|
| Language | JavaScript | Java | Java | Java |
| Runtime | Node.js | JVM | JVM | JVM |
| Concurrency | Event Loop | Thread Pool | Virtual Threads | Virtual Threads |
| Memory Model | Single Heap | Heap + Metaspace | Heap + Metaspace | Heap + Metaspace |

---

## 8. Expected Challenges & Solutions

### 8.1 Infrastructure Challenges

**Challenge**: Network bottleneck  
**Solution**: Use local Docker network, increase network MTU

**Challenge**: Container resource limits  
**Solution**: Run on dedicated host, allocate generous resources

**Challenge**: Metrics collection overhead  
**Solution**: Sample metrics, use efficient exporters

### 8.2 Application Challenges

**Challenge**: JVM warm-up affecting results  
**Solution**: Run 5-minute warm-up before tests, use JIT compilation

**Challenge**: GC pauses skewing latency  
**Solution**: Use low-latency GC (ZGC), measure GC impact separately

**Challenge**: Non-deterministic results  
**Solution**: Run 5+ iterations, calculate confidence intervals

### 8.3 k6/Test Challenges

**Challenge**: k6 hitting local resource limits  
**Solution**: Use k6 Cloud for large-scale tests

**Challenge**: Test script complexity  
**Solution**: Modularize scripts, use k6 templates

**Challenge**: Results reproducibility  
**Solution**: Containerize everything, version control all configs

---

## 9. Documentation & Reporting

### 9.1 Deliverables

1. **Test Execution Report**
   - Summary of all test runs
   - Raw metrics data (CSV/JSON)
   - Configuration files used

2. **Performance Analysis Report**
   - Statistical analysis
   - Trend identification
   - Bottleneck analysis

3. **Comparison Report**
   - Side-by-side framework comparison
   - Visual charts and graphs
   - Recommendation summary

4. **Technical Blog Post**
   - Methodology explanation
   - Key findings
   - Lessons learned

5. **Public Dataset**
   - All raw metrics exported
   - Grafana dashboard definitions
   - k6 test scripts

### 9.2 Report Template

```markdown
# Roya Framework Performance Benchmarks
**Date**: [Date]  
**Version**: [Roya version]  
**Infrastructure**: [Docker/K8s details]

## Executive Summary
[2-3 paragraph overview of results]

## Methodology
[Test setup, parameters, comparison criteria]

## Results

### Throughput
[Charts and analysis]

### Latency
[Charts and analysis]

### Resource Utilization
[Charts and analysis]

## Comparison with Competitors
[Side-by-side comparison]

## Conclusions
[Key takeaways and recommendations]

## Raw Data
[Links to datasets]
```

---

## 10. Success Definition

### 10.1 Quantitative Targets

✅ **Throughput**: 50,000+ RPS sustained  
✅ **Latency P99**: <20ms under 30K RPS  
✅ **Memory**: <100MB baseline, <200MB loaded  
✅ **Concurrency**: 1M+ connections handled  
✅ **Cold Start**: <100ms (native) / <500ms (JVM)

### 10.2 Qualitative Targets

✅ **Reproducibility**: Results within 5% across runs  
✅ **Stability**: No crashes, leaks, or degradation over 30 min  
✅ **Observability**: All metrics captured and visualized  
✅ **Documentation**: Complete, clear, actionable reports

---

## 11. Tools & Scripts

### 11.1 Directory Structure

```
roya-benchmarks/
├── README.md                           # This file
├── docker-compose.yml                  # Infrastructure setup
├── Makefile                            # Common commands
├── k6/
│   ├── simple-get.js                   # Basic GET test
│   ├── json-serialization.js          # JSON handling
│   ├── middleware-chain.js            # Middleware overhead
│   ├── concurrent-connections.js      # Concurrency test
│   ├── sustained-load.js              # 30-min endurance
│   └── spike-test.js                  # Load spike
├── apps/
│   ├── roya-app/                      # Roya implementation
│   ├── express-app/                   # Express.js implementation
│   ├── spring-app/                    # Spring Boot implementation
│   └── helidon-app/                   # Helidon SE baseline
├── prometheus/
│   └── prometheus.yml                 # Prometheus config
├── grafana/
│   └── dashboards/                    # Dashboard definitions
└── results/
    ├── raw/                           # CSV/JSON data
    ├── reports/                       # Generated reports
    └── analysis/                      # Statistical analysis
```

### 11.2 Key Commands

```bash
# Start entire infrastructure
make start

# Run all tests
make test-all

# Run specific test
make test TEST=simple-get

# Generate report
make report

# Clean up
make clean
```

---

## 12. Continuous Integration

### 12.1 CI/CD Integration

**GitHub Actions Workflow:**
- Nightly performance regression tests
- Automated benchmark reports
- Alert on >10% degradation

**Jenkins Pipeline:**
- Continuous performance monitoring
- Automated comparison with baselines
- Performance trend analysis

### 12.2 Performance Budgets

Set and enforce performance budgets:
- **RPS**: Must maintain 50K+ RPS
- **P99 Latency**: Must stay <20ms
- **Memory**: Must not exceed 200MB loaded

---

## Appendix

### A. References
- [k6 Documentation](https://k6.io/docs/)
- [Prometheus Best Practices](https://prometheus.io/docs/practices/)
- [GraalVM Native Image Performance](https://www.graalvm.org/latest/reference-manual/native-image/performance/)
- [Helidon Performance Tuning](https://helidon.io/docs/v4/guides/performance-tuning)

### B. Glossary
- **RPS**: Requests Per Second
- **VU**: Virtual User (k6 terminology)
- **P99**: 99th percentile latency
- **GC**: Garbage Collection
- **JFR**: Java Flight Recorder

### C. Contacts
- **Primary**: [TBD]
- **Infrastructure**: [TBD]
- **Results**: [TBD]

---

**Document Status**: Draft  
**Next Review**: After Phase 1 completion  
**Last Updated**: November 2, 2025

