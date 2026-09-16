package com.creditsimulator.io;

import com.creditsimulator.domain.LoanRequest;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Persists a calculated loan request as a named "sheet" (same key=value
 * format as file_inputs.txt) so it can be switched back to later, in the
 * same session or a future run.
 */
public class SheetRepository {

    private static final Pattern VALID_NAME = Pattern.compile("[a-zA-Z0-9_-]+");
    private static final String EXTENSION = ".sheet.txt";

    private final Path directory;
    private final FileInputReader fileInputReader = new FileInputReader();

    public SheetRepository() {
        this(Path.of("sheets"));
    }

    public SheetRepository(Path directory) {
        this.directory = directory;
    }

    public void save(String name, LoanRequest request) throws IOException {
        validateName(name);
        Files.createDirectories(directory);
        List<String> lines = List.of(
                "jenisKendaraan=" + request.getVehicle().getTypeName(),
                "kondisiKendaraan=" + request.getCondition(),
                "tahunKendaraan=" + request.getVehicleYear(),
                "jumlahPinjaman=" + request.getTotalLoanAmount().toPlainString(),
                "tenor=" + request.getTenorYears(),
                "jumlahDP=" + request.getDownPayment().toPlainString()
        );
        Files.write(pathFor(name), lines);
    }

    public LoanRequest load(String name) throws IOException {
        validateName(name);
        Path path = pathFor(name);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(
                    "Sheet '" + name + "' tidak ditemukan. Ketik 'sheets' untuk daftar sheet yang tersimpan.");
        }
        return fileInputReader.readLoanRequest(path);
    }

    public List<String> list() throws IOException {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return names;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                names.add(fileName.substring(0, fileName.length() - EXTENSION.length()));
            }
        }
        names.sort(String::compareTo);
        return names;
    }

    private Path pathFor(String name) {
        return directory.resolve(name + EXTENSION);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || !VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Nama sheet hanya boleh berisi huruf, angka, '-', dan '_'");
        }
    }
}
