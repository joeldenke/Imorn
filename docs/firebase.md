# Firebase

Imorn is wired for Firebase at the repo/config level. Nothing Firebase-specific is committed: the config file is
per Firebase project, so it lives outside git and is supplied locally (and to CI through a secret). The Firebase
project itself is created by hand in the console — this document is the exact set of steps. No service SDK is
declared yet (see [Which SDKs](#which-sdks) below).

## What is already wired in the repo

- **`gradle/libs.versions.toml`** — `googleServices = "4.5.0"` and `firebaseBom = "35.0.2"`, the
  `com.google.gms.google-services` plugin, and the `firebase-bom` platform library.
- **Root `build.gradle.kts`** — the google-services plugin on the classpath (`apply false`).
- **`app/build.gradle.kts`** — the plugin is applied **only when `app/google-services.json` exists**, so a fresh
  clone, a fork, or CI before the secret is set still builds (without Firebase). It also has
  `implementation(platform(libs.firebase.bom))`.
- **`.gitignore`** — `google-services.json` is ignored. `app/google-services.example.json` is a clearly-named
  placeholder (never used by the build).
- **`.github/workflows/ci.yml` + `.github/actions/setup-gradle-build`** — if the `GOOGLE_SERVICES_JSON` secret is
  set, it is base64-decoded into `app/google-services.json` before Gradle runs.
- **`.github/workflows/distribute.yml` + `scripts/release-notes.sh`** — Firebase App Distribution to testers on
  every push to `main` (port of Novila's workflow). See [App Distribution](#app-distribution).

## Package names Firebase must know

`app`'s `applicationId` is `se.imorn`; the debug build type adds a suffix in
`build-logic/.../AndroidApplicationConventionPlugin.kt`, so the two applicationIds are:

| Build   | applicationId    | Used by |
| ------- | ---------------- | ------- |
| release | `se.imorn`       | Play / signed release |
| debug   | `se.imorn.debug` | Firebase App Distribution, local debug |

The google-services plugin matches a client in `google-services.json` by `package_name`. **Register both** in the
Firebase project and download a single config that contains both clients; registering only one makes the plugin
fail for the other variant ("No matching client found for package name …"). `scripts/bootstrap-firebase.sh`
registers both for you, and `app/google-services.example.json` shows the two-client shape.

## Manual steps (Firebase console)

1. **Create the project.** <https://console.firebase.google.com> → *Add project*. Name it (e.g. `Imorn`); Google
   Analytics is optional.
2. **Add the release Android app.** Project settings → *Your apps* → Android → package name `se.imorn`, a nickname
   (e.g. *Imorn*). *(Skip the downloaded file for now if you will also add the debug app — download once after
   both exist.)*
3. **Add the debug Android app.** Same screen → Android → package name `se.imorn.debug`, nickname *Imorn (debug)*.
4. **Download `google-services.json`** (Project settings → *Your apps* → *Download google-services.json*). It must
   contain a `client` entry for **both** `se.imorn` and `se.imorn.debug`.
5. **Place it locally** at `app/google-services.json` (the exact path the plugin and `.gitignore` expect). Do not
   commit it.
6. **Give it to CI** (optional, so CI builds the Firebase-configured app):
   - Encode it: `base64 -w0 app/google-services.json` (macOS: `base64 app/google-services.json`).
   - Repo → Settings → Secrets and variables → Actions → *New repository secret* named `GOOGLE_SERVICES_JSON`,
     value = the base64 string. Without this secret CI simply skips Firebase.

That is all that is needed for the config to activate. `./gradlew :app:assembleDebug` now runs the google-services
plugin and generates the Firebase resources.

### Alternative: bootstrap the Google side by script

`scripts/bootstrap-firebase.sh` creates the GCP project, enables the APIs, adds Firebase, registers **both**
Android apps, creates the tester group, a least-privilege service account and Workload Identity Federation, then
sets the Actions **Variables** below. It does **not** download `google-services.json` — do step 4/5 above after it.
Run it from Cloud Shell:

```bash
./scripts/bootstrap-firebase.sh <project-id> [tester-email,...]
```

## CI secrets and variables

| Name | Kind | Required for | Purpose |
| ---- | ---- | ------------ | ------- |
| `GOOGLE_SERVICES_JSON` | Secret | `ci.yml` (optional) | base64 of `app/google-services.json`; decoded to `app/google-services.json` before Gradle |
| `FIREBASE_APP_ID` | Variable | `distribute.yml` | App ID of `se.imorn.debug`, e.g. `1:123…:android:abc…` |
| `FIREBASE_TESTER_GROUPS` | Variable | `distribute.yml` | App Distribution group alias(es), e.g. `testers` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Variable | `distribute.yml` | `projects/<number>/locations/global/workloadIdentityPools/github/providers/imorn` |
| `GCP_SERVICE_ACCOUNT` | Variable | `distribute.yml` | `github-distribute@<project>.iam.gserviceaccount.com` |
| `GRADLE_ENCRYPTION_KEY` | Secret | CI (optional) | `openssl rand -base64 16`; encrypts the Gradle cache |
| `IMORN_RELEASE_KEYSTORE_BASE64` | Secret | `distribute.yml` (optional) | base64 of the release `.jks`; signs the release APK |
| `IMORN_RELEASE_STORE_PASSWORD` | Secret | `distribute.yml` (optional) | release keystore password |
| `IMORN_RELEASE_KEY_PASSWORD` | Secret | `distribute.yml` (optional) | release key password |
| `IMORN_RELEASE_KEY_ALIAS` | Variable | `distribute.yml` (optional) | release key alias, e.g. `imorn` |

## App Distribution

`.github/workflows/distribute.yml` builds the debug and release APKs on every push to `main` and uploads the debug
one to Firebase App Distribution testers. It authenticates with Workload Identity Federation (no JSON key), and
uses the `firebase` GitHub environment. It needs the four `distribute.yml` variables above; without them the job
says which are missing. The release build is signed only when all four `IMORN_RELEASE_*` values are set.

## Which SDKs

No Firebase service SDK is declared yet: the ported setup (Novila) uses Firebase only for App Distribution, which
needs no in-app SDK, and Imorn deliberately strips the `INTERNET` permission to keep Gemini Nano and data
on-device. When a service is wanted, add it to the BoM in `app/build.gradle.kts` with no version, e.g.:

```kotlin
implementation(libs.firebase.analytics)
```

and add the matching library to `gradle/libs.versions.toml`:

```toml
firebase-analytics = { module = "com.google.firebase:firebase-analytics" }
```

That will add the service's manifest entries; run `:app:assembleDebug :app:checkDebugBadging` and, if intended,
record the change with the `/record-badging` PR comment (see `app/badging/debug-badging.txt`).
