# Innehållssystemet – mallar och länkar som uppdateras utan ny app

Mallar, länkar, brevtyper och säsongstips är data. De skrivs i repot, valideras i CI, publiceras signerade till
Firebase Hosting och hämtas av appen i bakgrunden. Appen har alltid ett inbakat paket som reserv.

```
 content/*.json ──PR──▶ CI: schema · länk-id · sv+en · länkarna svarar
        │
      merge
        ▼
 publish-content.yml ──WIF──▶ signera (ed25519) ──▶ Firebase Hosting
                                                     content.imorn.se/v1/manifest.json
                                                     content.imorn.se/v1/packs/<hash>.json
        ▲
 link-check.yml (varje måndag) ──▶ issue "Länk död: skatteverket.flytt"

 Appen: WorkManager (en gång per dygn, nätverk krävs)
   GET manifest (ETag) ─▶ signatur ok? ─▶ minAppVersion ok? ─▶ hämta paket (hash) ─▶ spara ─▶ använd
   annars: senaste goda paket, annars det inbakade i assets/
```

## Varför inte Remote Config eller Firestore?

| Alternativ | För | Emot |
| --- | --- | --- |
| **Statisk JSON på Firebase Hosting** (valt) | Ingen SDK, ingen spårning, CDN, versionerat i git, granskas som kod, går att redigera från mobilen | Kräver `INTERNET`; ingen redigerings-UI (GitHub räcker) |
| Firebase Remote Config | Konsol för ändringar, stegvis utrullning | SDK + Installations-id, storleksgräns, ingen granskning eller historik som i git |
| Firestore | Realtid, frågor | SDK, konto/regler, överdrivet för läsdata som ändras en gång i veckan |

Remote Config kan bli aktuellt för feature-flaggor senare; innehållet hör hemma i git.

## Filer i `content/`

```
content/
  links.json            länkregistret
  templates/move.json   en fil per mall
  letters/*.json        brevtyper för brevtolken
  tips.json             säsongstips
  lifestages.json       livsfaser → föreslagna mallar
  schema/*.schema.json  JSON Schema för allt ovan
```

### Länk

```json
{
  "id": "skatteverket.flytt",
  "org": "Skatteverket",
  "kind": "authority",
  "url": "https://www.skatteverket.se/",
  "fallbackUrl": "https://www.skatteverket.se/",
  "title": { "sv": "Gör flyttanmälan", "en": "Report your move" },
  "searchHint": { "sv": "flyttanmälan", "en": "notify change of address" },
  "verifiedAt": "2026-10-20"
}
```

- `kind`: `authority` (myndighet), `public` (kommun, region, offentligt ägt), `consumer` (opartisk
  konsumentvägledning), `service` (företag – används sparsamt, aldrig betalt, aldrig som enda väg).
- `fallbackUrl` är myndighetens startsida om djuplänken dör; `searchHint` visas som "sök på …" i appen.
- Länkkortet i appen visar alltid organisationens namn och domän, så användaren ser vart hen går.

### Mall

```json
{
  "id": "move",
  "version": 3,
  "reviewedAt": "2026-10-20",
  "title": { "sv": "Flytta", "en": "Moving" },
  "lifestages": ["building", "midlife", "first-home"],
  "anchors": [{ "id": "moveDay", "label": { "sv": "Flyttdag", "en": "Moving day" }, "required": false }],
  "questions": [
    {
      "id": "housing",
      "text": { "sv": "Vad flyttar ni från?", "en": "What are you moving from?" },
      "options": [
        { "id": "rental", "label": { "sv": "Hyresrätt", "en": "Rented flat" } },
        { "id": "condo", "label": { "sv": "Bostadsrätt", "en": "Owned flat" } },
        { "id": "house", "label": { "sv": "Hus", "en": "House" } }
      ]
    },
    { "id": "kids", "text": { "sv": "Byter barnen skola eller förskola?", "en": "Do the kids change school?" }, "type": "bool" }
  ],
  "phases": [
    {
      "id": "papers",
      "title": { "sv": "Papper", "en": "Paperwork" },
      "steps": [
        {
          "id": "address-change",
          "title": { "sv": "Gör flyttanmälan", "en": "Report your move" },
          "why": { "sv": "Tidigast fyra veckor före och senast en vecka efter flytten.", "en": "…" },
          "at": { "anchor": "moveDay", "offsetDays": -14 },
          "hardDeadline": { "anchor": "moveDay", "offsetDays": 7 },
          "links": ["skatteverket.flytt", "adressandring.eftersandning"],
          "photoPrompt": null,
          "when": null
        },
        {
          "id": "terminate-lease",
          "title": { "sv": "Säg upp hyreskontraktet", "en": "Give notice on the lease" },
          "at": { "anchor": "moveDay", "offsetDays": -90 },
          "when": { "question": "housing", "equals": "rental" }
        }
      ]
    }
  ],
  "longThreads": [],
  "memoryPrompts": [{ "afterStep": "keys-handed-over", "text": { "sv": "Fota tomma rummen en sista gång" } }]
}
```

- **Id:n är för evigt.** Ett steg byter text, aldrig id; ett borttaget steg markeras `"retired": true`.
  Användarens kapitel minns `templateId`, `templateVersion` och varje stegs `stepId`, och det är så uppdateringar
  kopplas ihop.
- `at` är relativ tid: `{anchor, offsetDays}`, `{anchor, offsetMonths}` eller `{anchor, nextYear: {month: 4}}`
  för långa trådar som deklarationen.
- `when` gömmer steg som inte gäller enligt svaren. Uttrycken är medvetet enkla (`equals`, `in`, `isTrue`) och
  testas i `:core:content`. En fråga utan svar gömmer ingenting: hellre ett steg för mycket än ett som saknas.
- Frågor är val (`options`, med id och etikett) eller ja/nej (`"type": "bool"`).
- `reviewedAt` och länkarnas `verifiedAt` sätts först när någon har kontrollerat fakta mot källan.
- Formatet är `Template` och `Link` i `:core:content`; `ContentValidator` kontrollerar det JSON inte kan säga
  (språk, länk-id, ankare, villkor), och `BundledContentTest` kör den på hela `content/` i CI. Det JSON Schema som
  nämns nedan kommer i fas 4.

### Brevtyp

```json
{
  "id": "skatteverket.slutskattebesked",
  "sender": "Skatteverket",
  "match": { "all": ["slutskattebesked"], "any": ["skatteverket"] },
  "explain": { "sv": "Det här är resultatet av din deklaration: hur mycket skatt du ska betala eller få tillbaka." },
  "usuallyNeeded": { "sv": "Ofta inget. Står det att du ska betala, finns sista dagen i brevet." },
  "extract": ["amount", "dueDate"],
  "links": ["skatteverket.start"]
}
```

## Hur appen använder uppdateringar

| Ändring i mallen | Vad händer med användarens kapitel |
| --- | --- |
| Länk ändrad i registret | Gäller direkt – länkar hämtas alltid ur registret |
| Text i ett steg ändrad | Ändras om användaren inte har ändrat texten själv |
| Nytt steg | Förslag: "2 nya tips i mallen – lägg till?" |
| Steg `retired` | Ligger kvar hos den som har det, med en diskret notering |
| Ny fråga | Ställs bara i nya kapitel |

Nya kapitel använder alltid senaste paketet.

## Säkerhet och integritet
- Manifestet och paketen signeras i CI med en ed25519-nyckel som bara finns som GitHub-hemlighet (eller i Cloud
  KMS, åtkomlig via WIF). Den publika nyckeln ligger i appen. Ett paket med fel signatur används aldrig.
- `minAppVersion` och `schemaVersion` i manifestet: en äldre app hoppar över ett paket den inte förstår.
- Appen skickar inget om användaren: ett vanligt GET utan id, cookies eller parametrar. Hosting-loggarna
  innehåller IP-adresser; det står i integritetspolicyn.
- Användaren kan stänga av innehållsuppdateringar i Inställningar.

## Arbetsflödet från mobilen
1. Öppna `content/links.json` eller en mall i GitHub-appen/webben, redigera, *Propose changes*.
2. CI validerar och kör länkkontrollen för de ändrade länkarna.
3. Merge → `publish-content.yml` signerar och publicerar. Ute hos användarna inom ett dygn.
4. Gick något fel: återställ commiten, så publiceras föregående paket igen.

## Workflows att bygga (fas 4)
- `content.yml` på PR: schema, referenser, språk, länkar för ändrade filer.
- `publish-content.yml` på push till `main` (bara `content/**`): bygg paket, signera, `firebase deploy --only hosting`.
- `link-check.yml` schemalagd varje måndag: alla länkar; öppnar eller uppdaterar ett issue per död länk.
