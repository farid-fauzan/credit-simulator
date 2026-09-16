# Credit Simulator

Console application untuk menghitung cicilan bulanan kredit kendaraan (Mobil/Motor), dibuat sebagai jawaban technical test Backend Engineer.

**Bahasa yang digunakan: Java 17** (JDK 17, tanpa library eksternal — hanya menggunakan API bawaan JDK, termasuk `java.net.http.HttpClient` untuk pemanggilan web service).

## Requirement

- JDK 17+
- Maven 3.6+

## Build

```bash
mvn clean package
```

Perintah ini akan compile, menjalankan unit test, lalu membungkus aplikasi menjadi satu executable jar di `target/credit-simulator.jar`. Tidak ada file hasil build (`.class`/`.jar`) yang di-commit ke repository — semuanya dihasilkan oleh Maven.

## Cara Menjalankan

Executable `credit_simulator` ada di root project (wrapper shell script yang memanggil `target/credit-simulator.jar`).

**Mode interaktif** (tanpa file input):
```bash
./credit_simulator
```

**Mode file input**:
```bash
./credit_simulator file_inputs.txt
```

Contoh isi `file_inputs.txt` (format `key=value`, disediakan di root project):
```
jenisKendaraan=Mobil
kondisiKendaraan=Baru
tahunKendaraan=2025
jumlahPinjaman=1000000000
tenor=6
jumlahDP=500000000
```

## Menu (mode interaktif)

Setelah `./credit_simulator` dijalankan tanpa argumen, tersedia command:

| Command | Fungsi |
|---|---|
| `show` | Menampilkan semua command yang tersedia |
| `new`  | Input data kendaraan secara manual, lalu hitung & tampilkan cicilan |
| `load` | Ambil data dari web service (`GET run.mocky.io/...`), otomatis hitung & tampilkan hasilnya |
| `save <nama>` | Simpan hasil kalkulasi terakhir (`new`/`load`/`switch`) sebagai sebuah "sheet" bernama `<nama>` |
| `sheets` | Menampilkan daftar sheet yang sudah tersimpan |
| `switch <nama>` (alias `sheet <nama>`) | Beralih ke sheet `<nama>` — input yang tersimpan dihitung ulang & hasilnya langsung ditampilkan |
| `exit` | Keluar dari aplikasi |

Sebuah "sheet" adalah satu skenario perhitungan (mirip tab di Excel) yang bisa disimpan lalu dipanggil kembali tanpa perlu input ulang. Disimpan sebagai file `<nama>.sheet.txt` (format sama dengan `file_inputs.txt`) di folder `sheets/` — dibuat otomatis di direktori kerja saat pertama kali `save` dipakai, dan tidak di-commit ke repo (lihat `.gitignore`).

Contoh sesi:
```
=== Credit Simulator ===
Command yang tersedia:
  show           - tampilkan daftar command
  new            - input data kendaraan baru & hitung cicilan
  load           - ambil data dari web service & hitung cicilan
  save <nama>    - simpan hasil kalkulasi terakhir sebagai sheet <nama>
  sheets         - tampilkan daftar sheet yang tersimpan
  switch <nama>  - pindah & tampilkan ulang hasil kalkulasi sheet <nama>
  exit           - keluar dari aplikasi

> new
Jenis Kendaraan (Motor/Mobil): Mobil
Kondisi Kendaraan (Bekas/Baru): Baru
Tahun Kendaraan (4 digit): 2025
Jumlah Pinjaman Total: 1000000000
Tenor Pinjaman (1-6 thn): 6
Jumlah DP: 500000000

Jenis Kendaraan   : Mobil
Kondisi           : BARU
...
Jumlah Cicilan Perbulan (tahun pertama): Rp. 7,500,000.00

> save mobil-baru-2025
Tersimpan sebagai sheet 'mobil-baru-2025'.

> sheets
Sheet tersimpan:
  - mobil-baru-2025

> switch mobil-baru-2025
Jenis Kendaraan   : Mobil
...
(beralih ke sheet 'mobil-baru-2025')
```

## Input & Business Rules

| Field | Aturan |
|---|---|
| Jenis Kendaraan | `Motor` atau `Mobil` (case-insensitive) |
| Kondisi Kendaraan | `Bekas` atau `Baru` (case-insensitive) |
| Tahun Kendaraan | 4 digit angka. Untuk kondisi **Baru**, tidak boleh kurang dari (tahun sekarang − 1) |
| Jumlah Pinjaman Total | Angka, maksimal Rp 1.000.000.000 |
| Tenor Pinjaman | 1–6 tahun |
| Jumlah DP | Minimal **35%** dari Jumlah Pinjaman untuk kendaraan **Baru**, minimal **25%** untuk **Bekas** |

Base suku bunga: **Mobil = 8%**, **Motor = 9%**.

## Formula Perhitungan

Bunga dihitung ulang tiap tahun dari **sisa pokok pinjaman** (declining balance), sesuai referensi kalkulasi di `Rumus.xlsx` yang dilampirkan pada soal:

```
Pokok Pinjaman = Jumlah Pinjaman − Jumlah DP

Untuk tiap tahun ke-N (N = 1..tenor):
  Total Pinjaman Tahun-N = Sisa Pokok + (Sisa Pokok × Rate Tahun-N)
  Cicilan/bulan Tahun-N  = Total Pinjaman Tahun-N ÷ (sisa bulan tenor)
  Cicilan/tahun Tahun-N  = Cicilan/bulan × 12
  Sisa Pokok Tahun-(N+1) = Total Pinjaman Tahun-N − Cicilan/tahun Tahun-N
```

**Catatan asumsi**: soal hanya memberikan 3 titik data contoh untuk kenaikan suku bunga tahunan (tahun1: 8%, tahun2: 8,1%, tahun3: 8,6%), tanpa formula eksplisit. Rule di soal menyebutkan "naik 0,1% tiap 1 tahun" dan "naik 0,5% tiap 2 tahun" — diinterpretasikan sebagai kenaikan yang berselang-seling per transisi tahun (+0,1% pada transisi ganjil, +0,5% pada transisi genap), karena pola ini yang paling cocok dengan ketiga sample yang diberikan. Diimplementasikan di `InterestRateService`, diverifikasi lewat unit test terhadap `Rumus.xlsx`.

## Unit Test

```bash
mvn test
```

Test yang tersedia:
- `InterestRateServiceTest` — memverifikasi kenaikan suku bunga tahunan cocok dengan sample di soal (8% → 8,1% → 8,6%)
- `LoanCalculatorServiceTest` — memverifikasi hasil kalkulasi cicilan persis sama dengan angka referensi di `Rumus.xlsx`
- `LoanValidatorTest` — memverifikasi seluruh business rule (tenor, DP minimum, tahun kendaraan, batas pinjaman)
- `SheetRepositoryTest` — memverifikasi save/list/switch sheet, termasuk validasi nama sheet & sheet yang tidak ditemukan

## Struktur Project

```
credit-simulator/
├── credit_simulator          # executable wrapper (shell script)
├── pom.xml                   # Maven build config
├── file_inputs.txt           # contoh file input
├── src/main/java/com/creditsimulator/
│   ├── domain/                # model data (Vehicle/Mobil/Motor, LoanRequest, dst)
│   ├── factory/                # VehicleFactory (Factory pattern)
│   ├── service/                # business logic kalkulasi & suku bunga
│   ├── validation/             # business rule validation
│   ├── io/                     # input konsol, file, HTTP client + JSON parser
│   ├── controller/             # entry point & menu (App.java)
│   └── util/                   # formatting
└── src/test/java/com/creditsimulator/   # unit test
```

## Known Issue

Endpoint contoh web service di soal (`run.mocky.io/v3/9108b1da-...`) menggunakan layanan mock gratis yang mock-nya bisa kedaluwarsa seiring waktu. Jika command `load` mengembalikan error `404`, ini bukan bug pada aplikasi — endpoint mock tersebut sudah tidak aktif di sisi penyedia layanan. Aplikasi menangani kondisi ini dengan pesan error yang jelas, tidak crash.
