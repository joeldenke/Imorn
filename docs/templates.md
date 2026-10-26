# Mallkatalogen

De första mallarna, som underlag för `content/templates/*.json`. Varje mall har frågor som gömmer det som inte
gäller, faser med steg i relativ tid, länkar ur länkregistret, foto-uppmaningar och långa trådar.

> Fakta om regler och frister här ska kontrolleras mot källan innan mallen publiceras (`reviewedAt`). Belopp,
> procentsatser och tak skrivs aldrig in i stegen – de ändras, och länken till källan gäller.
> Mallar beskriver vanliga steg, aldrig "allt du måste göra".

**I appen:** 1–6 och 8–13 och 15 finns i `content/templates/` (12 *Nytt jobb / förlora jobbet* heter `job`, en fråga
väljer vilket). 7, 14 och 16 återstår. Ingen mall har `reviewedAt` än.

Förkortningar: **T** = ankardatum (flyttdag, tillträde, förlossning …). `T−6v` = sex veckor före.

---

## Länkregistret (första versionen)

Länkarna pekar på startsidan tills djuplänkarna är verifierade; `searchHint` säger vad man söker på där.

| Id | Organisation | Domän | Sök på |
| --- | --- | --- | --- |
| `skatteverket.start` | Skatteverket | skatteverket.se | – |
| `skatteverket.flytt` | Skatteverket | skatteverket.se | flyttanmälan |
| `skatteverket.rot` | Skatteverket | skatteverket.se | rot och rut |
| `skatteverket.bostad` | Skatteverket | skatteverket.se | sälja bostad, K5, K6, uppskov |
| `skatteverket.hindersprovning` | Skatteverket | skatteverket.se | hindersprövning |
| `skatteverket.barn` | Skatteverket | skatteverket.se | namn på barn, föräldraskap |
| `skatteverket.dodsfall` | Skatteverket | skatteverket.se | bouppteckning, dödsbo |
| `adressandring.eftersandning` | Svensk Adressändring | adressandring.se | eftersändning |
| `fk.start` | Försäkringskassan | forsakringskassan.se | föräldrapenning, bostadsbidrag |
| `csn.start` | CSN | csn.se | – |
| `lantmateriet.lagfart` | Lantmäteriet | lantmateriet.se | lagfart |
| `lantmateriet.pantbrev` | Lantmäteriet | lantmateriet.se | pantbrev |
| `transportstyrelsen.start` | Transportstyrelsen | transportstyrelsen.se | ägarbyte, körkort |
| `polisen.pass` | Polisen | polisen.se | pass och nationellt id-kort |
| `pensionsmyndigheten.start` | Pensionsmyndigheten | pensionsmyndigheten.se | ta ut pension |
| `minpension.start` | minPension | minpension.se | – |
| `kronofogden.start` | Kronofogden | kronofogden.se | – |
| `konsumentverket.hallakonsument` | Hallå konsument | hallakonsument.se | – |
| `arn.start` | Allmänna reklamationsnämnden | arn.se | – |
| `energimarknadsbyran.start` | Energimarknadsbyrån | energimarknadsbyran.se | byta elavtal |
| `elpriskollen.start` | Energimarknadsinspektionen | elpriskollen.se | – |
| `konsumenternas.start` | Konsumenternas Bank- och finansbyrå / Försäkringsbyrå | konsumenternas.se | hemförsäkring, bolån |
| `fmi.start` | Fastighetsmäklarinspektionen | fmi.se | anlita mäklare |
| `fi.bolan` | Finansinspektionen | fi.se | bolånetak och amorteringskrav |
| `boverket.start` | Boverket | boverket.se | bygglov |
| `elsakerhetsverket.start` | Elsäkerhetsverket | elsakerhetsverket.se | elinstallatör, behörighet |
| `sakervatten.start` | Säker Vatten | sakervatten.se | våtrum, auktoriserat företag |
| `verksamt.start` | verksamt.se | verksamt.se | starta företag |
| `arbetsformedlingen.start` | Arbetsförmedlingen | arbetsformedlingen.se | – |
| `akassan.start` | Sveriges a-kassor | sverigesakassor.se | – |
| `1177.start` | 1177 | 1177.se | – |
| `domstol.start` | Sveriges Domstolar | domstol.se | bodelning, äktenskapsskillnad |
| `kommun.start` | Din kommun | (räknas ut ur kommunnamnet) | – |
| `krisinformation.start` | Krisinformation.se | krisinformation.se | – |
| `minameddelanden.start` | Mina meddelanden | minameddelanden.se | digital brevlåda |

---

## 1. Flytta
**Livsfaser:** alla. **Ankare:** Flyttdag (T); vid köp är det tillträdesdagen.
**Frågor:** Vad flyttar du från (hyresrätt, bostadsrätt, hus, annat)? Vad flyttar du till (hyresrätt, en bostadsrätt
du köper, ett hus du köper, annat)? Säljer du din nuvarande bostad? Byter barnen skola eller förskola? Bostadsbidrag
eller studiemedel?

Faserna är **delflöden**: varje del av flytten som hänger ihop, med egna steg i rätt ordning. Delflöden som inte gäller
(inget köp, ingen försäljning) försvinner av svaren; en obesvarad fråga gömmer ingenting.

| Delflöde | Steg | När | Länkar |
| --- | --- | --- | --- |
| Planera | Säg upp hyreskontraktet *(om hyresrätt)* | T−3 mån | – |
| | Boka flyttfirma eller släp, be om två–tre offerter | T−8v | `konsumentverket.hallakonsument` |
| | Ansök om plats i ny skola/förskola *(om barn)* | T−8v | `kommun.start` |
| | Boka ledigt för flyttdagen | T−6v | – |
| Mäklaren och försäljningen *(om du säljer)* | Välj mäklare, jämför minst två | T−5 mån | `fmi.start` |
| | Gör i ordning bostaden inför foto och visning | T−4 mån | – |
| | Visningar och budgivning | T−15v | – |
| | Skriv köpekontrakt med köparen | T−3 mån | – |
| | Be banken lösa det gamla lånet vid tillträdet | T−4v | – |
| Bolån och kontantinsats *(om du köper)* | Räkna på kontantinsatsen och månadskostnaden | T−5 mån | `fi.bolan`, `konsumenternas.start` |
| | Jämför bolån hos flera banker | T−4 mån | `konsumenternas.start` |
| | Skaffa lånelöfte | T−4 mån | `konsumenternas.start` |
| | Betala handpenningen när köpekontraktet skrivs | T−11v | – |
| Bankens steg *(om du köper)* | Skicka köpekontraktet till banken och ansök om lånet | T−10v | – |
| | Få medlemskap i bostadsrättsföreningen *(bostadsrätt)* | T−4v | – |
| | Låt banken ordna pantbrev eller pantsättning | T−3v | `lantmateriet.pantbrev` |
| | Signera lånehandlingarna | T−2v | – |
| | Tillträdet: lånet betalas ut, du får nycklarna **[foto]** | T | – |
| Abonnemang: säg upp och teckna nytt | Teckna elavtal för nya adressen | T−4v | `energimarknadsbyran.start`, `elpriskollen.start` |
| | Säg upp elavtalet på gamla adressen | T−4v | `energimarknadsbyran.start` |
| | Flytta bredband och säg upp det gamla | T−4v | – |
| | Flytta eller teckna hemförsäkring – gäller den under själva flytten? | T−3v | `konsumenternas.start` |
| | Teckna villaförsäkring från tillträdet *(köper hus)* | T−3v | `konsumenternas.start` |
| | Anmäl ny ägare för vatten, avlopp och sophämtning *(köper hus)* | T−2v | `kommun.start` |
| | Avsluta vatten och sophämtning för gamla huset *(från hus)* | T−2v | `kommun.start` |
| | Gå igenom resten: larm, parkering, gym, tidningar, streaming | T−2v | – |
| Papper | Gör flyttanmälan (tidigast 4 v före, senast 1 v efter) | T−14d, hård T+7d | `skatteverket.flytt` |
| | Beställ eftersändning *(valfritt)* | T−14d | `adressandring.eftersandning` |
| | Bostadsbidrag eller CSN: anmäl ny adress och hyra *(om aktuellt)* | T | `fk.start`, `csn.start` |
| | Byt adress hos bank, vård, tandvård, jobb, föreningar, prenumerationer | T+7d | – |
| Packa | Rensa – sälj, skänk, släng | T−6v | `kommun.start` (återvinning) |
| | Packlådor, tejp, märkpenna | T−4v | – |
| | Packa det du inte behöver | T−3v | – |
| | Packa en "första natten"-väska | T−2d | – |
| Flyttdagen | Läs av el- och vattenmätare **[foto]** | T | – |
| | Fota tomma rummen i gamla bostaden **[foto]** | T | – |
| | Lämna nycklarna, få kvittens **[foto]** | T | – |
| Städa och besiktning | Flyttstäda (checklista: ugn, kyl, fönster, golvbrunnar) | T−1d | – |
| | Avflyttningsbesiktning *(hyresrätt)* | T±0 | – |
| Landa | Testa brandvarnaren, leta upp huvudkranen och elcentralen **[foto]** | T+1d | – |
| | Sätt namnet på dörren och brevlådan | T+1d | – |
| | Första frukosten i nya hemmet **[minne]** | T+1d | – |

**Långa trådar:** Kolla att depositionen kommit tillbaka (T+1 mån). Se över elavtalet efter första fakturan (T+2 mån).
Kolla att lagfarten är beviljad *(köper hus, T+2 mån)*. Deklarera försäljningen *(säljer, mars året efter)*.

Belopp, procentsatser och tak (kontantinsats, amorteringskrav) står aldrig i stegen: länken till källan gäller.
Mallarna *Sälja bostad* och *Köpa bostad* nedan går djupare, för den som bara säljer eller köper.

---

## 2. Sälja bostad
**Ankare:** Kontraktsdag (K), Tillträde (T). **Frågor:** Bostadsrätt eller hus? Mäklare? Köper ni nytt samtidigt?

| Fas | Steg | När | Länkar |
| --- | --- | --- | --- |
| Förbereda | Välj mäklare – jämför arvode och avtal; kolla att mäklaren är registrerad | – | `fmi.start` |
| | Ta fram papper: köpekontrakt från köpet, kvitton på förbättringar (sparas för vinstberäkningen) | – | `skatteverket.bostad` |
| | Fixa det lilla, städa, fotografering **[foto: före försäljningen]** | – | – |
| Visning och budgivning | Visningar, budgivning, välj köpare | – | – |
| | Skriv köpekontrakt; handpenning | K | – |
| Till tillträdet | Bostadsrätt: köparen godkänns av föreningen | K+2v | – |
| | Säg upp el, bredband, försäkring från tillträdet | T−2v | – |
| | Gör flyttanmälan *(öppnar mallen Flytta)* | – | `skatteverket.flytt` |
| | Flyttstäda, lämna nycklar, läs av mätare **[foto]** | T | – |
| Efteråt | Spara allt: kontrakt, likvidavräkning, mäklarens arvode | T | – |

**Långa trådar (det som glöms):**
- **Deklarera försäljningen** – K5 (småhus) eller K6 (bostadsrätt) i deklarationen våren året efter det år
  kontraktet skrevs. Påminnelse i mars, igen i april. `skatteverket.bostad`
- **Uppskov** om ni köper ny bostad – kolla villkoren innan deklarationen. `skatteverket.bostad`
- **Dolda fel**: köparen kan höra av sig långt efteråt (preskription normalt tio år från tillträdet). Spara
  besiktningsprotokoll och papper. Tråd: "Papperen från husförsäljningen" ligger kvar i tio år.

---

## 3. Köpa bostad
**Ankare:** Kontraktsdag, Tillträde. **Frågor:** Bostadsrätt eller hus? Första bostaden?

Steg i korthet: lånelöfte (`konsumenternas.start`) · bevaka och gå på visningar **[foto: varje visning, med
anteckning]** · läs föreningens årsredovisning *(bostadsrätt)* · överlåtelsebesiktning *(hus)* · budgivning ·
kontrakt och handpenning · bolån och amortering · hemförsäkring från tillträdet (villaförsäkring för hus) · lagfart
inom tre månader *(hus, ofta via banken)* `lantmateriet.lagfart` · tillträde **[minne: nycklarna]** · öppnar
*Flytta*.

**Långa trådar:** Kolla att lagfarten är beviljad (T+3 mån). Ränte- och bindningstid löper ut (valfritt datum).

---

## 4. Renovera (kök eller badrum)
**Ankare:** Start, Klart. **Frågor:** Kök eller badrum? Hantverkare eller själv? Bostadsrätt (fråga föreningen)?

| Fas | Steg | Länkar |
| --- | --- | --- |
| Planera | **Före-foton** av varje vägg och vinkel **[före/efter]** | – |
| | Bostadsrätt: fråga föreningen om tillstånd | – |
| | Behövs bygglov eller anmälan? | `boverket.start`, `kommun.start` |
| | Budget och tidsplan | – |
| Hantverkare | Två–tre offerter, skriftligt avtal, kolla F-skatt och omdömen | `konsumentverket.hallakonsument` |
| | El: behörig elinstallatör | `elsakerhetsverket.start` |
| | Våtrum: auktoriserat företag, spara intyget | `sakervatten.start` |
| | ROT-avdrag: kolla villkoren | `skatteverket.rot` |
| Under arbetet | Foto varje dag/vecka **[foto]**, särskilt det som byggs in (rör, tätskikt, el) | – |
| | Ändringar skriftligt – ÄTA-arbeten | – |
| Klart | Slutbesiktning/genomgång, lista anmärkningar | – |
| | **Efter-foton** med hinnan från före-fotot **[före/efter]** | – |
| | Spara: avtal, fakturor, intyg, foton på dolda installationer | – |

**Långa trådar:** Reklamation och garanti – påminnelse inför garantitidens slut (enligt avtalet); kolla att
ROT-avdraget syns i deklarationen (våren därpå). Fel upptäckta senare: `arn.start`, `konsumentverket.hallakonsument`.

---

## 5. Vänta barn
**Ankare:** Beräknad förlossning (BF), sedan Födelsedag (F). **Frågor:** Gifta? Första barnet?

Före: barnmorskemottagning (`1177.start`) · föräldraledigheten till arbetsgivaren (i god tid) · bekräfta
föräldraskap *(om ni inte är gifta)* `skatteverket.barn` · planera föräldrapenningen `fk.start` · bilbarnstol,
vagn, säng · packa BB-väskan (BF−4v) · **[minne: magen, vecka för vecka]**.

Efter: anmäl barnets namn **senast tre månader efter födseln** (hård deadline) `skatteverket.barn` · ansök om
föräldrapenning `fk.start` · barnbidraget kommer automatiskt · BVC (`1177.start`) · pass om ni ska resa
`polisen.pass` · **[minne: första dagen hemma]**.

**Långa trådar:** förskoleansökan (`kommun.start`, ofta flera månader innan önskad start), föräldradagar som
förfaller vid en viss ålder (`fk.start`), första tanden, första steget – minnesuppmaningar som går att stänga av.

---

## 6. Gifta sig
**Ankare:** Bröllopsdag. Steg: hindersprövning (giltig en begränsad tid – kolla när den ska göras) `skatteverket.hindersprovning`
· vigselförrättare · lokal, gäster, budget · namnbyte efteråt `skatteverket.start` · äktenskapsförord? (registreras
hos Skatteverket) · **[minne: dagen]**. Lång tråd: byt namn på pass/id, bank, jobb.

---

## 7. När någon har dött
**Varsam ton. Inga minnesuppmaningar om användaren inte själv slår på dem.**
**Ankare:** Dödsdag (D). **Frågor:** Var personen gift? Finns testamente? Ägde personen bostad?

Första veckorna: kontakta begravningsbyrå eller ordna själv · meddela bank, försäkringar, arbetsgivare, pension
(`pensionsmyndigheten.start`) · bevaka post (`adressandring.eftersandning`) · säga upp abonnemang.

Bouppteckning: förrättning inom tre månader från dödsfallet (anstånd går att söka), ges in till Skatteverket inom
en månad efter förrättningen `skatteverket.dodsfall` · dödsboanmälan om tillgångarna är små `skatteverket.dodsfall`.

**Långa trådar:** deklaration för den döde och för dödsboet (våren därpå) · arvskifte · försäljning av bostad
(öppnar *Sälja bostad*) · avsluta dödsboet. **Delning:** syskon som *Medhjälpare*, steg tilldelas per person.

---

## 8. Första egna boendet
För *Ny på egen hand*. Ställ dig i bostadskö (`kommun.start`) · förstå hyreskontraktet · hemförsäkring
(`konsumenternas.start`) · elavtal (`energimarknadsbyran.start`) · flyttanmälan · bredband · basutrustning (lista) ·
bostadsbidrag? (`fk.start`) · budget · **[minne: första kvällen]**. Lång tråd: deklarera första gången (våren).

## 9. Börja plugga
Ansök till utbildningen · CSN-ansökan i god tid (`csn.start`) · bostad (öppnar *Första egna boendet*) · terminsstart.
Långa trådar: studieresultat till CSN, ny ansökan inför varje läsår, återbetalning efter studierna.

## 10. Ta körkort
Körkortstillstånd med syntest (`transportstyrelsen.start`) · handledarutbildning *(om övningskörning privat)* ·
riskettan och risktvåan · övningskörning **[minne]** · teoriprov · uppkörning.

## 11. Köpa eller sälja bil
Kolla fordonets historik och skulder (`transportstyrelsen.start`) · provkörning · köpeavtal · ägarbyte samma dag ·
försäkring från samma dag. Säsongstips: vinterdäck vid vinterväglag 1 dec–31 mars.

## 12. Nytt jobb / förlora jobbet
Nytt: anställningsavtal, skattetabell, pension och försäkringar via jobbet (`minpension.start`).
Förlora: anmäl dig hos Arbetsförmedlingen första dagen utan jobb (`arbetsformedlingen.start`), a-kassan
(`akassan.start`), budget, se över utgifter.

## 13. Gå i pension
**Ankare:** Önskad första pensionsmånad. Överblick (`minpension.start`) · ansök i god tid, flera månader innan
(`pensionsmyndigheten.start`) · tjänstepension och privat pension · bostadstillägg? · säg till arbetsgivaren ·
**[minne: sista arbetsdagen]**. Följ upp med *Flytta till mindre* eller *Framtidsfullmakt och testamente*.

## 14. Framtidsfullmakt och testamente
Vad är vad (texter i appen, länk till källan) · prata med familjen · skriv framtidsfullmakt · testamente med
vittnen · förvara originalen säkert **[foto: var de ligger]** · berätta för någon var de finns.

## 15. Ny i Sverige
Enkel svenska och fler språk. Folkbokföring och personnummer (`skatteverket.start`) · id-kort (`skatteverket.start`) ·
bankkonto · BankID · vård (`1177.start`) · Mina meddelanden (`minameddelanden.start`) · Försäkringskassan (`fk.start`)
· svenska för invandrare (`kommun.start`). Brevtolken lyfts fram i den här mallen.

## 16. Bilen
Att äga en bil, år efter år. Ett vilande kapitel som vaknar när det är dags; stegen är återkommande (se
`ROADMAP.md`, fas 3) och höstens och vårens förskjuts efter landsdelen.
Frågor: *Byter du däck själv eller på verkstad?* · *Har bilen ett serviceavtal?* Ankare: **B** = besiktning senast
(står på Transportstyrelsens sida och i brevet), **S** = nästa service (okänt är okej).

| Delflöde | Steg | När | Länkar |
| --- | --- | --- | --- |
| Hösten | Boka däckbytet *(om verkstad)* | varje år, sep (norr) – okt (söder) | – |
| | Byt till vinterdäck, kolla mönsterdjupet **[foto]** | varje år, okt (norr) – nov (söder) | `transportstyrelsen.start` |
| | Lägg vinterkitet i bilen: filt, varningstriangel, skrapa, startkablar, mössa och vantar | varje år, med däcken | `krisinformation.start` |
| | Fyll på spolarvätska för vintern, kolla batteriet | varje år, med däcken | – |
| Våren | Byt till sommardäck, tvätta bort saltet | varje år, apr (söder) – maj (norr) | – |
| Underhållet | Service | S, sedan varje år | – |
| | Besiktning | B−4v, sedan varje år | `transportstyrelsen.start` |
| | Fordonsskatten, se över försäkringen | varje år | `transportstyrelsen.start`, `konsumenternas.start` |

Vinterdäck krävs vid vinterväglag 1 dec–31 mars; tidsgränserna och mönsterdjupet länkas, de skrivs inte in.
Med väderprognosen (beslut 8) kan vinterkitet komma tillbaka vid den första riktigt kalla natten.

---

## Senare
Skilsmässa och separation (bodelning, `domstol.se`) · Skaffa hund · Starta företag (`verksamt.start`) · Barnet
flyttar hemifrån · Hjälpa en förälder (hemtjänst, `kommun.start`) · Flytta till mindre · Stor resa · Bygga hus ·
Årets ekonomi (deklaration, försäkringar, abonnemang) · Krisberedskap hemma (`krisinformation.start`) · Hemmet
(återkommande: brandvarnare, sotning, hängrännor, vattenavstängning inför vintern).
