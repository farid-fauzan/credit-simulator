package com.creditsimulator.controller;

import com.creditsimulator.domain.CalculationResult;
import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.VehicleCondition;
import com.creditsimulator.domain.YearlyInstallment;
import com.creditsimulator.factory.VehicleFactory;
import com.creditsimulator.io.ConsoleInputReader;
import com.creditsimulator.io.FileInputReader;
import com.creditsimulator.io.JsonHttpClient;
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
import java.util.Map;

public class App {

    private static final String LOAD_EXISTING_URL =
            "https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c";

    private final LoanValidator loanValidator = new LoanValidator();
    private final LoanCalculatorService loanCalculatorService =
            new LoanCalculatorService(new InterestRateService());
    private final JsonHttpClient jsonHttpClient = new JsonHttpClient();

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
            String command = line.trim().toLowerCase();

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
                case "exit":
                case "quit":
                    return;
                case "":
                    break;
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
        System.out.println("  show  - tampilkan daftar command");
        System.out.println("  new   - input data kendaraan baru & hitung cicilan");
        System.out.println("  load  - ambil data dari web service & hitung cicilan");
        System.out.println("  exit  - keluar dari aplikasi");
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
