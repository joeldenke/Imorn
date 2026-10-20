# Firebase

Imorn uses Firebase only for **App Distribution**: every push to `main` builds the debug APK and sends it to testers.
There are **no JSON files** anywhere — no `google-services.json` and no service account key. GitHub Actions signs in
to Google Cloud with **Workload Identity Federation** and gets short-lived tokens; nothing long-lived is stored.

## The project

| What | Value |
| ---- | ----- |
| GCP / Firebase project | `imorn-app` (number `258082285405`) |
| Android app `se.imorn` (release) | `1:258082285405:android:25073c09b6971d529e2ab4` |
| Android app `se.imorn.debug` (debug, sent to testers) | `1:258082285405:android:ce5d5ce0da6a3aa89e2ab4` |
| App Distribution group | `testers` |
| Service account | `github-distribute@imorn-app.iam.gserviceaccount.com` — only `roles/firebaseappdistro.admin`, no keys |
| Workload Identity provider | `projects/258082285405/locations/global/workloadIdentityPools/github/providers/imorn` |

The provider only accepts GitHub OIDC tokens from `joeldenke/Imorn` (repo id `1425129397`, which survives renames)
for jobs running in the `firebase` GitHub environment, and only that repo may impersonate the service account.

All of this is created by `scripts/bootstrap-firebase.sh`, which is safe to run again (it only creates what is
missing). From Google Cloud Shell:

```bash
./scripts/bootstrap-firebase.sh imorn-app [tester-email,...]
```

## GitHub Actions configuration

Repo → Settings → Secrets and variables → Actions.

| Name | Kind | Value | Required for |
| ---- | ---- | ----- | ------------ |
| `FIREBASE_APP_ID` | Variable | `1:258082285405:android:ce5d5ce0da6a3aa89e2ab4` | `distribute.yml` |
| `FIREBASE_TESTER_GROUPS` | Variable | `testers` | `distribute.yml` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Variable | `projects/258082285405/locations/global/workloadIdentityPools/github/providers/imorn` | `distribute.yml` |
| `GCP_SERVICE_ACCOUNT` | Variable | `github-distribute@imorn-app.iam.gserviceaccount.com` | `distribute.yml` |
| `GRADLE_ENCRYPTION_KEY` | Secret | `openssl rand -base64 16` | optional; encrypts the Gradle cache |
| `IMORN_RELEASE_KEYSTORE_BASE64` | Secret | base64 of the release `.jks` | optional; signs the release APK |
| `IMORN_RELEASE_STORE_PASSWORD` | Secret | release keystore password | optional |
| `IMORN_RELEASE_KEY_PASSWORD` | Secret | release key password | optional |
| `IMORN_RELEASE_KEY_ALIAS` | Variable | release key alias, e.g. `imorn` | optional |

The four Firebase/GCP values are identifiers, not credentials, which is why they are Variables. Never add a
`FIREBASE_SERVICE_ACCOUNT` or `GOOGLE_SERVICES_JSON` secret.

## App Distribution

`.github/workflows/distribute.yml` builds the debug and release APKs on every push to `main` (or manually from the
Actions tab) and uploads the debug one to the tester groups. `google-github-actions/auth` exchanges the job's GitHub
OIDC token for short-lived credentials of the service account, which `firebase-tools` picks up. Without the four
variables the job says which are missing. The release build is signed only when all four `IMORN_RELEASE_*` values are
set.

## Which SDKs

No Firebase service SDK is declared, and Imorn deliberately strips the `INTERNET` permission to keep Gemini Nano and
data on-device. The Firebase BoM is in `app/build.gradle.kts` only to pin versions. There is deliberately no
google-services plugin: if a service SDK is ever wanted, initialise it in code with `FirebaseOptions` (app id,
project id, API key) instead of adding a `google-services.json`, then run `:app:assembleDebug :app:checkDebugBadging`
and record the badging change with the `/record-badging` PR comment.
