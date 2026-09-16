package com.creditsimulator.controller;

import com.creditsimulator.domain.CalculationResult;
import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.VehicleCondition;
import com.creditsimulator.domain.YearlyInstallment;
import com.creditsimulator.factory.VehicleFactory;
import com.creditsimulator.io.ConsoleInputReader;
import com.creditsimulator.io.FileInputReader;
import com.creditsimulator.io.JsonHttpClient;
import com.creditsimulator.io.SheetRepository;
import com.creditsimulator.io.SimpleJsonParser;
import com.creditsimulator.service.InterestRateService;
import com.creditsimulator.service.LoanCalculatorService;
import com.creditsimulator.util.MoneyFormatter;
import com.creditsimulator.validation.LoanValidator;
import com.creditsimulator.validation.ValidationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class App {

    private static final String LOAD_EXISTING_URL =
            "https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c";

    private final LoanValidator loanValidator = new LoanValidator();
    private final LoanCalculatorService loanCalculatorService =
            new LoanCalculatorService(new InterestRateService());
    private final JsonHttpClient jsonHttpClient = new JsonHttpClient();
    private final SheetRepository sheetRepository = new SheetRepository();

    private LoanRequest lastRequest;

    public static void main(String[] args) {
        new App().run(args);
    }

    public void run(String[] args) {
        try {
            if (args.length > 0) {
                runFileMode(args[0]);
            } else {
                runInteractiveMode();
            }
        } catch (Exception e) {
            System.out.println("Terjadi kesalahan: " + e.getMessage());
            System.exit(1);
        }
    }

    private void runFileMode(String filePath) throws Exception {
        FileInputReader fileInputReader = new FileInputReader();
        LoanRequest request = fileInputReader.readLoanRequest(Path.of(filePath));
        processAndPrint(request);
    }

    private void runInteractiveMode() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        printWelcome();

        while (true) {
            System.out.print("\n> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }
            String[] parts = trimmedLine.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String argument = parts.length > 1 ? parts[1].trim() : null;

            switch (command) {
                case "show":
                    printCommands();
                    break;
                case "new":
                    handleNew(reader);
                    break;
                case "load":
                    handleLoad();
                    break;
                case "save":
                    handleSave(argument);
                    break;
                case "sheets":
                    handleListSheets();
                    break;
                case "switch":
                case "sheet":
                    handleSwitch(argument);
                    break;
                case "exit":
                case "quit":
                    return;
                default:
                    System.out.println("Command tidak dikenal: '" + command + "'. Ketik 'show' untuk daftar command.");
            }
        }
    }

    private void printWelcome() {
        System.out.println("=== Credit Simulator ===");
        printCommands();
    }

    private void printCommands() {
        System.out.println("Command yang tersedia:");
        System.out.println("  show           - tampilkan daftar command");
        System.out.println("  new            - input data kendaraan baru & hitung cicilan");
        System.out.println("  load           - ambil data dari web service & hitung cicilan");
        System.out.println("  save <nama>    - simpan hasil kalkulasi terakhir sebagai sheet <nama>");
        System.out.println("  sheets         - tampilkan daftar sheet yang tersimpan");
        System.out.println("  switch <nama>  - pindah & tampilkan ulang hasil kalkulasi sheet <nama>");
        System.out.println("  exit           - keluar dari aplikasi");
    }

    private void handleNew(BufferedReader reader) {
        try {
            ConsoleInputReader consoleInputReader = new ConsoleInputReader(reader);
            LoanRequest request = consoleInputReader.readLoanRequest();
            processAndPrint(request);
        } catch (ValidationException e) {
            System.out.println("Input tidak memenuhi aturan: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Gagal memproses input: " + e.getMessage());
        }
    }

    private void handleLoad() {
        try {
            String json = jsonHttpClient.get(LOAD_EXISTING_URL);
            LoanRequest request = mapJsonToLoanRequest(json);
            processAndPrint(request);
        } catch (ValidationException e) {
            System.out.println("Data dari web service tidak memenuhi aturan: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Gagal memuat data dari web service: " + e.getMessage());
        }
    }

    private void handleSave(String name) {
        if (lastRequest == null) {
            System.out.println("Belum ada hasil kalkulasi untuk disimpan. Jalankan 'new' atau 'load' dulu.");
            return;
        }
        if (name == null || name.isBlank()) {
            System.out.println("Gunakan format: save <nama-sheet>");
            return;
        }
        try {
            sheetRepository.save(name, lastRequest);
            System.out.println("Tersimpan sebagai sheet '" + name + "'.");
        } catch (Exception e) {
            System.out.println("Gagal menyimpan sheet: " + e.getMessage());
        }
    }

    private void handleListSheets() {
        try {
            List<String> names = sheetRepository.list();
            if (names.isEmpty()) {
                System.out.println("Belum ada sheet yang tersimpan. Gunakan 'save <nama>' setelah 'new'/'load'.");
            } else {
                System.out.println("Sheet tersimpan:");
                names.forEach(n -> System.out.println("  - " + n));
            }
        } catch (Exception e) {
            System.out.println("Gagal membaca daftar sheet: " + e.getMessage());
        }
    }

    private void handleSwitch(String name) {
        if (name == null || name.isBlank()) {
            System.out.println("Gunakan format: switch <nama-sheet>");
            return;
        }
        try {
            LoanRequest request = sheetRepository.load(name);
            processAndPrint(request);
            System.out.println("(beralih ke sheet '" + name + "')");
        } catch (ValidationException e) {
            System.out.println("Sheet '" + name + "' tidak memenuhi aturan: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Gagal switch ke sheet: " + e.getMessage());
        }
    }

    private LoanRequest mapJsonToLoanRequest(String json) {
        Map<String, String> fields = SimpleJsonParser.parseFlatObject(json);
        return new LoanRequest(
                VehicleFactory.create(fields.get("vehicleType")),
                VehicleCondition.fromString(fields.get("vehicleCondition")),
                Integer.parseInt(fields.get("vehicleYear")),
                new BigDecimal(fields.get("totalLoanAmount")),
                Integer.parseInt(fields.get("loanTenure")),
                new BigDecimal(fields.get("downPayment"))
        );
    }

    private void processAndPrint(LoanRequest request) {
        loanValidator.validate(request);
        CalculationResult result = loanCalculatorService.calculate(request);
        lastRequest = request;
        printResult(request, result);
    }

    private void printResult(LoanRequest request, CalculationResult result) {
        System.out.println();
        System.out.println("Jenis Kendaraan   : " + request.getVehicle().getTypeName());
        System.out.println("Kondisi           : " + request.getCondition());
        System.out.println("Tahun Kendaraan   : " + request.getVehicleYear());
        System.out.println("Jumlah Pinjaman   : " + MoneyFormatter.toRupiah(request.getTotalLoanAmount()));
        System.out.println("Jumlah DP         : " + MoneyFormatter.toRupiah(request.getDownPayment()));
        System.out.println("Pokok Pinjaman    : " + MoneyFormatter.toRupiah(result.getPokokPinjaman()));
        System.out.println("Tenor             : " + request.getTenorYears() + " tahun");
        System.out.println();
        System.out.println("Rincian per tahun:");
        for (YearlyInstallment yearly : result.getYearlyBreakdown()) {
            System.out.printf("  Tahun %d : %s/bln , Suku Bunga : %s%n",
                    yearly.getYear(),
                    MoneyFormatter.toRupiah(yearly.getInstallmentMonthly()),
                    MoneyFormatter.toPercentage(yearly.getRate()));
        }
        System.out.println();
        System.out.println("Jumlah Cicilan Perbulan (tahun pertama): "
                + MoneyFormatter.toRupiah(result.getFirstYearInstallmentMonthly()));
    }
}
