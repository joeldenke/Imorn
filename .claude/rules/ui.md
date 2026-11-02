---
paths:
  - "app/**"
  - "feature/**"
  - "core/designsystem/**"
  - "core/ui/**"
  - "core/navigation/**"
  - "tools/icon/**"
---
# Imorn – Gränssnittet

Del av projektets instruktioner (`CLAUDE.md`). Claude Code läser in filen när du arbetar med filerna ovan;
läs den själv innan du planerar en ändring här. Allt i `CLAUDE.md` gäller också.

## Moduler
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

## Regler
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

## Kända fallgropar
- **Locale i composables:** använd aldrig `Locale.getDefault()` i en `@Composable` (lint `NonObservableLocale`).
  Läs den med `LocalConfiguration.current.locales[0]`.
- **Modifier-parametrar:** en composable har bara en `Modifier`-parameter, och den heter `modifier`.
- **Insets i toppen:** appen är edge-to-edge, och `Scaffold` lägger bara insets på innehållet, aldrig på en egen
  `topBar`. En egen toppbar tar själv `WindowInsets.safeDrawing` (topp och sidor), som titeln i `ImornScaffold`.
