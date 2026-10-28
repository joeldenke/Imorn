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
- `:app` – bara skalet: `ImornApplication` (håller grafen), `MainActivity`, `ImornApp` (flikar och `NavDisplay`),
  `navigation/TopLevelDestination` och `di/`: `AppGraph` (Metro-grafen, där källor väljs) och
  `ImornViewModelFactory`. Ingen skärm, ViewModel eller logik som går att lägga i en modul.
- `:feature:today` – *Imorgon*, startskärmen: dagen och en hälsning efter timmen, nedräkningen till närmaste ankare
  (`nextMilestone`, en stor siffra), det enda att göra nu i aprikosblocket (`ImornNowCard`, *Klart* och *Imorgon*);
  sedan resten av `tomorrowPlan` (högst tre saker totalt) med datum och en ring att bocka av, *Längre fram*,
  assistenten och kapitlen som bokryggar i en hylla (`ImornShelf`, framsteg `progress`). Tomt läge bjuder in till
  första kapitlet.
- `:feature:chapters` – kapitlen: `NewChapterScreen` (mallbiblioteket två och två med ikon och en rad om vad mallen
  täcker, *Föreslaget för dig* efter livsfaserna först, frågorna en i taget med framsteg, ankardatum som får vara
  okända, sedan `instantiate` och spara).
- `:feature:onboarding` – välkomsten vid första start, tre sidor på gryningsscenen (`ImornDawnScene`): vad Imorn är,
  varför (fyra löften i glaspaneler, som bara säger det appen verkligen gör) och "Vad stämmer på dig just nu?"
  (`LifeStage`, flera val, i stället för ålder). Inget namn och inget annat appen inte behöver. Visas ensam (utan
  flikar) tills `AppSettings.onboarded` är satt; *Hoppa över* finns alltid. Livsfaserna bestämmer bara vilka mallar
  som kommer först (`suggestedFirst`), aldrig vilka som finns.
- `:feature:timeline` – ett kapitel (`ChapterScreen`: omslaget i indigo (`ImornCover`: titel, ankardatum som pills,
  framsteg i segment och tre siffror), flikarna `ImornTabs`, *Lista* som delflöden (`subflows`: varje fas ett kort
  med framsteg och vad som är nästa, öppnas till sina steg), *Tidslinje* som kalender (`ImornCalendarRow`: stora
  dagsiffror, månaden som rubrik, ankarna bland stegen, aprikosbandet för nu, `ImornNowBand`) med berättelsen under
  nu-bandet (`story`: avbockade steg med sina foton och anteckningar) och *Papper*, snabb-+ (fota,
  välj foto, steg, anteckning, papper), egna steg, och vila, avsluta och radera i ett blad bakom *Mer*), ett steg (`StepScreen`: när, varför, länkkort som alltid
  visar organisation och domän, anteckning, stegets foton, *Klart* med foto-uppmaningen, *Imorgon*, eget datum,
  *Gäller inte*) och något sparat (`EntryScreen`: fotot, anteckningen eller pappret, PDF:er sida för sida med
  `PdfRenderer` i appen, radera). ViewModels med ruttens id (`@AssistedInject`, `assistedMetroViewModel`).
  Varje feature äger sin navigation (`navigation/`): `@Serializable`-rutter (`NavKey`) och
  `EntryProviderScope<NavKey>.<feature>Entry(...)`, som `:app` lägger i sin `entryProvider`. Features beror aldrig
  på varandra (`ArchitectureTest`); flikbyten går via callbacks från `:app`.
- `:core:designsystem` – `se.imorn.designsystem`, enda modulen som använder Material 3 direkt
  - `theme` – `ImornTheme`: färger (`ImornColors`, paletten *Gryning* på papper), typografi (Bricolage Grotesque),
    former och spacing
  - `component` – `Imorn*`-komponenter som wrappar Material 3 (`ImornScaffold`, `ImornCard`, `ImornButton`,
    `ImornText`, `ImornSheet`, `ImornFloatingButton` …), `ImornIcons` (egna vektorer, inget ikonbibliotek; mallarnas
    linjeikoner via `ImornIcons.template(namn)`), mallens märke (`ImornChapterMark`), `ImornTag`, bokhyllan
    (`Shelf.kt`: `ImornNowCard`, `ImornCountdown`, `ImornCheckCircle`, `ImornShelf`, `ImornCover`, `ImornStat`,
    `ImornSegments`, `ImornTabs`, `ImornPolaroid`), kalendern (`Calendar.kt`: `ImornCalendarRow`, `ImornDateBadge`,
    `ImornMonthHeading`, `ImornNowBand`) och
    `ImornImage` (en sparad bild, läst utanför huvudtråden; en lugn ruta i previews), och gryningsscenen (`Dawn.kt`:
    `ImornDawnScene`, `ImornGlassPanel`, `ImornSceneText`, `ImornSceneButton`, `ImornPageIndicator`; previews lägger
    sig i `ImornStillGlass`)
- `:core:ui` – det flera features delar: `dayText` (i dag, i morgon, i går eller datumet i användarens locale; dagen
  kommer från ViewModeln) och kalenderns `dayNumber`, `monthShort`, `monthHeading` och `longDayText`, `rememberCapture` (kameraappen via `TakePicture` och en egen `FileProvider` i cachen,
  Photo Picker och filväljaren, inga behörigheter; ligger i entryn, aldrig i skärmen), `PhotoPromptDialog` och
  `PreviewData` (samma kapitel, dag, `TODAY`, och sparade saker, `kept`, i alla previews)
- `:core:navigation` – `NavigationState` (en back stack per flik) och `Navigator`
- `:core:coach` – ren Kotlin: assistentens `LanguageModel`-gränssnitt och `AssistantPrompt` (bygger prompten och
  tvättar svaret, enhetstestat). Se *Assistenten* under Regler.
- `:core:notificationpolicy` – ren Kotlin: notisbudgeten (`NotificationPolicy`): allt som är försenat eller dags
  samlas i en notis per dag, högst två om ett hårt datum får stå ensamt, aldrig under tysta timmar, och *Veckan som
  kommer* bara om användaren slagit på den. `Chapter.reminders()` gör kapitlets öppna, daterade steg till `Reminder`.
  Policyn skriver inga texter; notislagret hämtar dem från `strings.xml`.
- `:core:data` – lagring i Preferences DataStore: `SettingsStore` (tema, bakgrundsfärger och om välkomsten är klar,
  `AppSettings`) och `ProfileStore` (livsfaserna, `Profile`). `SettingsStore` är en
  singleton i `AppScope` vars `StateFlow` får läsarna att läsa om efter en sparning. `PreferencesStore` (samma som
  Novilas) håller datan i minnet och köar sparningar i storens egen scope, i ordning och klara även när ViewModeln
  rensas. Filerna förseglas av `EncryptingPreferencesSerializer` med `KeystoreDatastoreCipher` (Tink, nyckeln i
  Android Keystore); en fil som inte går att öppna läses som tom. Tills första läsningen är klar är värdet `null`
  och `MainActivity` ritar inget, så temat aldrig blinkar. Här finns också `GeminiNanoLanguageModel` (bunden som
  `LanguageModel`) och `BundledContent`, innehållspaketet som appen bär med sig (`content/` som assets).
- `:core:database` – Room: `ImornDatabase` (kapitel, ankare, faser, steg och minnen, med främmande nycklar som tar
  allt i ett kapitel med sig) och `ChapterRepository` (singleton i `AppScope`), som sparar hela kapitel i en
  transaktion och läser dem som `Flow`, så skärmarna läser om efter varje sparning. Ändra ett kapitel med
  funktionerna i `:core:model` och spara det. Foton och papper är filer i `files/kept/<kapitel>/` (`KeptFiles`):
  bilder sparas om som JPEG, rättvända, högst 2560 px och utan kamerans metadata (plats), PDF:er som de är, var och en
  med en miniatyr. `ChapterRepository.keep`/`delete` är enda vägen in och ut, så filerna alltid följer med när en
  post eller ett kapitel raderas. Schemat exporteras till `core/database/schemas`, se *Lagring*.
- `:core:model` – ren Kotlin: kapitlen (`Chapter`, `Anchor`, `Phase`, `Step`, `Entry`, `LinkRef`), berättelsen
  (`Story.kt`: `story` och `papers`; papper är alltid `isPrivate` och aldrig del av berättelsen), `RelativeTime`
  (dag från ett ankare: dagar, månader eller "nästa år, april") och planen (`Plan.kt`: `dateOf`, `upcoming`,
  `timeline` runt nu-linjen, `subflows` (faserna som delflöden med framsteg), ändringar som `complete` och
  `withAnchor`), `tomorrowPlan` för *Imorgon* (högst tre
  saker: det försenade, sedan nästa steg per kapitel; långa trådar under *Längre fram*), samt `AppSettings`.
  Ett eget datum på ett steg vinner alltid och står kvar när ankaret flyttas; okända ankare ger steg utan datum i
  mallens ordning.
- `:core:content` – ren Kotlin: innehållsformatet v1 (`Template`, `Link`, kotlinx.serialization), `ContentPack`,
  `ContentValidator` och `instantiate` (mall + svar + ankardatum → `Chapter`). Se `docs/content-system.md`.
- `build-logic` – inkluderat bygge med convention plugins (`imorn.*`) och `imorn { }`, se `build-logic/README.md`
- `tools/icon` – `render_icon.py` ritar appikonens reserver, se *Appikonen* under Regler.
- `content/` – innehållspaketet: `links.json` (länkregistret) och `templates/*.json` (13 mallar: flytta, köpa och
  sälja bostad, första egna boendet, renovera, vänta barn, gifta sig, byta jobb, börja plugga, ny i Sverige, gå i
  pension, ta körkort, köpa eller sälja bil). Varje mall har `order` (platsen i biblioteket), `summary` och `icon`.
  `imorn { bundledContent() }` gör det till testindata och, i `:core:data`, till appens assets; `BundledContentTest`
  validerar det i CI.

Beroenden går nedåt: `:app` → `:feature:*` → `:core:ui`/`:core:navigation`/`:core:designsystem` → ren Kotlin.
Strängar och ikoner ligger i modulen som använder dem; det flera moduler använder ligger i `:core:ui`.

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
  `lint.xml` (eller i `ArchitectureTest` om lint inte kan fånga det), och lägg till en rad under *Kända fallgropar*.
- **Badging:** `app/badging/debug-badging.txt` är den granskade bilden av APK:n (paket, SDK-nivåer, behörigheter,
  features, exporterade komponenter). Build-jobbet fallerar på minsta skillnad. Är ändringen avsiktlig: kommentera
  `/record-badging` på PR:en och skriv i beskrivningen varför. Uppdatera aldrig filen bara för att få grönt; en ny
  behörighet eller exporterad komponent kräver ägarens ok.

### Kända fallgropar
Misstag som redan har gjorts i Novila, med samma bygge. Gör dem inte här.
- **Locale i composables:** använd aldrig `Locale.getDefault()` i en `@Composable` (lint `NonObservableLocale`).
  Läs den med `LocalConfiguration.current.locales[0]`.
- **Kotlin-version i AGP 9:** lägg aldrig till `org.jetbrains.kotlin.android`, inte heller med `apply false`.
- **Robolectric:** tester körs mot API 36 via `src/test/resources/robolectric.properties` (på API 37 faller Compose
  UI-tester). Nya moduler med Robolectric-tester behöver samma fil. Använd `imorn.android.unittest`, som sätter
  JDK 21-flaggan Robolectric kräver, i stället för egen testkonfiguration.
- **Modifier-parametrar:** en composable har bara en `Modifier`-parameter, och den heter `modifier`.
- **Insets i toppen:** appen är edge-to-edge, och `Scaffold` lägger bara insets på innehållet, aldrig på en egen
  `topBar`. En egen toppbar tar själv `WindowInsets.safeDrawing` (topp och sidor), som titeln i `ImornScaffold`.
- **Providers från tasks:** ge tasks vanliga värden (strängar) för sådant som bara är text, aldrig ett värde härlett
  ur en `TaskProvider`; det ger ett dolt beroende på den tasken.
- **Beroenden som bara debug får:** deklarera varje bibliotek en modul importerar direkt i modulen, även om
  debug-bygget får det transitivt.
- **Spotless och Isolated Projects:** ge Spotless filträd (`fileTree(...)`) som `target`, aldrig glob-strängar.
- **GitHub API:** GraphQL och `gh pr create` fungerar inte i molnsessioner. Använd REST eller GitHub-MCP:n.

## Googles Android-skills
Använd rätt skill innan du ändrar respektive område: `agp-9-upgrade` (bygget, AGP/Kotlin/KSP), `edge-to-edge`
(nya skärmar, listor, FAB:ar, textfält), `testing-setup`, `navigation-3`, `adaptive`,
`jetpack-compose-performance`, `r8-analyzer`, `android-permissions-security` (före nya behörigheter, t.ex. kamera
och notiser), `play-policy-insights` och `glance-canonical-layouts` (widgeten i Fas 3).

## Gradle-bygget
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
- **Gryning:** paletten bär betydelse och används konsekvent: **indigo** (primary) är framtiden och överblicken,
  **aprikos** (secondary) är nuet och vägen, **rosa** (tertiary) är minnena. `Night` och `Dawn` är varumärkets
  färger utanför schemat. Typsnittet Bricolage Grotesque (variabelt, `res/font/bricolage_grotesque.ttf`, licens i
  `licenses/`) är inbakat; ladda aldrig ner typsnitt i runtime.
  Kort är vita på papperet med hårfin kant (`ImornTheme.surfaces`, som i Novila); `ImornCard` sköter det.
- **Bokhyllan:** vardagens utseende. Varmt papper (`Paper`), bläckmörk text och knappar (`Ink`, piller), stor
  vänlig typografi och några få modiga färgblock: aprikosblocket för det att göra nu, kapitlen som bokryggar i en
  hylla och varje kapitel under ett omslag i indigo. Lekfullt i formen, lugnt i färgen. Tidslinjen är en kalender
  (stora dagsiffror, månader som rubriker, ett aprikosband för nu), och minnen visas som snett uppsatta foton
  (`ImornPolaroid`). Rita inga solar, himlar eller landskap på vardagsskärmarna; gryningsscenen hör bara till
  välkomsten. Hälsningen följer timmen (`Today.hour()` i ViewModeln, aldrig klockan i en composable; previews
  `PreviewData.HOUR`).
- **Gryningsscenen:** varumärkets stora ögonblick (välkomsten): appikonen som landskap, natthimmel som värms mot
  horisonten, solen som stiger ur vattnet och speglingen i streck som smalnar av. Ritad i kod i `ImornDawnScene`,
  alltid mörk, med ljus text och glaspaneler (`ImornGlassPanel`) som suddar scenen bakom sig med Haze (Android 12+,
  annars en genomskinlig ton). Haze används bara där, och bara i `:core:designsystem`. Vardagsskärmarna är
  bokhyllan, inte glas.
- **Appikonen:** ett vitt i på den enfärgade Night-plattan, där pricken är solen och stammen solens spegling i
  vattnet. Vektorerna `app/src/main/res/drawable/ic_launcher_foreground.xml` och `…_monochrome.xml` och färgen
  `ic_launcher_background` (lika med `Night` i `Color.kt`) är källan. Ändras de: kör
  `python3 tools/icon/render_icon.py`, som ritar WebP-reserverna i `mipmap-*dpi` (Firebase App Distribution kan
  inte visa en adaptiv ikon) och Play-ikonen `docs/play-icon.png`. Rita aldrig om dem för hand.
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
- **Lagring:** aldrig SharedPreferences. Inställningar och små värden sparas i en store i `:core:data` ovanpå
  `PreferencesStore` (krypterad DataStore, en `@SingleIn(AppScope::class)` per fil, testad med Robolectric och
  `testCipher()`). Kapitel och steg ligger i Room (`:core:database`, `imorn { room() }`), testat med en databas i
  minnet. Ändra aldrig en tabell utan att höja versionen i `ImornDatabase` och skriva en migration med test
  (`MigrationTest` bygger den gamla databasen ur dess exporterade schema och låter Room migrera och kontrollera). Det
  exporterade schemat i `schemas/` är granskat som badgingen: CI (Tests) fallerar när bygget ändrar det, och
  `/record-schemas` spelar in det. Ta aldrig fram schemat lokalt.
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
