# Otchlan Dumper (Fabric 1.20.1, tylko klient)

Po wpisaniu `/otchlan` mod czeka na otwarcie menu i wysyla pakiety klikniecia
(`clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player)`) w kazdy slot z przedmiotem.
Nie rusza myszka - wysyla sam pakiet klikniecia. Inne komendy sa ignorowane.

- Right Shift - menu ustawien (ON/OFF, przerwa miedzy kliknieciami, opoznienie startu)
- ON  -> `/otchlan` wyrzuca wszystko z menu
- OFF -> `/otchlan` dziala normalnie, mod nic nie klika
- Config: `config/otchlan.json` (mozna tam zmienic nazwe komendy)

## Budowanie
JDK 17, potem `gradle build`. Plik: `build/libs/otchlan-1.0.0.jar` -> do `mods`
razem z Fabric Loader i Fabric API (1.20.1). Mozna tez uzyc GitHub Actions (.github/workflows/build.yml).
