import { Link } from 'wouter';
import { PageHeader, SectionHeader, SubsectionHeader } from '../../components/PageHeader';
import { Seo } from '../../components/Seo';

export function ProductionGuide() {
  return (
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-12 space-y-10">
      <Seo
        title="Production Guide"
        description="Hardening checklist for running Roya in production: configuration, observability, deployment, and scaling."
      />
      <Link href="/docs/guide" class="text-roya-primary dark:text-roya-primary hover:underline inline-block mb-4">
        ← Guide
      </Link>
      <PageHeader
        title="Production Guide"
        subtitle="Prepare Roya applications for production workloads on DigitalOcean, Kubernetes, or bare metal."
        variant="gradient"
        subtitleMuted
      />

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Baseline Checklist" variant="primary" />
        <ul class="list-disc list-inside text-roya-text dark:text-roya-textDark space-y-3 leading-relaxed">
          <li>
            <strong>Environment separation:</strong> Use <code class="font-mono text-sm">SITEMAP_BASE_URL</code>, database URLs, and secrets per environment.
          </li>
          <li>
            <strong>Migrations first:</strong> Run <code class="font-mono text-sm">database.migrate()</code> before rolling out new application pods.
          </li>
          <li>
            <strong>Ready/Live probes:</strong> Helidon health endpoints (<code class="font-mono text-sm">/health</code>, <code class="font-mono text-sm">/health/live</code>, <code class="font-mono text-sm">/health/ready</code>) integrate with Kubernetes and DigitalOcean App Platform.
          </li>
          <li>
            <strong>Structured logging:</strong> Morgan JSON logging plus `logstash-logback-encoder` ensures ingest into DO Logs or ELK.
          </li>
        </ul>
      </section>

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Configuration & Secrets" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          Combine <code class="font-mono text-sm">ConfigMiddleware</code> and <code class="font-mono text-sm">SecretsMiddleware</code> to centralize precedence rules.
          Environment variables should override system properties inside container environments like DigitalOcean.
        </p>
        <pre class="bg-black text-roya-primary text-sm p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code>{`app.use(ConfigMiddleware.builder()
    .override(ConfigSources.file("conf/override.yaml").build())
    .includeSystemProperties(true)
    .includeEnvironmentVariables(true)
    .build());

app.use(SecretsMiddleware.defaults());`}</code></pre>
        <p class="text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          For DigitalOcean App Platform, define environment variables and secrets via the control panel or <code class="font-mono text-sm">doctl apps update</code>.
        </p>
      </section>

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Containerization & Deployment" />
        <SubsectionHeader title="DigitalOcean Droplet" />
        <ol class="list-decimal list-inside space-y-3 text-roya-text dark:text-roya-textDark leading-relaxed">
          <li>Build the fat JAR: <code class="font-mono text-sm">./gradlew :roya-examples-supportdesk:shadowJar</code>.</li>
          <li>Copy the JAR and <code class="font-mono text-sm">docker-compose.yml</code> to the droplet.</li>
          <li>Run <code class="font-mono text-sm">docker compose up -d postgres redis qdrant</code>.</li>
          <li>Start Roya with <code class="font-mono text-sm">java --enable-preview -jar roya-examples-supportdesk/build/libs/...-all.jar</code>.</li>
        </ol>
        <SubsectionHeader title="DigitalOcean App Platform" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          App Platform builds from Git. Add a component pointing at <code class="font-mono text-sm">roya-showcase</code> for the static site and one pointing at the Support Desk module for the API. Configure build commands:
        </p>
        <ul class="list-disc list-inside text-roya-text dark:text-roya-textDark space-y-2 leading-relaxed">
          <li><code class="font-mono text-sm">npm install && npm run build</code> in <code class="font-mono text-sm">roya-showcase/frontend</code></li>
          <li><code class="font-mono text-sm">./gradlew :roya-examples-supportdesk:shadowJar</code> at repository root</li>
        </ul>
      </section>

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Observability" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          Helidon integrates Micrometer/Prometheus out of the box. Add the metrics plugin (Phase 5 continuation) or expose Morgan logs to whichever collector you prefer.
        </p>
        <ul class="list-disc list-inside text-roya-textMuted dark:text-roya-textMutedDark space-y-2">
          <li>Enable `helidon.metrics.api` to expose `/metrics`.</li>
          <li>Forward structured logs to DigitalOcean Logs or Datadog.</li>
          <li>Export AI usage metrics via LangChain4j’s token/cost metadata.</li>
        </ul>
      </section>

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Scaling & Resilience" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          Roya scales horizontally with virtual threads. For background workloads, rely on the workflow scheduler or Helidon Scheduling.
        </p>
        <ul class="list-disc list-inside text-roya-textMuted dark:text-roya-textMutedDark space-y-2">
          <li>Use a supervised process manager (systemd, DigitalOcean App Platform) to restart on failure.</li>
          <li>Configure connection pool sizes for each environment (<code class="font-mono text-sm">db.pool.maxSize</code>).</li>
          <li>Enable retry policies with Helidon Fault Tolerance for outbound calls.</li>
          <li>Set up CDN + WAF for the docs site if exposed publicly.</li>
        </ul>
      </section>
    </div>
  );
}

