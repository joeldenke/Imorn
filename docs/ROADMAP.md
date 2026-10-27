# Imorn – roadmap

> *Det finns en dag imorgon.* En sak i taget, allt på ett ställe, och minnena på köpet.

Imorn hjälper vem som helst att ta sig igenom livets stora projekt – flytta, sälja huset, renovera, få barn, ta
hand om ett dödsbo – utan att tappa kontrollen och utan att glömma det som var fint på vägen. Appen vet vad som
brukar behöva göras, länkar rätt till myndigheten, påminner i god tid (även om det dröjer ett år), tolkar brevet du
inte förstår, och gör till sist hela resan till ett minne du kan dela med familjen.

Den här filen är både produktvisionen och utvecklingsplanen. De färdiga mallarna, med steg och länkar, finns i
[templates.md](templates.md). Innehållssystemet (mallar och länkar som uppdateras utan ny app) beskrivs i
[content-system.md](content-system.md).

---

## 1. Känslan

Imorn ska kännas som en lugn vän som har gjort det här förut.

| Princip | Vad det betyder i appen |
| --- | --- |
| **En sak i taget** | Startskärmen visar aldrig mer än tre saker. Resten finns, men den skriker inte. |
| **Imorgon är okej** | Varje påminnelse har knappen *Imorgon*. Att skjuta upp är ett val, inte ett misslyckande. Inga streaks, ingen skuld, inga röda siffror. |
| **Kontroll utan arbete** | Mallarna gör jobbet: du väljer "Vi ska flytta 1 mars" och har en färdig plan på tio sekunder. |
| **Allt är ett minne** | När du bockar av ett steg kan du spara ett foto av det. Checklistan blir berättelsen. |
| **För vem som helst** | Stor text, enkel svenska, uppläsning, TalkBack, fungerar offline och utan konto. En 19-åring och en 84-åring ska båda klara första flytten respektive dödsboet. |
| **Ditt, på din telefon** | Allt ligger lokalt tills du själv väljer att dela. Brev med personnummer lämnar aldrig enheten. |

Gryning-paletten bär betydelse och används konsekvent: **indigo** är framtiden och överblicken (det som ska göras),
**aprikos** är nuet och vägen (det som händer nu), **rosa** är minnena (det som har varit).

Ton i texterna: varm, kort, aldrig byråkratisk. "Dags att göra flyttanmälan. Det tar fem minuter." – inte
"Påminnelse: Flyttanmälan ska inkomma till Skatteverket".

---

## 2. Kärnkonceptet: kapitel med en nu-linje

Allt i Imorn är ett **kapitel** i livet: *Flytten till Gävle*, *Badrummet*, *Lilla Elsa*, *Mammas dödsbo*.

Ett kapitel är **en enda tidslinje** med en lysande **nu-linje** i mitten:

```
   ┌──────────────────────────────────────────────┐
   │  ○  15 mars   Tillträde                 (indigo, framtid)
   │  ○  1 mars    Flyttdag – boka hiss
   │  ○  22 feb    Säga upp elavtalet
   │  ●  Imorgon   Gör flyttanmälan      [Gör nu]   ← nästa steg
 ══╪══ NU ════════════════════════════════════════  (aprikos)
   │  ◆  igår      Packade köket  [foto: 14 lådor] (rosa, minne)
   │  ✓  10 feb    Flyttfirma bokad   [kvitto]
   │  ◆  2 feb     Första visningen  [foto: tomma vardagsrummet]
   └──────────────────────────────────────────────┘
```

- **Ovanför nu-linjen**: stegen som väntar (checklistan), sorterade på tid.
- **Under nu-linjen**: det som har hänt – avbockade steg, foton, anteckningar, kvitton, brev.
- **Checklistan och tidslinjen är samma data**, två vyer: *Lista* (grupperad i faser, för att beta av) och
  *Tidslinje* (för att se helheten och minnas). Inget dubbelarbete.
- Ett avbockat steg **faller ner genom nu-linjen** och blir ett minne. Animationen är kort, lugn och sker en gång.

### Byggstenarna

| Byggsten | Vad det är | Exempel |
| --- | --- | --- |
| **Kapitel** | Ett livsprojekt, skapat från en mall eller tomt | Flytten, Sälja huset |
| **Ankardatum** | De datum allt annat räknas från | Flyttdag, tillträde, beräknad förlossning |
| **Fas** | En grupp steg | Planera, Papper, Packa, Flyttdagen, Efteråt |
| **Steg** | Något att göra. Har tid (fast eller relativ ett ankare), länkar, ansvarig, foto-uppmaning | "Gör flyttanmälan" (ankare: flyttdag −7 d) |
| **Minne** | Foto, anteckning, röstanteckning, före/efter-par | "Sista kvällen i gamla lägenheten" |
| **Papper** | Dokument, kvitto, kontrakt, inskannat brev – privat som standard | Köpekontrakt, offert, brev från Försäkringskassan |
| **Lång tråd** | Ett steg långt fram, som överlever att kapitlet är "klart" | Deklarera försäljningen i maj nästa år |
| **Återkommande steg** | Ett steg som kommer tillbaka (varje år i en månad, eller ett ankare + 12 månader); när det bockas av läggs nästa gång | Byta till vinterdäck varje oktober, service varje år |
| **Länk** | En referens till myndighet eller tjänst, hämtad ur länkregistret | Skatteverket – flyttanmälan |

### Relativ tid och ankare

Mallarnas steg anges relativt ett ankare: `flyttdag − 6 veckor`, `tillträde + 3 månader`, `nästa år, april`.
Flyttas flyttdagen, flyttas hela planen – utom de steg användaren har satt ett eget datum på. Ankare kan vara
okända ("vi vet inte när än"): då visas stegen i ordning utan datum, och appen frågar efter datumet vid rätt tillfälle.

---

## 3. Fem flöden som gör Imorn unikt

### 3.1 Starta ett kapitel på tio sekunder
1. *Nytt kapitel* → "Vad händer?" → stora kort: Flytta, Sälja bostad, Renovera, Vänta barn …
2. Två–fyra frågor som formar planen: "Hyresrätt, bostadsrätt eller hus?", "Har ni barn som byter skola?",
   "Flyttfirma eller själva?"
3. Ett ankardatum.
4. Klart: planen är byggd, de tre första stegen syns på startskärmen. Frågorna gömmer de steg som inte gäller –
   användaren ser aldrig 80 punkter, bara de som är relevanta.

### 3.2 Fånga på vägen
En stor **+**-knapp, alltid nåbar med tummen, med fyra val: **Fota**, **Steg**, **Anteckning**, **Brev**.
- När ett steg bockas av med en foto-uppmaning i mallen ("Fota mätarställningen", "Fota tomma rummet") frågar
  appen en gång: *Vill du spara ett foto?* Ett tryck öppnar kameran.
- **Före/efter** för renovering: ett "Före"-foto blir en mall. När du tar "Efter" visas det gamla fotot som en
  halvgenomskinlig hinna i sökaren, så vinkeln blir samma. Resultatet är en skjutreglage-jämförelse i tidslinjen.
- Röstanteckning med automatisk text (på enheten) för den som inte vill skriva.

### 3.3 Brevtolken – "Jag fattar inte det här brevet"
1. *Brev* → dokumentskannern (ML Kit Document Scanner: rak beskärning, ingen kamerabehörighet behövs).
2. Texten läses på enheten (ML Kit Text Recognition).
3. Imorn hittar avsändare, datum, belopp, OCR/bankgiro och sista dag – deterministiskt och testat.
4. Känns avsändare och brevtyp igen (innehållspaketets **brevtyper**, skrivna av människor) visas en färdig
   förklaring på enkel svenska: *"Det här är ett slutskattebesked. Du ska få tillbaka 3 214 kr. Du behöver inte göra
   något."*
5. Finns Gemini Nano på telefonen kan den dessutom förklara det okända med egna ord – alltid märkt som AI, alltid
   med "kontrollera mot brevet".
6. Ett tryck: **Lägg till som steg** med sista dagen som deadline, i rätt kapitel (eller i *Livet i stort*).
7. *Läs upp* finns alltid.

Brevet stannar på telefonen. Appen svarar aldrig på brevet, betalar aldrig och ger aldrig juridiska råd; den säger
vad brevet verkar vara, vad som brukar behöva göras, och länkar till avsändaren.

### 3.4 Långa trådar – det man glömmer efter ett år
Det farligaste i livsprojekt är inte flyttdagen, utan det som händer långt efter:
- Sälja huset → **deklarera vinsten** (K5/K6) våren året efter, uppskov, dolda fel i upp till tio år.
- Renovera → **garanti och reklamation** för hantverkarens arbete, spara ROT-underlaget, egenkontrollerna för våtrum.
- Barn → namnet ska anmälas inom tre månader, föräldradagar som förfaller vid en viss ålder.
- Dödsbo → bouppteckning, deklaration för dödsboet, arvskifte.

När ett kapitel avslutas frågar Imorn: *"Det finns 3 saker längre fram. Ska jag hålla koll på dem?"* Kapitlet blir
**vilande**, inte borta. Trådarna visas i *Längre fram* på startskärmen när de närmar sig, och kapitlet vaknar med
en lugn notis när det är dags.

**Glömskeskydd** utan tjat:
- Högst en–två notiser per dag; flera påminnelser samma dag blir en.
- *Veckan som kommer* – en notis på söndag kväll med det viktigaste, valfri.
- Ett steg som passerat sitt datum frågar en gång: *Klart? · Imorgon · Nytt datum · Gäller inte*. Sedan tyst.
- Steg med hårda deadlines (märkta i mallen) får en extra påminnelse dagen före – bara de.

### 3.5 Minnen och familjen
- **Minnesboken**: när ett kapitel avslutas (eller när som helst) gör Imorn en vacker sammanfattning – omslag,
  före/efter, de bästa fotona, valda anteckningar, datum. Dela som bildserie eller PDF via vanliga delningen. Inga
  papper och inga kvitton följer med, bara det som är märkt som minne.
- **För ett år sedan**: en gång ibland – "För ett år sedan bar ni in den första lådan i nya huset." Kan stängas av.
- **Familjen** (senare fas, kräver konto): bjud in till ett kapitel som *Läsare* (mormor ser minnena) eller
  *Medhjälpare* (syskonen delar på dödsboets steg). Steg kan tilldelas: "Erik ringer banken".
- **Släktens tidslinje**: kapitel som delas i familjen bildar tillsammans en tidslinje över generationer – flyttar,
  barn, bröllop, hus. Det är Imorns långsiktiga själ: *vardagens projekt blir familjens historia*.
- **Visningslänk för den utan app**: mormor öppnar minnesboken i webbläsaren, utan konto (fas 8).

---

## 4. Livsfaser – rätt mallar till rätt person

Onboardingen frågar inte om ålder, utan *"Vad stämmer på dig just nu?"* (flera val). Livsfasen styr bara vilka
mallar som föreslås först och vilka säsongstips som visas; allt finns alltid tillgängligt i biblioteket.

| Livsfas | Vanligast att vilja göra | Mallar som föreslås först |
| --- | --- | --- |
| **Ny på egen hand** (ca 18–25) | Första lägenheten, plugga, första jobbet, körkort | Första egna boendet, Börja plugga (CSN), Ta körkort, Nytt jobb, Deklarera första gången |
| **Bygga upp** (ca 25–40) | Köpa bostad, flytta ihop, barn, bröllop, renovera, hund | Köpa bostad, Flytta, Vänta barn, Gifta sig, Renovera badrum/kök, Skaffa hund |
| **Mitt i livet** (ca 40–60) | Större boende, renovera, barn flyttar hemifrån, hjälpa föräldrar | Sälja bostad, Renovera, Barnet flyttar hemifrån, Hjälpa en förälder, Byta jobb |
| **Nästa kapitel** (ca 60+) | Gå i pension, flytta till mindre, framtidsfullmakt, testamente | Gå i pension, Flytta till mindre, Framtidsfullmakt och testamente, Rensa och ge bort |
| **Ny i Sverige** | Folkbokföring, personnummer, id, bank, vård | Ny i Sverige (med enkel svenska och fler språk) |
| **Något tungt har hänt** | Separation, en närstående har dött, jobbet försvann | När någon har dött, Separation, Förlora jobbet – med extra varsam ton |

**Säsongstips** (lugna kort, högst ett i veckan): deklarationen på våren, vinterdäck på hösten, se över
försäkringar och abonnemang i januari, rensa förråd på våren. Varje tips kan bli ett steg med ett tryck.

Rekommendationerna är deterministiska regler i innehållspaketet (livsfas × säsong × befintliga kapitel), testade –
ingen modell gissar vad du borde göra.

**Nordiskt klimat:** användaren kan välja sin landsdel (norra, mellersta eller södra Sverige; ingen plats och ingen
behörighet). Säsongsstegen förskjuts efter den: vinterkitet i bilen (filt, varningstriangel, skrapa, startkablar,
mössa och vantar) och vinterdäcken kommer i september i Kiruna och i november i Malmö. Zonerna och förskjutningen är
data i innehållspaketet, testade som resten.

**Vädret** (senare, beslut 8): med en prognos från SMHI:s öppna data kan ett steg som redan finns dyka upp i rätt
stund – "Det blir −18 i natt. Lägg filten i bilen." Vädret väljer bara *när* ett befintligt steg eller tips visas,
aldrig nya steg, och prognosen hämtas för landsdelen eller en ort användaren väljer, aldrig för telefonens plats.

---

## 5. Innehållssystemet – mallar och länkar som lever

Kort version (detaljer i [content-system.md](content-system.md)):

- Mallar, länkregister, brevtyper och säsongstips är **data**, inte kod: JSON i mappen `content/` i repot.
- Varje länk finns **en gång** i länkregistret (`links.json`) och refereras med id från mallarna. Byter
  Skatteverket adress rättas den på ett ställe, och alla användares kapitel får rätt länk utan uppdatering.
- **CI validerar** varje ändring: schema, att alla länk-id finns, att texter finns på svenska och engelska,
  och att alla länkar svarar.
- **En schemalagd länkkontroll** varje vecka öppnar ett issue om en länk har slutat fungera.
- Vid merge till `main` **publiceras ett signerat innehållspaket** till Firebase Hosting (via samma Workload
  Identity Federation som App Distribution använder, inga nycklar). Appen hämtar det med vanlig HTTPS i bakgrunden,
  verifierar signaturen och använder det. Ingen Firebase-SDK och ingen spårning i appen.
- Appen har alltid **ett inbakat paket** i APK:n, så allt fungerar offline och vid första start.
- En mall som uppdateras **ändrar aldrig användarens kapitel** utan lov: länkar uppdateras alltid, nya steg visas
  som "2 nya tips i mallen – lägg till?".
- Ägaren kan redigera mallar och länkar **från mobilen** i GitHubs webbredigerare → PR → grönt → merge → ute hos
  alla inom ett dygn.

---

## 6. Faser

Varje fas slutar med något en riktig människa kan använda. Ordningen är vald så att *Flytta* fungerar helt först,
och att mallformatet är det slutliga redan från början (inbakat), så att fjärruppdateringen i fas 4 bara är ett nytt
sätt att leverera samma data.

### Fas 0 – Grunden *(klar)*
- [x] Gradle med convention plugins, versionskatalog, Isolated Projects
- [x] CI (format och lint, tester med skärmdumpar, bygge och badging), App Distribution via WIF
- [x] Designsystemet *Gryning* (Figtree, `Imorn*`-komponenter, Material 3 bara i `:core:designsystem`)
- [x] Navigation 3, Metro, Gemini Nano-assistent som bara förklarar
- [x] `CLAUDE.md` för Imorn (regler som i Novila: designsystem, inga hårdkodade texter, logik i rena Kotlin-moduler)
- [x] `ArchitectureTest`: features beror inte på varandra, inga Material-importer i features, strängar i resurser

### Fas 1 – Kärnan: kapitel, steg och nu-linjen
**Mål:** någon kan planera och genomföra en hel flytt i Imorn, offline.
- [x] `:core:model` (ren Kotlin): `Chapter`, `Anchor`, `Phase`, `Step`, `Entry`, `RelativeTime`, `LinkRef`,
      planering av relativa datum (`schedule()`), framsteg – enhetstestat
- [x] `:core:content` (ren Kotlin): mallformatet v1, parser, validering, `instantiate(template, answers, anchors)`
- [x] `:core:database`: Room med explicita migrationer, främmande nycklar, testade DAO:er
- [x] Inbakat innehållspaket med mallen **Flytta** och länkregistret
- [x] `:feature:today` blir *Imorgon*: högst tre saker – nästa steg per kapitel, det som är försenat, *Längre fram*
- [x] `:feature:chapters`: skapa kapitel från mall (frågor + ankare), kapitlets översikt, redigera, avsluta, vila, radera
- [x] `:feature:timeline`: Lista och Tidslinje med nu-linje, bocka av, flytta steg, egna steg
- [x] Steg-detalj: beskrivning, länkkort till myndigheten, "varför", anteckning
- [x] Tomma lägen och onboarding light (varför Imorn, livsfas; inget namn)

### Fas 2 – Fånga på vägen
**Mål:** kapitlet blir en berättelse, inte bara en lista.
- [x] Snabb-+ med Fota, Steg, Anteckning (och Papper)
- [x] Foton via Photo Picker och kamera-intent, kopierade till appens privata lagring, miniatyrer
- [x] Foto-uppmaning när ett steg med `photoPrompt` bockas av
- [x] Papper: PDF och bilder som dokument, märkta privata, egen flik i kapitlet
- [ ] Före/efter-par med jämförelsereglage; hinna i sökaren (CameraX, kräver `CAMERA` – ägarens ok)
- [ ] Röstanteckningar med text på enheten (utvärdera)
- [x] Radering som städar filer, och tydligt vad som händer med foton när ett kapitel raderas

### Fas 3 – Påminnelser och långa trådar
**Mål:** inget viktigt glöms, även ett år senare – och ingen känner sig tjatad på.
- [ ] `:core:reminders` med WorkManager (samma mönster som Novila: ett engångsjobb, lägger nästa själv, inga exakta larm)
- [ ] Notisbudget: sammanslagning per dag, tysta timmar, *Imorgon*-knapp i notisen – reglerna är klara och testade
  i `:core:notificationpolicy`; kvar: koppla in dem i `:core:reminders` och *Imorgon*-knappen i notisen
- [ ] Försenat-flödet: Klart · Imorgon · Nytt datum · Gäller inte
- [x] Långa trådar och vilande kapitel; *Längre fram* på startskärmen
- [ ] *Veckan som kommer* (valfri söndagsnotis) – reglerna är klara i `:core:notificationpolicy` (av som standard);
  kvar: koppla in när `:core:reminders` finns
- [x] Widget (Glance): nästa steg + snabbfoto
- [x] Omstart, tidszoner, sommartid och ändrade ankare testade
- [ ] Återkommande steg i `:core:model` och mallformatet (`repeat`: varje år i en månad, eller ankare + N månader);
      ett avbockat steg lägger nästa gång, deterministiskt och testat
- [ ] *Lägg i kalendern* på ett steg: kalenderappen öppnas ifylld (`CalendarContract`, `ACTION_INSERT`), användaren
      sparar själv – ingen behörighet
- [ ] Kalendersynk (beslut 7): Imorn håller stegen i en kalender användaren väljer och flyttar dem när ankaret
      flyttas. Väljs en kalender som redan delas med familjen ser alla stegen, utan konto i Imorn (mellansteg före
      fas 7). Bara skrivning, aldrig läsning av andra händelser

### Fas 4 – Levande innehåll och mallbiblioteket
**Mål:** ägaren uppdaterar mallar och länkar utan ny app; biblioteket täcker livets vanligaste projekt.
- [ ] `content/` i repot, JSON Schema, validering i CI, signering, publicering till Firebase Hosting
- [ ] Veckovis länkkontroll som öppnar issue
- [ ] Appen: hämtning i bakgrunden (WorkManager, ETag, signaturkontroll, reserv till inbakat paket)
- [ ] **Beslut:** `INTERNET`-behörigheten tillbaka, bara för innehållet (ägarens ok, badging, integritetspolicy)
- [x] Mallbibliotek med sök, kategorier och livsfaser
- [ ] Mall-uppdateringar: länkar automatiskt, nya steg som förslag
- [ ] Säsongstips, med landsdelen (klimatzon) som förskjuter höstens och vårens steg
- [ ] Mallen **Bilen** (äga en bil: däckbyten, vinterkit, service, besiktning, skatt och försäkring som
      återkommande steg), se [templates.md](templates.md)
- [ ] **Beslut:** väderprognos från SMHI (kräver `INTERNET`, samma beslut som innehållet), som bara väljer när
      bilens och hemmets vädersteg visas
- [ ] De första 15 mallarna från [templates.md](templates.md), granskade mot källorna (31 inbakade, utan
      `reviewedAt` än)

### Fas 5 – Brevtolken
**Mål:** vem som helst kan fota ett obegripligt brev och veta vad som behöver göras.
- [ ] Dokumentskanner och textigenkänning på enheten
- [ ] `:core:letters` (ren Kotlin): avsändare, datum, belopp, OCR, sista dag – testat med anonymiserade exempel
- [ ] Brevtyper i innehållspaketet (de 30 vanligaste breven från Skatteverket, Försäkringskassan, CSN,
      Kronofogden, Pensionsmyndigheten, Transportstyrelsen, kommunen, vården)
- [ ] Gemini Nano-förklaring som tillägg, med AI-märkning; aldrig beslut, aldrig belopp den hittar på
- [ ] *Läs upp*, *Enkel svenska*, *Lägg till som steg*
- [ ] Integritet: brevbilder krypterade i privat lagring, aldrig i minnesboken, aldrig delade

### Fas 6 – Minnen utan konto
**Mål:** dela det fina utan att dela det privata.
- [ ] Minnesboken: generering, redigering (välj foton), export som bilder/PDF, delningsdialog
- [ ] *För ett år sedan* och kapitlets årsdag
- [ ] Säkerhetskopia: Androids egen backup för databasen; export/import av ett kapitel som fil

### Fas 7 – Familjen
**Mål:** livsprojekt görs sällan ensam.
- [ ] **Beslut:** konto (Firebase Auth med Google och lösenordsnyckel), Firestore + Cloud Storage, region i EU
- [ ] Dela kapitel: Läsare / Medhjälpare / Ägare, inbjudningslänk, återkalla (före kontot räcker en delad kalender,
      fas 3, för att hålla koll tillsammans; fas 6 ger en kopia av kapitlet som fil)
- [ ] Tilldela steg till personer, notis till den som fått steget
- [ ] Separata delningsnivåer: minnen delas, papper aldrig utan uttryckligt val per dokument
- [ ] Synk med offline-först och konfliktregler (sista skrivning per fält; avbockning vinner)
- [ ] Säkerhetsregler testade i CI mot Firebase-emulatorn
- [ ] Släktens tidslinje
- [ ] Radera konto och all molndata; integritetspolicy, Data safety

### Fas 8 – Bredare
- [ ] Fler språk för innehållet (engelska först, sedan arabiska, somaliska, persiska, ukrainska – där behovet av
      brevtolken är störst)
- [ ] Webbvisning av minnesboken för den utan app
- [ ] Surfplatta och vikbara (adaptiva layouter)
- [ ] Mallar från användare? Bara efter granskning, aldrig fritt publicerade

---

## 7. Arkitektur

Samma ryggrad som i dag (Metro, Navigation 3, en ViewModel per skärm, tillståndslösa skärmar). Nya moduler:

| Modul | Typ | Ansvar |
| --- | --- | --- |
| `:core:model` | ren Kotlin | Kapitel, steg, ankare, relativ tid, framsteg |
| `:core:notificationpolicy` | ren Kotlin | Notisbudget: sammanslagning per dag, tysta timmar, *Veckan som kommer* |
| `:core:content` | ren Kotlin | Mallformat, länkregister, brevtyper, validering, mallinstansiering, versionsjämförelse |
| `:core:letters` | ren Kotlin | Utvinning ur brevtext, matchning mot brevtyper |
| `:core:database` | Android | Room, DAO:er, migrationer, repositories |
| `:core:media` | Android | Foton, miniatyrer, privat lagring, städning |
| `:core:reminders` | Android | WorkManager, notiser, kanaler |
| `:core:calendar` | Android | *Lägg i kalendern* och kalendersynk (fas 3) |
| `:core:weather` | ren Kotlin + Android | Klimatzonerna (ren Kotlin) och SMHI-prognosen (fas 4, efter beslut 8) |
| `:core:contentsync` | Android | Hämta och verifiera innehållspaket (fas 4) |
| `:core:capture` | Android | Dokumentskanner, textigenkänning (fas 5) |
| `:feature:today` | feature | *Imorgon* |
| `:feature:chapters` | feature | Skapa, översikt, avsluta |
| `:feature:timeline` | feature | Lista, tidslinje, steg-detalj |
| `:feature:library` | feature | Mallbiblioteket |
| `:feature:letters` | feature | Brevtolken |
| `:feature:memories` | feature | Minnesboken, för ett år sedan |
| `:feature:settings`, `:feature:onboarding`, `:feature:widget` | feature | Som i Novila |

Regler som följer med från Novila:
- All logik som går att testa utan Android ligger i rena Kotlin-moduler och testas på sekunder.
- Planen är alltid deterministisk. Gemini Nano **förklarar** (assistentens kommentar, brevets innebörd) men
  **bestämmer aldrig** steg, datum eller belopp.
- Inga hårdkodade texter; mallarnas texter kommer ur innehållspaketet, appens egna ur `strings.xml`.
- Nya behörigheter (`INTERNET`, `CAMERA`, `POST_NOTIFICATIONS`, `WRITE_CALENDAR`) kräver ägarens ok och syns i badging.

---

## 8. Beslut som behövs från ägaren

| # | Beslut | Rekommendation | När |
| --- | --- | --- | --- |
| 1 | `INTERNET` tillbaka för innehållspaketet | Ja, men bara HTTPS till en egen domän, inga SDK:er. Användaren kan stänga av uppdateringar. | Fas 4 |
| 2 | Var paketet ligger | Firebase Hosting i `imorn-app` (CDN, gratis nivå räcker länge, WIF finns redan) | Fas 4 |
| 3 | `CAMERA` för före/efter-hinnan | Ja, frågas först när användaren tar ett efter-foto | Fas 2 |
| 4 | Konto och moln för familjen | Firebase Auth + Firestore + Storage i EU-region; utvärdera ändpunktskryptering för papper | Fas 7 |
| 5 | Juridisk ton | Mallar är "vanliga steg", aldrig "allt du måste". Fast text: *Imorn ersätter inte rådgivning.* | Fas 1 |
| 6 | Vem granskar innehållet | Ägaren + en checklista per mall (källa, datum, granskad av); `reviewedAt` i varje mall | Fas 4 |
| 7 | `WRITE_CALENDAR` för kalendersynk | Ja, frågas först när användaren slår på synken; aldrig `READ_CALENDAR`. *Lägg i kalendern* kommer först och behöver ingen behörighet | Fas 3 |
| 8 | Väderprognos | SMHI:s öppna data över samma `INTERNET` som innehållet, för landsdelen eller en vald ort, aldrig `ACCESS_*_LOCATION`. Klimatzonerna utan prognos kommer först | Fas 4 |

---

## 9. Risker

- **Fel information** (en regel ändras): varje steg med en regel länkar till källan, mallar har `reviewedAt`,
  länkkontrollen larmar, och fjärruppdatering gör att rättelser når ut på ett dygn.
- **Länkröta**: länkregister + veckovis kontroll + rotdomän som reserv om en djuplänk dör.
- **Notiströtthet**: notisbudget, sammanslagning, ingen notis som inte kan bli ett ett-tryck-svar.
- **Gemini Nano finns inte på alla telefoner**: allt fungerar utan; brevtyperna är skrivna av människor.
- **Känsliga data** (personnummer i brev, dödsbo): lokalt, krypterat, aldrig i delning som standard.
- **Andras bilder i familjedelning**: tydligt vem som ser vad, och den som laddat upp kan alltid ta bort.
- **Omfång**: varje mall ska vara bra, inte heltäckande. Hellre 15 utmärkta än 60 halvbra.

---

## 10. Hur vi vet att det fungerar

- **Nordstjärna:** kapitel som når sitt ankardatum utan en missad hård deadline.
- Andel avbockade steg som får ett foto eller en anteckning (minnen på köpet).
- Hur många som kommer tillbaka till ett vilande kapitel när en lång tråd vaknar.
- Brev som blir ett steg efter tolkningen.
- Delade minnesböcker.
- Kvalitativt: "Jag kände att jag hade koll" i betatesternas svar.

Allt mäts bara via egna betatester och frivillig feedback tills ägaren beslutar annat – ingen analys-SDK.

---

## Definition of done (varje PR)
- Kriterierna i fasen uppfyllda, logik enhetstestad, skärmar med previews och skärmdumpar
- Laddning, tomt läge och fel hanterade; data överlever omstart
- Behörigheter och integritet granskade; tillgänglighet (TalkBack, stor text, kontrast) kontrollerad
- Inga hemligheter i repot, dokumentationen uppdaterad, CI grönt
