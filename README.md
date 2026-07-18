# N3XR Client Mod (Minecraft 1.21.1 / Fabric)

## Isi project
- `src/main/java/com/n3xr/N3XRClient.java` -> logic Armor HUD + FPS Display
- `src/main/resources/fabric.mod.json` -> metadata mod
- `src/main/resources/assets/minecraft/textures/gui/title/` -> taruh PNG custom di sini buat ganti logo

## Ganti logo "Minecraft" & "Java Edition"
1. Extract `minecraft.png` dan `edition.png` dari client jar 1.21.1
   (biasanya di `.minecraft/versions/1.21.1/1.21.1.jar` -> `assets/minecraft/textures/gui/title/`)
   buat tau ukuran pixel yang pas.
2. Bikin gambar custom "N3XR" dengan ukuran SAMA PERSIS seperti file asli.
3. Timpa file itu, taruh di:
   `src/main/resources/assets/minecraft/textures/gui/title/minecraft.png`
   `src/main/resources/assets/minecraft/textures/gui/title/edition.png`

## Cara build jadi .jar
Butuh device dengan **koneksi internet** (buat download Minecraft libs, Yarn mappings, Fabric API) dan **JDK 21**.

```bash
cd n3xr-mod
./gradlew build
```

Hasil jar ada di: `build/libs/n3xr-client-1.0.0.jar`
Tinggal taruh ke folder `mods` di launcher (TowoLauncher/Fabric) lo.

## Kalau ada error compile
Nama method `getArmorItems()` bisa beda tergantung versi Yarn mapping.
Kalau merah, cek class `PlayerEntity` / `LivingEntity` di decompiled source (lewat Android Studio/Code on the Go "go to definition") buat cari nama method armor yang bener di 1.21.1.
