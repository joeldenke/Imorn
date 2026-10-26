# Mallkatalogen

Mallarna är data i `content/templates/*.json`: frågor som gömmer det som inte gäller, faser med steg i
relativ tid, länkar ur länkregistret, foto-uppmaningar och långa trådar. Appens galleri visar dem per
kategori, föreslår dem som passar användarens livsfaser först och låter användaren söka på titel,
beskrivning och nytta.

> Fakta om regler och frister ska kontrolleras mot källan innan mallen publiceras (`reviewedAt`). Belopp,
> procentsatser och tak skrivs aldrig in i stegen – de ändras, och länken till källan gäller. Mallar beskriver
> vanliga steg, aldrig "allt du måste göra".

**I appen:** alla 31 mallar finns i `content/templates/`. *Nytt jobb / förlora jobbet* heter `job`; en fråga väljer
vilket. Ingen mall har `reviewedAt` än.

Förkortningar: **T** = ankardatum (flyttdag, tillträde, förlossning …). `T−6v` = sex veckor före.

Biblioteket har **31 mallar**. `move` är den stora flyttmallen; de andra är medvetet kortare.

## Kategorier

| Kategori | `category` | Mallar |
| --- | --- | --- |
| Hemmet | `home` | `buy-home`, `declutter`, `first-home`, `move`, `renovate`, `sell-home` |
| Familjen | `family` | `adoption`, `child-moving-out`, `baby`, `pet`, `wedding` |
| Arbete och studier | `work-study` | `drivers-license`, `graduation`, `job-loss`, `job`, `start-business`, `study` |
| Hälsa | `health` | `caring-for-relative`, `health-treatment` |
| Livshändelser | `life-events` | `bereavement`, `divorce`, `funeral`, `move-abroad`, `new-in-sweden`, `retire`, `travel` |
| Ekonomi och vardag | `finance-admin` | `car`, `major-purchase`, `personal-finance`, `tax-year`, `will` |

## Mallarna

### Hemmet

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `buy-home` | Köpa bostad | Buying a home | Tillträdesdag | 14 | 2 |
| `declutter` | Rensa hemma | Decluttering | Måldag | 8 | 0 |
| `first-home` | Första egna hemmet | Your first home | Inflyttningsdag | 12 | 2 |
| `move` | Flytta | Moving | Flyttdag | 42 | 5 |
| `renovate` | Renovera | Renovating | Startdag | 15 | 3 |
| `sell-home` | Sälja bostad | Selling a home | Kontraktsdag | 14 | 2 |

### Familjen

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `adoption` | Adoptera | Adopting | Hemkomst | 8 | 0 |
| `child-moving-out` | Barn flyttar ut | A child moving out | Flyttdag | 8 | 0 |
| `baby` | Vänta barn | Expecting a baby | Beräknad förlossning | 12 | 2 |
| `pet` | Skaffa husdjur | Getting a pet | Inflyttningsdag | 9 | 1 |
| `wedding` | Gifta sig | Getting married | Bröllopsdag | 9 | 2 |

### Arbete och studier

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `drivers-license` | Ta körkort | Getting a driving licence | Uppkörning | 8 | 1 |
| `graduation` | Ta examen | Graduating | Examensdag | 9 | 0 |
| `job-loss` | Förlora jobbet | Losing your job | Sista arbetsdagen | 8 | 0 |
| `job` | Nytt jobb | A new job | Första arbetsdagen | 8 | 1 |
| `start-business` | Starta företag | Starting a business | Startdag | 10 | 0 |
| `study` | Börja studera | Starting to study | Terminsstart | 7 | 2 |

### Hälsa

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `caring-for-relative` | Hjälpa en närstående | Caring for a relative | Startdag | 9 | 0 |
| `health-treatment` | Behandling | Treatment | Behandlingsstart | 9 | 0 |

### Livshändelser

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `bereavement` | Någon har dött | Someone has died | Dödsdagen | 8 | 0 |
| `divorce` | Skilja sig | Divorcing | Beslutsdag | 9 | 1 |
| `funeral` | Begravning | A funeral | Begravningsdag | 9 | 0 |
| `move-abroad` | Flytta utomlands | Moving abroad | Avresedag | 11 | 0 |
| `new-in-sweden` | Ny i Sverige | New in Sweden | Ankomstdag | 10 | 1 |
| `retire` | Gå i pension | Retiring | Pensionsdag | 8 | 1 |
| `travel` | Resa | Travelling | Avresedag | 10 | 0 |

### Ekonomi och vardag

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `car` | Köpa bil | Buying a car | Hämtningsdag | 9 | 1 |
| `major-purchase` | Större köp | A major purchase | Köpdag | 8 | 0 |
| `personal-finance` | Privatekonomi | Personal finances | Genomgångsdag | 8 | 0 |
| `tax-year` | Deklaration | Tax return | Deklarationsdag | 9 | 0 |
| `will` | Testamente | A will | Signeringsdag | 9 | 0 |

## Länkregistret

Länkarna pekar på startsidor tills djuplänkarna är verifierade; `searchHint` säger vad man söker på där.
Länkkortet i appen visar alltid organisation och domän.

| Id | Organisation | Domän | Sök på |
| --- | --- | --- | --- |
| `1177.start` | 1177 | www.1177.se | – |
| `adressandring.eftersandning` | Svensk Adressändring | www.adressandring.se | eftersändning |
| `akassan.start` | Sveriges a-kassor | www.sverigesakassor.se | – |
| `arbetsformedlingen.start` | Arbetsförmedlingen | www.arbetsformedlingen.se | – |
| `arn.start` | Allmänna reklamationsnämnden | www.arn.se | – |
| `bolagsverket.start` | Bolagsverket | www.bolagsverket.se | registrera företag, årsredovisning |
| `boverket.start` | Boverket | www.boverket.se | bygglov |
| `csn.start` | CSN | www.csn.se | – |
| `domstol.start` | Sveriges Domstolar | www.domstol.se | bodelning, äktenskapsskillnad |
| `elpriskollen.start` | Energimarknadsinspektionen | www.elpriskollen.se | – |
| `elsakerhetsverket.start` | Elsäkerhetsverket | www.elsakerhetsverket.se | elinstallatör, behörighet |
| `energimarknadsbyran.start` | Energimarknadsbyrån | www.energimarknadsbyran.se | byta elavtal |
| `fi.bolan` | Finansinspektionen | www.fi.se | bolånetak och amorteringskrav |
| `fk.start` | Försäkringskassan | www.forsakringskassan.se | föräldrapenning, bostadsbidrag |
| `fmi.start` | Fastighetsmäklarinspektionen | www.fmi.se | anlita mäklare |
| `jordbruksverket.start` | Jordbruksverket | www.jordbruksverket.se | djurhållning, märkning av djur |
| `kommun.start` | Din kommun | (räknas ut ur kommunnamnet) | – |
| `konsumenternas.start` | Konsumenternas | www.konsumenternas.se | hemförsäkring, bolån |
| `konsumentverket.hallakonsument` | Hallå konsument | www.hallakonsument.se | – |
| `krisinformation.start` | Krisinformation.se | www.krisinformation.se | – |
| `kronofogden.start` | Kronofogden | www.kronofogden.se | – |
| `lantmateriet.lagfart` | Lantmäteriet | www.lantmateriet.se | lagfart |
| `lantmateriet.pantbrev` | Lantmäteriet | www.lantmateriet.se | pantbrev |
| `migrationsverket.start` | Migrationsverket | www.migrationsverket.se | uppehållstillstånd, medborgarskap |
| `minameddelanden.start` | Mina meddelanden | www.minameddelanden.se | digital brevlåda |
| `minpension.start` | minPension | www.minpension.se | – |
| `pensionsmyndigheten.start` | Pensionsmyndigheten | www.pensionsmyndigheten.se | ta ut pension |
| `polisen.pass` | Polisen | www.polisen.se | pass och nationellt id-kort |
| `sakervatten.start` | Säker Vatten | www.sakervatten.se | våtrum, auktoriserat företag |
| `skatteverket.barn` | Skatteverket | www.skatteverket.se | namn på barn, föräldraskap |
| `skatteverket.bostad` | Skatteverket | www.skatteverket.se | sälja bostad, K5, K6, uppskov |
| `skatteverket.dodsfall` | Skatteverket | www.skatteverket.se | bouppteckning, dödsbo |
| `skatteverket.flytt` | Skatteverket | www.skatteverket.se | flyttanmälan |
| `skatteverket.hindersprovning` | Skatteverket | www.skatteverket.se | hindersprövning |
| `skatteverket.rot` | Skatteverket | www.skatteverket.se | rot och rut |
| `skatteverket.start` | Skatteverket | www.skatteverket.se | – |
| `socialstyrelsen.start` | Socialstyrelsen | www.socialstyrelsen.se | adoption, vård, socialtjänst |
| `trafikverket.start` | Trafikverket | www.trafikverket.se | vägar, tåg, trafik |
| `transportstyrelsen.start` | Transportstyrelsen | www.transportstyrelsen.se | ägarbyte, körkort |
| `ud.start` | Utrikesdepartementet | www.regeringen.se | reseråd, utlandsmyndigheter |
| `verksamt.start` | verksamt.se | www.verksamt.se | starta företag |

## Så hänger en mall ihop

- `summary`, `category` och `helps` är galleriets texter: en rad om vad mallen täcker, hyllan den hör till och
  vad den hjälper med. `note` är en varsam notering som bara visas när den behövs (till exempel juridik eller sorg).
- `order` och `icon` ger mallen sin plats och sitt märke i biblioteket.
- `anchors` är datumen allt räknas från. Ett ankare får vara okänt; då får stegen inga datum men behåller
  mallens ordning.
- `questions` är val (`options`) eller ja/nej (`bool`). Ett obesvarat villkor gömmer ingenting.
- `phases` blir delflöden i listan. `longThreads` ligger under *Längre fram* och följer med när kapitlet vilar.
- `retired` behålls för evigt: användarens kapitel refererar till steg-id, inte till radnummer.

## Nästa mallar

### Bilen
Att äga en bil, år efter år. Ett vilande kapitel som vaknar när det är dags; stegen är återkommande (se
`ROADMAP.md`, fas 3) och höstens och vårens förskjuts efter landsdelen. `car` i katalogen är *Köpa bil*; den här
mallen handlar om att äga den.
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

### Hemmet
Återkommande hemunderhåll: brandvarnare, sotning, hängrännor och vattenavstängning inför vintern.
