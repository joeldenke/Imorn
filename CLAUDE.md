# Imorn – instruktioner för Claude

Imorn hjälper vem som helst genom livets stora projekt (flytta, renovera, få barn, ta hand om ett dödsbo) som
*kapitel* med en nu-linje: stegen som väntar ovanför, det som har hänt (avbockat, foton, papper) under. Produktvisionen
och planen finns i [docs/ROADMAP.md](docs/ROADMAP.md); mallarna i [docs/templates.md](docs/templates.md) och
innehållssystemet i [docs/content-system.md](docs/content-system.md). Läs roadmapen innan du bygger en ny del, och
bocka av punkten där i samma PR.

Ägaren utvecklar enbart från mobilen: ingen Android Studio, ingen emulator, ingen lokal adb.
CI är därför den enda sanningen om att något bygger och fungerar.

Bygget, CI och reglerna kommer från Novila (samma ägare). När något saknas här: gör som Novila, och skriv in regeln
här när den börjar gälla Imorn.

## Stack
- Kotlin 2.4 via AGP 9:s inbyggda Kotlin (ingen `kotlin-android` i modulerna), Gradle 9.8
- Jetpack Compose med eget designsystem ovanpå Material 3 1.5 (beta), compileSdk/targetSdk 37, minSdk 26
- Navigation 3 (`androidx.navigation3`) med en back stack per flik (i dag bara *Imorgon*)
- Alla versioner i `gradle/libs.versions.toml`. Hårdkoda aldrig versioner i byggfiler.
- DI med [Metro](https://zacsweers.github.io/metro/) (`dev.zacsweers.metro`, Kotlin-kompilatorplugin, ingen KSP).
  Varje skärm har en egen ViewModel (`StateFlow`) i sin feature, skapad av Metro och scopad till sin Navigation 3-entry.
  Ingen delad ViewModel. Skärmarna i `feature/*` är tillståndslösa.
- Data lokalt: inställningar i Preferences DataStore, krypterad med Tink och en nyckel i Android Keystore (samma
  som Novila), via storarna i `:core:data`; kapitlen i Room (`:core:database`, kompilatorn i KSP).
- Gemini Nano (ML Kit Prompt API via AICore) för assistenten, bara på enheten.

## Struktur
Moduler. Namespace och paket följer sökvägen (`:core:ui` → `se.imorn.ui`, `:feature:today` → `se.imorn.feature.today`):
- `:app` – bara skalet: `ImornApplication`, `MainActivity`, `ImornApp`, `navigation/TopLevelDestination` och `di/`
  (`AppGraph`, `ImornViewModelFactory`). Ingen skärm, ViewModel eller logik som går att lägga i en modul.
- `:feature:today` (*Imorgon*, startskärmen), `:feature:chapters` (ett nytt kapitel ur mallbiblioteket),
  `:feature:onboarding` (välkomsten på gryningsscenen) och `:feature:timeline` (ett kapitel, ett steg och något
  sparat). Varje feature äger sin navigation (`navigation/`) och beror aldrig på en annan.
- `:core:designsystem` (enda modulen som använder Material 3 direkt), `:core:ui` (det flera features delar) och
  `:core:navigation`.
- `:core:data` (krypterad Preferences DataStore, Gemini Nano och innehållspaketet som assets) och `:core:database`
  (Room: kapitlen, och foton och papper som filer).
- Ren Kotlin: `:core:model` (kapitlen och planen), `:core:content` (innehållsformatet och `instantiate`),
  `:core:coach` (assistentens prompt) och `:core:notificationpolicy` (notisbudgeten).
- `build-logic` – inkluderat bygge med convention plugins (`imorn.*`) och `imorn { }`, se `build-logic/README.md`
- `tools/icon` – `render_icon.py` ritar appikonens reserver, se *Appikonen* i `.claude/rules/ui.md`.

Beroenden går nedåt: `:app` → `:feature:*` → `:core:ui`/`:core:navigation`/`:core:designsystem` → ren Kotlin.
Strängar och ikoner ligger i modulen som använder dem; det flera moduler använder ligger i `:core:ui`.

## Områdesregler
Varje moduls fullständiga beskrivning och reglerna för ett område står i `.claude/rules/`. Claude Code läser in en
regelfil först när du arbetar med filer som matchar dess `paths`, så den kostar inget i sessioner som inte rör
området. Läs filen själv innan du planerar en ändring i området, även innan du har öppnat någon fil där.
- `ui.md` – `:app`, skärmarna i `feature/*`, `:core:designsystem`, `:core:ui`, `:core:navigation`: paletten
  *Gryning*, bokhyllan, gryningsscenen, appikonen och fallgroparna i Compose (locale, `Modifier`, insets)
- `plan.md` – `:core:model`, `:core:content`, `:core:coach`, `:core:notificationpolicy` och `content/`
- `data.md` – `:core:data` och `:core:database`: storarna, Room, migrationer och sparade filer
- `gradle.md` – Gradle-bygget och dess fallgropar

En ny regel läggs i regelfilen som äger området (utvidga dess `paths` om den inte täcker filerna), en regel som
gäller överallt här. En ny modul får sin beskrivning i områdets regelfil och en rad i *Struktur* ovan.

## Tokens
Kontexten är dyr: varje rad som läses in följer med resten av sessionen.
- Sök med Grep (eller Glob) innan du läser, och läs sedan bara raderna du behöver (`offset`/`limit`). En hook
  (`.claude/hooks/read-guard.py`) stoppar en läsning av en hel fil över 400 rader utan `offset` eller `limit`;
  behövs hela filen, läs den med `limit`.
- Läs inte genererade filer (`build/`, skärmdumpar, scheman) om uppgiften inte gäller dem.
- Kör Gradle med `--console=plain` och filtrera utdata (`2>&1 | tail -n 60` eller `grep -E "e: |w: |FAILED"`).
- Använd en subagent (Explore) för breda sökningar över många moduler, så bara slutsatsen kommer tillbaka.

## Arbetsflöde
- Pusha aldrig direkt till `main`. Skapa en branch och öppna en PR.
- **Commit-meddelanden, branchnamn, PR-titlar och PR-beskrivningar skrivs på engelska.**
  PR-titeln (vid squash: commit-rubriken) blir en rad i testarnas release notes, så skriv den som en tydlig
  sammanfattning. `scripts/release-notes.sh` listar allt på `main` sedan förra lyckade distributionen.
- `CI`-workflowen måste vara grön innan PR:en är klar. Den kör tre jobb parallellt: Format and lint (`spotlessCheck`
  och `:app:lintDebug`, alla moduler), Tests (`test verifyAndRecordRoborazziDebug koverXmlReportCoverage`) och Build
  (`:app:assembleDebug :app:checkDebugBadging`); kontrollen *Lint, test and build* samlar dem och är den som krävs.
  CI körs bara på PR:er mot `main` och på `main`, inte på en branch utan PR. Gemensamma steg ligger i
  `.github/actions/`. Bygg aldrig egen cachelogik. En PR som bara ändrar dokumentation (`docs/`, `*.md`) hoppar
  över Gradle-jobben men får ändå en grön kontroll. Concurrency, triggers och cache förklaras i
  [docs/ci.md](docs/ci.md): concurrency-grupper börjar med workflowens namn, CI avbryter en PR:s gamla körning,
  och distributionen köar och avbryts aldrig.
- **Bara GitHub-hostade runners.** Repot är publikt, så en PR från en fork kör sin kod i CI; på en egen runner
  skulle den köra på vår maskin. Ingen self-hosted runner här (Novilas Hive-runner gäller bara Novila).
- Kan din miljö inte nå Google Maven: öppna PR:en, följ körningen och rätta tills den är grön. Säg aldrig att något
  fungerar utan grönt CI.
- När PR:en mergas skickar `Distribute to testers` (`distribute.yml`) debug-APK:n (`se.imorn.debug`) via Firebase
  App Distribution, autentiserad med Workload Identity Federation. Se `docs/firebase.md`; ändra setupen via
  `scripts/bootstrap-firebase.sh` och håll den idempotent. Inga JSON-nycklar och ingen `google-services.json`.
- `Record references` (`screenshots.yml`) spelar in skärmdumpar (`/record-screenshots`), badging
  (`/record-badging`) eller Room-scheman (`/record-schemas`) som kommentar på en PR eller från Actions-fliken.

## Kvalitet
- **Lint-policy:** `lint.xml` i roten sätter projektets regler och orsaker. Höj inte lint-fel till varningar
  och lägg inte till `@SuppressLint` utan att fråga ägaren.
- **Kodstil:** `.editorconfig`. Inga wildcard-importer, trailing commas, max 120 tecken. Spotless med ktlint
  kontrolleras i CI; `./gradlew spotlessApply` rättar.
- **Lär av CI:** när CI hittar en ny typ av misstag, gör tre saker i samma PR. Fixa felet, lägg till regeln i
  `lint.xml` (eller i `ArchitectureTest` om lint inte kan fånga det), och lägg till en rad under *Kända fallgropar*
  (eller i områdets regelfil).
- **Badging:** `app/badging/debug-badging.txt` är den granskade bilden av APK:n (paket, SDK-nivåer, behörigheter,
  features, exporterade komponenter). Build-jobbet fallerar på minsta skillnad. Är ändringen avsiktlig: kommentera
  `/record-badging` på PR:en och skriv i beskrivningen varför. Uppdatera aldrig filen bara för att få grönt; en ny
  behörighet eller exporterad komponent kräver ägarens ok.

### Kända fallgropar
Misstag som redan har gjorts i Novila, med samma bygge. Gör dem inte här. De som hör till ett område står i dess
regelfil: Compose i `ui.md`, bygget (Kotlin-versionen, providers, debug-beroenden, Spotless) i `gradle.md`.
- **Robolectric:** tester körs mot API 36 via `src/test/resources/robolectric.properties` (på API 37 faller Compose
  UI-tester). Nya moduler med Robolectric-tester behöver samma fil. Använd `imorn.android.unittest`, som sätter
  JDK 21-flaggan Robolectric kräver, i stället för egen testkonfiguration.
- **GitHub API:** GraphQL och `gh pr create` fungerar inte i molnsessioner. Använd REST eller GitHub-MCP:n.

## Googles Android-skills
Repot aktiverar pluginet `android-skills` från github.com/android/skills via `.claude/settings.json`.
Använd rätt skill innan du ändrar respektive område: `agp-9-upgrade` (bygget, AGP/Kotlin/KSP), `edge-to-edge`
(nya skärmar, listor, FAB:ar, textfält), `testing-setup`, `navigation-3`, `adaptive`,
`jetpack-compose-performance`, `r8-analyzer`, `android-permissions-security` (före nya behörigheter, t.ex. kamera
och notiser), `play-policy-insights` och `glance-canonical-layouts` (widgeten i Fas 3).

## Gradles skills
Repot aktiverar också pluginet `gradle-skills` från github.com/gradle/gradle-skills
(`gradle-skills:gradle-best-practices` och `gradle-skills:gradle-wrapper-upgrade`):
- `gradle-best-practices` för att granska bygget mot Gradles best practices och rätta det som hittas. Reglerna under
  *Gradle-bygget* går före: ett förslag som bryter mot dem (t.ex. Isolated Projects) tas inte.
- `gradle-wrapper-upgrade` när Gradle-versionen höjs (wrappern med checksumma), ihop med `agp-9-upgrade` när AGP,
  Kotlin eller KSP följer med.

## Gradle-bygget
- Varje modul använder convention plugins (`imorn.*`), som ensamma sätter SDK-nivåer, Java-nivå, namespace,
  signering, lint och tester. Modulens `build.gradle.kts` innehåller bara `plugins`, `imorn { }` och `dependencies`.
- Isolated Projects är påslaget: aldrig `allprojects {}`, `subprojects {}`, `rootProject.<…>` eller `project(":x")`.
- Allt annat (pluginen, `imorn { }`, Isolated Projects, KSP, `workers.max`, nya moduler och plugins) står i
  `.claude/rules/gradle.md`. Läs den innan du ändrar bygget eller lägger till en modul.

## Tester
- **Logik:** rena Kotlin-tester i `src/test`. **UI:** Compose UI-tester med `createComposeRule()` på Robolectric,
  också i `src/test`. Ingen emulator behövs.
- **Skärmdumpar:** varje `@Preview` testas mot referensbilder i modulens `src/test/screenshots`. Ändrar du UI med
  flit: skriv `/record-screenshots` i PR-beskrivningen, så spelar CI in och pushar bilderna. Ta aldrig fram
  referensbilder lokalt.
- **Arkitektur:** `ArchitectureTest` i `:app` läser alla moduler och bevakar reglerna nedan som går att kontrollera
  (Material 3 bara i designsystemet, inga hårdkodade texter, features beror inte på varandra, ViewModels via Metro
  och aldrig i `:app`, ingen SharedPreferences). Ny regel som går att kontrollera i källkoden: lägg den där.

## Regler
- **Känslan:** en lugn vän som har gjort det här förut. Startskärmen visar högst tre saker. Att skjuta upp är ett
  val: varje påminnelse har *Imorgon*. Inga streaks, ingen skuld, inga röda siffror. Texterna är varma, korta och
  aldrig byråkratiska ("Dags att göra flyttanmälan. Det tar fem minuter.").
- **Designsystem:** feature-kod använder bara `se.imorn.designsystem` och aldrig `androidx.compose.material3`
  direkt. Saknas en komponent, lägg till en `Imorn*`-wrapper i `:core:designsystem` (`component/`). Wrappers
  exponerar inga Material-typer. Använd `ImornTheme.colors/typography/spacing/shapes`, aldrig hårdkodade färger
  eller dp-värden i features. Behövs en färgroll som saknas, lägg till den i `ImornColors` och `toImornColors()`.
- **Inga emojis i UI:t**, och inte i assistentens svar.
- **Innehåll:** mallar och länkar är data i `content/`, aldrig kod. Id:n är för evigt (ett steg byter text, aldrig
  id; ett borttaget markeras `retired`). Varje text finns på svenska och engelska. Belopp, procentsatser och tak
  skrivs aldrig in i stegen; länken till källan gäller. Mallar beskriver vanliga steg, aldrig "allt du måste".
- **Planen är deterministisk:** mallen, svaren och ankarna bestämmer stegen och datumen, testat i `:core:model` och
  `:core:content`. Ingen modell föreslår steg eller datum.
- **Inga hårdkodade texter:** all text som visas eller läses upp kommer från `strings.xml`. Engelska i `values/`;
  svenska i `values-sv/` när den läggs till (då även i `localeFilters` i `imorn.android.application`). Symboler och
  enheter markeras `translatable="false"`.
- **För vem som helst:** stor text, enkel svenska, TalkBack. `contentDescription` på ikoner, eller `null` när de är
  dekorativa.
- **Navigation:** nya skärmar blir en `@Serializable`-rutt som implementerar `NavKey` i featurens `navigation/`,
  plus en `entry<…>` i featurens entry-funktion. Navigation inom en flik går via `Navigator`; till en annan flik
  via callbacks som `:app` kopplar. Entryn hämtar sin ViewModel och skickar state och callbacks till en
  tillståndslös skärm, så skärmen går att förhandsgranska.
- **ViewModels och DI (Metro):** en ViewModel per skärm, i skärmens feature, aldrig i `:app` och aldrig delad.
  Annotera `@ViewModelKey @ContributesIntoMap(AppScope::class) @Inject`; hämta den i entryn med `metroViewModel()`,
  aldrig `viewModel()`. Det skärmar delar (lagring, källor) ligger i `:core:*` som `@SingleIn(AppScope::class)`.
  Allt som bidrar till `AppScope` måste ligga på `:app`:s classpath (direkt `implementation`).
- Logik som går att testa utan Android ligger i rena Kotlin-moduler (`imorn.jvm.library`) med enhetstester.
- **Lagring:** aldrig SharedPreferences, och ändra aldrig en tabell utan att höja versionen i `ImornDatabase` och
  skriva en migration med test. Storarna, Room och det exporterade schemat: se `.claude/rules/data.md`.
- **Säkerhetskopia:** Auto Backup och flytt till ny telefon kopierar ingenting (`app/src/main/res/xml`, båda filerna
  i synk); nyckeln ligger i Keystore och kan ändå inte följa med. Ändras det (Fas 6), ändras integritetstexten också.
- **Assistenten (Gemini Nano):** körs i AICore på telefonen; prompten lämnar aldrig enheten. Modellen får aldrig
  bestämma planen, stegen, datum eller belopp: den förklarar bara fakta den får (`AssistantPrompt`), och svaret ska
  märkas som AI när det visas. Att använda den till mer, eller byta till en molnmodell, kräver ägarens ok.
- **Integritet:** allt ligger lokalt tills användaren själv väljer att dela. Brev och papper med personnummer lämnar
  aldrig enheten. Appen har ingen `INTERNET`-behörighet: ML Kit drar in Googles telemetri, som ber om den, så den
  tas bort i appens manifest (`tools:node="remove"`). Lägg aldrig tillbaka den utan ägarens ok; roadmapen tar det
  beslutet i Fas 4 (innehållet) och Fas 7 (familjen). Inga analysverktyg eller Firebase-SDK:er i appen utan att
  ägaren ber om det.
- **Foton och papper:** bara Photo Picker, kameraappen (`TakePicture`) och filväljaren, så appen behöver ingen
  behörighet för dem; `CAMERA` kommer först med före/efter-hinnan (Fas 2, ägarens ok). Allt kopieras till appens
  privata lagring direkt och läses aldrig från galleriet igen. Papper visas bara i appen (PDF:er med
  `PdfRenderer`), skickas aldrig till en annan app och märks *Bara på den här telefonen*.
- **Behörigheter:** varje ny behörighet (kamera, notiser …) kräver ägarens ok, skillen
  `android-permissions-security` och en ny rad i badging. Fråga i sammanhanget, aldrig vid start.
- **Påminnelser** (Fas 3): som i Novila, bara WorkManager, ett engångsjobb som lägger nästa själv. Inga periodiska
  jobb, ingen tjänst i förgrunden och inga exakta larm.
- `versionCode` sätts av CI. Ändra `versionName`-prefixet (`0.1`) bara vid större milstolpar.
- Signering: `app/debug.keystore` är bara för debug-byggen. Release signeras med en hemlig nyckel som bara finns i
  GitHub-hemligheterna. Lägg aldrig en release-nyckel eller dess lösenord i repot.
- Nya beroenden: lägg dem i versionskatalogen och motivera dem i PR-beskrivningen.
- Pinna GitHub Actions till commit-SHA med versionskommentar. Dependabot håller dem uppdaterade.
