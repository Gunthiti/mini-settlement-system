# mini-settlement-system

A learning project for CI/CD, Docker, Kubernetes and GitOps, from a Java/Spring developer's
point of view. Three Spring Boot services share one Postgres database:

| service              | role                                                              |
|-----------------------|-------------------------------------------------------------------|
| `transaction-api`     | `POST /api/transactions` — accepts a transaction, stores it as `PENDING` |
| `settlement-worker`   | batch job — settles `PENDING` transactions (scheduled locally, one-shot `CronJob` in K8s) |
| `report-service`      | `GET /api/reports/daily` — daily settlement summary, grouped by currency |

## Layout

```
services/
  transaction-api/     Maven project + Dockerfile
  settlement-worker/    Maven project + Dockerfile
  report-service/       Maven project + Dockerfile
db/
  init.sql              shared schema, mounted into Postgres on first boot
```

Each service is an independent Maven project (not a multi-module reactor) so that each one's
Docker build context is self-contained — see the Dockerfile comments for why that matters.

## Building a service locally

```
cd services/transaction-api
mvn clean verify        # compiles + runs tests against an in-memory H2 database
docker build -t mini-settlement/transaction-api:dev .
```

Same commands apply to `settlement-worker` and `report-service`.

## Running everything locally

```
docker compose up --build -d
docker compose ps                 # wait until all show (healthy)

curl -X POST localhost:8081/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"reference":"txn-001","amount":100.00,"currency":"THB"}'

curl localhost:8083/api/reports/daily      # after the next settlement pass (30s)

# simulate one CronJob-style invocation of settlement-worker instead of
# waiting for the always-on scheduled loop:
docker compose run --rm -e SETTLEMENT_MODE=oneshot settlement-worker

docker compose down            # add -v to also drop the postgres volume
```

## Kubernetes manifests (Kustomize)

```
k8s/
  base/               shared resources: what every environment deploys
    postgres/
    transaction-api/
    settlement-worker/
    report-service/
  overlays/
    dev/              namespace mini-settlement-dev, image tag "dev", faster CronJob schedule
    prod/              namespace mini-settlement-prod, image tag "stable", 2 replicas per API
```

Render and inspect either environment without needing a cluster:

```
kubectl kustomize k8s/overlays/dev
kubectl kustomize k8s/overlays/prod
```

Apply to a real cluster (Minikube/kind) once you have one:

```
kubectl apply -k k8s/overlays/dev
```

## GitHub Actions CI ([.github/workflows/ci.yml](.github/workflows/ci.yml))

`test` (matrix over the 3 services) -> `build-and-push` (GHCR, tag = short commit SHA) ->
`update-manifests` (bumps `k8s/overlays/dev/kustomization.yaml`, commits, pushes). It never
touches a cluster directly -- ArgoCD (Phase 5) is what acts on that commit.

One-time repo setup once this is pushed to GitHub:

- **Settings > Actions > General > Workflow permissions** -> "Read and write permissions"
  (needed for `update-manifests` to push a commit, and for pushing to GHCR)
- After the first successful run, the 3 packages appear under the repo owner's
  **Packages** tab as *private* by default -- either make them public, or set up an
  `imagePullSecret` if a real cluster needs to pull them over the network.

## ArgoCD (GitOps) on a local kind cluster

```
kind create cluster --config k8s/kind-cluster.yaml
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml --server-side --force-conflicts

kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d
kubectl port-forward svc/argocd-server -n argocd 8080:443   # UI: https://localhost:8080, CLI: argocd login localhost:8080

kubectl apply -f k8s/argocd/application-dev.yaml   # the Application CRD that watches k8s/overlays/dev
```

`k8s/argocd/application-dev.yaml` has `syncPolicy.automated` with `selfHeal: true` and
`prune: true` -- ArgoCD polls this repo's `main` branch, applies new commits on its own, and
reverts any manual `kubectl edit`/`scale` drift back to what git says.

**Rollback**: `argocd app rollback <app> <history-id>` only works while auto-sync is *off*
(`argocd app set <app> --sync-policy none`) -- ArgoCD refuses otherwise
(`FailedPrecondition: rollback cannot be initiated when auto-sync is enabled`). Even then, a
CLI rollback only fixes the *cluster*; git still points at the bad commit, so the next sync
(or self-heal) reapplies it. The actual GitOps-correct rollback is `git revert` + push, then
re-enable auto-sync (`argocd app set <app> --sync-policy automated --self-heal --auto-prune`).

## Status

- [x] Phase 1 — project structure + multi-stage Dockerfiles
- [x] Phase 2 — docker-compose for local dev
- [x] Phase 3 — Kubernetes manifests (Kustomize base/overlays)
- [x] Phase 4 — GitHub Actions CI
- [x] Phase 5 — ArgoCD (GitOps)
