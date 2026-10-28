# CI

Three workflows in `.github/workflows/`, sharing the steps in `.github/actions/`. The workflows say *how* in their
comments; this page says *why*.

| Workflow | Runs on | Concurrency |
| -------- | ------- | ----------- |
| `CI` (`ci.yml`) | PRs to `main`, pushes to `main`, `workflow_dispatch` | Per PR or ref; a new push cancels the PR's older run. Never cancelled on `main`. |
| `Distribute to testers` (`distribute.yml`) | Pushes to `main` (not docs only), `workflow_dispatch` | One group for everything; queued, never cancelled. |
| `Record references` (`screenshots.yml`) | `/record-*` comments, `workflow_dispatch` | Per PR or ref, on the job; queued, never cancelled. |

## Concurrency

- **Groups start with the workflow name** (`${{ github.workflow }}-…`), so runs of different workflows never
  cancel or wait for each other.
- **CI cancels superseded runs** on a PR (and on a branch started by `workflow_dispatch`): only the newest commit's
  result matters. On `main` it queues instead. Each run there saves the Gradle cache PRs restore, and every merged
  commit gets a result.
- **Distribution is never cancelled.** A cancelled run could stop in the middle of the Firebase App Distribution
  upload. GitHub keeps one run waiting per group, so if several merges land during a distribution, only the newest
  waits. That is fine: its release notes list everything since the last *successful* distribution.
- **Recording is never cancelled**, because it pushes to the branch. The group is on the job, not the workflow:
  every comment anywhere starts `Record references`, and a workflow-level group would let an ordinary comment
  replace a queued `/record-*` run.

## No duplicate runs

`CI` runs on `push` only for `main` and on `pull_request` only for PRs to `main`. A commit on a PR branch therefore
runs once (as the PR), and a merge runs once (as the push to `main`). Commits pushed by the workflows themselves
(recorded screenshots, badging, schemas) use `GITHUB_TOKEN`, which triggers nothing, so the workflows start CI on
them explicitly with `workflow_dispatch`, also once.

## Cheap reruns

- **Gradle cache:** `setup-gradle` (in `setup-gradle-build`) saves the Gradle User Home, with the build cache and,
  given `GRADLE_ENCRYPTION_KEY`, the configuration cache, only on `main`. PR runs only read it. Unchanged modules
  come `FROM-CACHE`, and a rerun of one failed job does just that job's work. No other caching:
  `setup-java`'s `cache: gradle` or `actions/cache` on `~/.gradle` would fight setup-gradle.
- **Path filters:** a PR that only touches docs (`docs/`, `*.md`) skips the three Gradle jobs. The `Changes` job
  decides, and *Lint, test and build* still reports success. A workflow-level `paths` filter would leave that required
  check pending forever. Distribution ignores docs-only merges with `paths-ignore`, because nothing in the APK
  changed. Everything else counts as code: `content/` is the app's assets, and build files, workflows and scripts
  change the build.
- **Timeouts:** every job has `timeout-minutes` (30 for Gradle jobs, 5 for the small ones), so a hung build never
  burns six hours of runner time.

## Runners: GitHub-hosted only

Imorn is a **public repository**, so anyone can open a PR from a fork, and that PR's code runs in CI. On a
self-hosted runner it would run on our own machine, which can keep state, credentials and network access between
jobs. GitHub advises against self-hosted runners on public repositories for exactly this reason. GitHub-hosted
runners are a fresh VM per job and are free for public repositories.

Novila (private) may use an opt-in self-hosted Hive runner. Imorn doesn't, and shouldn't get one while it is public.
