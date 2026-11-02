---
paths:
  - "core/model/**"
  - "core/content/**"
  - "core/coach/**"
  - "core/notificationpolicy/**"
  - "content/**"
  - "docs/content-system.md"
  - "docs/templates.md"
  - "docs/timing.md"
---
# Imorn – Kapitlen, planen och innehållet

Del av projektets instruktioner (`CLAUDE.md`). Claude Code läser in filen när du arbetar med filerna ovan;
läs den själv innan du planerar en ändring här. Allt i `CLAUDE.md` gäller också.

## Moduler
- `:core:coach` – ren Kotlin: assistentens `LanguageModel`-gränssnitt och `AssistantPrompt` (bygger prompten och
  tvättar svaret, enhetstestat). Se *Assistenten* i `CLAUDE.md`.
- `:core:notificationpolicy` – ren Kotlin: notisbudgeten (`NotificationPolicy`): allt som är försenat eller dags
  samlas i en notis per dag, högst två om ett hårt datum får stå ensamt, aldrig under tysta timmar, och *Veckan som
  kommer* bara om användaren slagit på den. `Chapter.reminders()` gör kapitlets öppna, daterade steg till `Reminder`.
  Policyn skriver inga texter; notislagret hämtar dem från `strings.xml`.
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
- `content/` – innehållspaketet: `links.json` (länkregistret) och `templates/*.json` (13 mallar: flytta, köpa och
  sälja bostad, första egna boendet, renovera, vänta barn, gifta sig, byta jobb, börja plugga, ny i Sverige, gå i
  pension, ta körkort, köpa eller sälja bil). Varje mall har `order` (platsen i biblioteket), `summary` och `icon`.
  `imorn { bundledContent() }` gör det till testindata och, i `:core:data`, till appens assets; `BundledContentTest`
  validerar det i CI.
