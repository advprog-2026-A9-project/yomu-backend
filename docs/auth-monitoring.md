# Auth Monitoring Setup

This service now exposes auth-related metrics through Spring Boot Actuator and Prometheus.

## Exposed endpoint

- `GET /actuator/prometheus`

The endpoint is enabled by:

- `spring-boot-starter-actuator`
- `io.micrometer:micrometer-registry-prometheus`
- `management.endpoints.web.exposure.include=health,info,prometheus`

## Auth metrics

### Counters

- `yomu_auth_operations_total`
  - Tags: `operation`, `outcome`
  - Operations: `register`, `login`, `update_account`, `delete_account`, `link_login_method`, `oauth2_callback`
- `yomu_auth_jwt_validation_total`
  - Tags: `outcome`
  - Outcomes: `missing`, `valid`, `invalid`, `error`
- `yomu_auth_events_total`
  - Tags: `event`, `outcome`
  - Example event: `logout`

### Timer

- `yomu_auth_operation_duration_seconds`
  - Tags: `operation`, `outcome`
  - Tracks duration for auth service and OAuth2 callback flows

## Prometheus scrape config

```yaml
scrape_configs:
  - job_name: yomu-backend
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - localhost:8080
```

You can also use the ready-made file at [monitoring/prometheus/prometheus.yml](../monitoring/prometheus/prometheus.yml) if Prometheus runs in Docker and the backend runs on your host.

### Alert rules

Use [monitoring/prometheus/alerts.yml](../monitoring/prometheus/alerts.yml) as the `rule_files` entry in Prometheus if you want alerts for login failures, JWT issues, and slow auth operations.

Example:

```yaml
rule_files:
  - /etc/prometheus/alerts.yml
```

### Grafana dashboard

Import [monitoring/grafana/dashboards/auth-monitoring-dashboard.json](../monitoring/grafana/dashboards/auth-monitoring-dashboard.json) into Grafana and map the Prometheus datasource when prompted.

The dashboard includes:

- login success/failure rate
- JWT validation outcomes
- p95 auth latency for login and register
- login failure ratio as a quick health signal

### Local run example

1. Start the backend.
2. Run Prometheus with the config file mounted:

```powershell
docker run --rm -p 9090:9090 `
  -v "${PWD}/monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml" `
  prom/prometheus
```

3. Open `http://localhost:9090` and check `Status -> Targets`.
4. Query auth metrics like `yomu_auth_operations_total` in the Prometheus graph UI.
5. Check `Status -> Rules` to confirm the alert group is loaded.
6. If Grafana is available, import the dashboard JSON and select your Prometheus datasource.

## Suggested alerts

### High auth failure rate

Trigger if login failures grow faster than successes over a short window.

Example idea:

```promql
sum(rate(yomu_auth_operations_total{operation="login",outcome="failure"}[5m]))
/
sum(rate(yomu_auth_operations_total{operation="login"}[5m]))
> 0.2
```

### JWT validation issues

Trigger if invalid or error JWT validations increase.

```promql
sum(rate(yomu_auth_jwt_validation_total{outcome=~"invalid|error"}[5m])) > 0
```

### Slow auth operations

Trigger if auth latency spikes for login or register.

```promql
histogram_quantile(
  0.95,
  sum(rate(yomu_auth_operation_duration_seconds_bucket{operation=~"login|register"}[5m])) by (le)
) > 1
```

## Notes

- The metrics are intentionally low-cardinality so they are safe to keep in production.
- If you want Grafana dashboards next, the current metric names are ready for panel wiring.
