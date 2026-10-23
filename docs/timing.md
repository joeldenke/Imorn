# Tid: dagar, inte tider

Imorn planerar **dagar**. En dag har ingen klockslag och ingen tidszon, så planen flyttar sig aldrig när telefonen
reser sig, klockorna ställs om eller appen startas om. Bara *i dag* följer telefonen.

## Reglerna

1. **Ett ankare bestämmer sina steg.** Ett steg räknas från sitt ankare (`T−6v`, `T−3 månader`, `nästa år, april`).
   Flyttar användaren ankaret, flyttar sig stegen och deras frister – utom de steg som har ett eget datum.
2. **Ett eget datum vinner.** Ett steg med eget datum står kvar där det står, även när ankaret flyttas. Det är
   användarens egen dag, inte mallens.
3. **Dagar lagras som dagar.** I databasen ligger varje dag som ett epoch day (`LocalDate`), aldrig som ett klockslag
   med en tidszon. Därför är samma kapitel samma kapitel i Kirunati, i New York och mitt på ett dygn med
   sommartidsomställning.
4. **I dag är telefonens dag.** `Today` (`se.imorn.model.Today`) läses ur telefonens egen klocka, i telefonens egen
   tidszon, och läses om varje gång appen visas. Dagen byter vid lokal midnatt – inte vid midnatt UTC – och hoppar
   inte när klockorna ställs om fram eller tillbaka.
5. **En omstart ändrar ingenting.** Kapitlet, planen, det flyttade ankaret och det som sparats (foton, papper,
   anteckningar) finns kvar när appen stängs och öppnas igen.

## Varför det är så

En plan som rör sig när telefonen rör sig är inte en plan, det är en slump. Den som ska betala flyttanmälan senast en
vecka efter flytten ska se samma datum i mars som i juli, och samma datum när hen kommer hem från en vecka i
utlandet. Därför ligger tid som *dagar* i databasen och som *en dags skillnad* i modellen, och klockan läses bara
där den verkligen behövs: för att veta vilken dag det är nu.

## Vad som är testat

| Test | Vad den låser fast |
| ---- | ------------------- |
| `core/model/.../TimingTest.kt` | Planen är densamma i UTC, Stockholm, New York, Kiritimati och Midway; sommartid flyttar ingen dag; ett flyttat ankare flyttar planen och fristerna; ett eget datum står kvar; ett okänt ankare lämnar stegen odaterade |
| `core/data/.../DeviceTodayTest.kt` | Appens dag vänder vid lokal midnatt (inte UTC-midnatt); samma ögonblick är olika dag på olika platser; sommartid hoppar inte över en dag |
| `core/database/.../ChapterRestartTest.kt` | Kapitlet, planen, det flyttade ankaret, stegets eget datum och det sparade finns kvar efter att databasen stängs och öppnas igen; dagarna flyttar sig inte när telefonens tidszon ändras |

## Vad som inte täcks än

- **Påminnelser** (fas 3) finns inte än. När de byggs måste nästa påminnelse räknas om i telefonens egen tidszon,
  annars skulle en flytt ändra när appen säger ifrån.
- **Inställningar** sparas i DataStore, och skrivningen är köad: ett hårt avbrott (batteriet dras) kan tappa den
  sista inställningen. Det är samma avvägning som `SharedPreferences.apply()` ger och ändras inte här.
- Appen som står kvar öppen över midnatt läser om dagen nästa gång den visas, inte klockan varje sekund.
