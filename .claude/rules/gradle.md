---
paths:
  - "build-logic/**"
  - "**/*.gradle.kts"
  - "gradle/**"
  - "gradle.properties"
---
# Imorn – Gradle-bygget

Del av projektets instruktioner (`CLAUDE.md`). Claude Code läser in filen när du arbetar med filerna ovan;
läs den själv innan du planerar en ändring här. Allt i `CLAUDE.md` gäller också.

## Regler
- Varje modul använder convention plugins: `imorn.android.application` (bara `:app`), `imorn.android.feature`
  (`:feature:*`), `imorn.android.library` eller `imorn.jvm.library` (ren Kotlin), plus `imorn.android.compose` för
  Compose. Tester: `imorn.android.unittest` (JUnit, Robolectric, Compose UI-tester, Kover) och
  `imorn.android.screenshot` (Roborazzi, varje `@Preview` i modulen blir ett skärmdumpstest).
  SDK-nivåer, Java-nivå, namespace, signering, lint och testuppsättning sätts bara i `build-logic`.
- Modulens `build.gradle.kts` innehåller bara `plugins`, `imorn { }` och `dependencies`. Bibliotek som flera moduler
  använder hämtas via `imorn { coroutines(); lifecycle(); … }` (`ImornExtension`); lägg till en funktion där hellre
  än att kopiera beroenden. Metro: `imorn { metro() }` för moduler med `@Inject`-klasser, `imorn { viewModels() }`
  för moduler med ViewModels. Lägg aldrig till Metro-pluginet eller `metrox`-artefakter direkt i en modul.
- Logik utan Android blir en `imorn.jvm.library`-modul. Den kompilerar och testar utan resurser och Robolectric.
- Isolated Projects är påslaget: aldrig `allprojects {}`, `subprojects {}`, `rootProject.<…>` eller `project(":x")`
  för att läsa eller ändra en annan modul; beroenden deklareras med `projects.<…>`. Behövs en fil i roten, använd
  `project.isolated.rootProject.projectDirectory`. `ksp.project.isolation.enabled=true` är satt för Room (Fas 1);
  ta aldrig bort den. Sätt aldrig `workers.max` till 2 eller lägre (gradle#39040, bygget fryser).
- Nya moduler: `include(...)` i `settings.gradle.kts`, beroende via `projects.<…>`.
- Nya Gradle-plugins deklareras med `apply false` i rotens `build.gradle.kts`.

## Kända fallgropar
- **Kotlin-version i AGP 9:** lägg aldrig till `org.jetbrains.kotlin.android`, inte heller med `apply false`.
- **Providers från tasks:** ge tasks vanliga värden (strängar) för sådant som bara är text, aldrig ett värde härlett
  ur en `TaskProvider`; det ger ett dolt beroende på den tasken.
- **Beroenden som bara debug får:** deklarera varje bibliotek en modul importerar direkt i modulen, även om
  debug-bygget får det transitivt.
- **Spotless och Isolated Projects:** ge Spotless filträd (`fileTree(...)`) som `target`, aldrig glob-strängar.
