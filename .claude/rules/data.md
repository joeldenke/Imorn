---
paths:
  - "core/data/**"
  - "core/database/**"
---
# Imorn – Lagring

Del av projektets instruktioner (`CLAUDE.md`). Claude Code läser in filen när du arbetar med filerna ovan;
läs den själv innan du planerar en ändring här. Allt i `CLAUDE.md` gäller också.

## Moduler
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

## Regler
- **Lagring:** aldrig SharedPreferences. Inställningar och små värden sparas i en store i `:core:data` ovanpå
  `PreferencesStore` (krypterad DataStore, en `@SingleIn(AppScope::class)` per fil, testad med Robolectric och
  `testCipher()`). Kapitel och steg ligger i Room (`:core:database`, `imorn { room() }`), testat med en databas i
  minnet. Ändra aldrig en tabell utan att höja versionen i `ImornDatabase` och skriva en migration med test
  (`MigrationTest` bygger den gamla databasen ur dess exporterade schema och låter Room migrera och kontrollera). Det
  exporterade schemat i `schemas/` är granskat som badgingen: CI (Tests) fallerar när bygget ändrar det, och
  `/record-schemas` spelar in det. Ta aldrig fram schemat lokalt.
