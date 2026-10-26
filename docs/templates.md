# Mallkatalogen

Mallarna är data i `content/templates/*.json`: frågor som gömmer det som inte gäller, faser med steg i
relativ tid, länkar ur länkregistret, foto-uppmaningar och långa trådar. Appens galleri visar dem per
kategori, föreslår dem som passar användarens livsfaser först och låter användaren söka på titel,
beskrivning och nytta.

> Fakta om regler och frister ska kontrolleras mot källan innan mallen publiceras (`reviewedAt`). Belopp,
> procentsatser och tak skrivs aldrig in i stegen – de ändras, och länken till källan gäller. Mallar beskriver
> vanliga steg, aldrig "allt du måste göra".

Förkortningar: **T** = ankardatum (flyttdag, tillträde, förlossning …). `T−6v` = sex veckor före.

Biblioteket har **31 mallar**. `move` är den stora flyttmallen; de andra är medvetet kortare.

## Kategorier

| Kategori | `category` | Mallar |
| --- | --- | --- |
| Hemmet | `home` | `buy-home`, `declutter`, `first-home`, `move`, `renovate`, `sell-home` |
| Familjen | `family` | `adoption`, `child-moving-out`, `new-baby`, `pet`, `wedding` |
| Arbete och studier | `work-study` | `drivers-license`, `graduation`, `job-loss`, `new-job`, `start-business`, `study` |
| Hälsa | `health` | `caring-for-relative`, `health-treatment` |
| Livshändelser | `life-events` | `bereavement`, `divorce`, `funeral`, `move-abroad`, `new-in-sweden`, `travel` |
| Ekonomi och vardag | `finance-admin` | `car`, `major-purchase`, `personal-finance`, `retirement`, `tax-year`, `will` |

## Mallarna

### Hemmet

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `buy-home` | Köpa bostad | Buying a home | Tillträdesdag | 12 | 1 |
| `declutter` | Rensa hemma | Decluttering | Måldag | 8 | 0 |
| `first-home` | Första egna hemmet | Your first home | Inflyttningsdag | 8 | 0 |
| `move` | Flytta | Moving | Flyttdag | 42 | 5 |
| `renovate` | Renovera | Renovating | Startdag | 13 | 2 |
| `sell-home` | Sälja bostad | Selling a home | Kontraktsdag | 9 | 1 |

### Familjen

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `adoption` | Adoptera | Adopting | Hemkomst | 8 | 0 |
| `child-moving-out` | Barn flyttar ut | A child moving out | Flyttdag | 8 | 0 |
| `new-baby` | Vänta barn | Expecting a baby | Beräknad förlossning | 10 | 1 |
| `pet` | Skaffa husdjur | Getting a pet | Inflyttningsdag | 9 | 1 |
| `wedding` | Gifta sig | Getting married | Bröllopsdag | 9 | 0 |

### Arbete och studier

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `drivers-license` | Ta körkort | Getting a driving licence | Uppkörning | 10 | 0 |
| `graduation` | Ta examen | Graduating | Examensdag | 9 | 0 |
| `job-loss` | Förlora jobbet | Losing your job | Sista arbetsdagen | 8 | 0 |
| `new-job` | Nytt jobb | A new job | Första arbetsdagen | 9 | 0 |
| `start-business` | Starta företag | Starting a business | Startdag | 10 | 0 |
| `study` | Börja studera | Starting to study | Terminsstart | 9 | 0 |

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
| `new-in-sweden` | Ny i Sverige | New in Sweden | Ankomstdag | 9 | 0 |
| `travel` | Resa | Travelling | Avresedag | 10 | 0 |

### Ekonomi och vardag

| Id | Svensk titel | Engelsk titel | Ankare | Steg | Frågor |
| --- | --- | --- | --- | --- | --- |
| `car` | Köpa bil | Buying a car | Hämtningsdag | 10 | 0 |
| `major-purchase` | Större köp | A major purchase | Köpdag | 8 | 0 |
| `personal-finance` | Privatekonomi | Personal finances | Genomgångsdag | 8 | 0 |
| `retirement` | Gå i pension | Retiring | Pensionsdag | 9 | 0 |
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

- `description` och `helps` är galleriets korta texter: vad mallen är och vad den hjälper med. `note` är en
  varsam notering som bara visas när den behövs (till exempel juridik eller sorg).
- `anchors` är datumen allt räknas från. Ett ankare får vara okänt; då får stegen inga datum men behåller
  mallens ordning.
- `questions` är val (`options`) eller ja/nej (`bool`). Ett obesvarat villkor gömmer ingenting.
- `phases` blir delflöden i listan. `longThreads` ligger under *Längre fram* och följer med när kapitlet vilar.
- `retired` behålls för evigt: användarens kapitel refererar till steg-id, inte till radnummer.
