import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

// Interface Organism
interface Organism {
    void displayInfo();
}

// Abstract class LivingOrganism
abstract class LivingOrganism {
    protected String name;
    protected String scientificName;

    public LivingOrganism(String name, String scientificName) {
        this.name = name;
        this.scientificName = scientificName;
    }
}

// Subclass Plant
class Plant extends LivingOrganism implements Organism {
    private String plantType;

    public Plant(String name, String scientificName, String plantType) {
        super(name, scientificName);
        this.plantType = plantType;
    }

    @Override
    public void displayInfo() {
        System.out.println("Jenis: Tumbuhan");
        System.out.println("Nama: " + name);
        System.out.println("Nama Ilmiah: " + scientificName);
        System.out.println("Tipe Tumbuhan: " + plantType);
    }
}

// Subclass Animal
class Animal extends LivingOrganism implements Organism {
    private String habitat;

    public Animal(String name, String scientificName, String habitat) {
        super(name, scientificName);
        this.habitat = habitat;
    }

    @Override
    public void displayInfo() {
        System.out.println("Jenis: Hewan");
        System.out.println("Nama: " + name);
        System.out.println("Nama Ilmiah: " + scientificName);
        System.out.println("Habitat: " + habitat);
    }
}

public class ForestManagement {
    // Kredensial database
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/forestmanagement";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "mynqsa29";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;

        try {
            // Menghubungkan ke database PostgreSQL
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Terhubung ke database!");

            while (true) {
                // Menampilkan tanggal dan waktu
                String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                System.out.println("\n=== Sistem Pengelolaan Hutan ===");
                System.out.println("Tanggal dan Waktu: " + currentDateTime);
                System.out.println("1. Tambah Organisme");
                System.out.println("2. Lihat Semua Organisme");
                System.out.println("3. Perbarui Organisme");
                System.out.println("4. Hapus Organisme");
                System.out.println("5. Keluar");
                System.out.print("Pilih opsi: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1 -> addOrganism(scanner, connection);
                    case 2 -> viewAllOrganisms(connection);
                    case 3 -> updateOrganism(scanner, connection);
                    case 4 -> deleteOrganism(scanner, connection);
                    case 5 -> {
                        System.out.println("Keluar dari program. Sampai jumpa!");
                        return;
                    }
                    default -> System.out.println("Opsi tidak valid. Silakan coba lagi.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Koneksi database gagal: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println("Gagal menutup koneksi database: " + e.getMessage());
            }
        }
    }

    // Tambah Organisme
    private static void addOrganism(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("\n=== Tambah Organisme ===");
        System.out.println("1. Tumbuhan");
        System.out.println("2. Hewan");
        System.out.print("Masukkan jenis (1 untuk Tumbuhan, 2 untuk Hewan): ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine(); // Konsumsi newline

        String type;
        String specificField;
        int totalQuantity = 0;

        // Tentukan jenis organisme
        switch (typeChoice) {
            case 1 -> {
                type = "Tumbuhan";
                System.out.print("Masukkan tipe tumbuhan: ");
                specificField = scanner.nextLine();

                // Input jumlah organisme
                System.out.print("Masukkan jumlah organisme: ");
                totalQuantity = scanner.nextInt();
                scanner.nextLine(); // Konsumsi newline

                if (totalQuantity <= 0) {
                    System.out.println("Jumlah organisme harus lebih dari 0. Proses dibatalkan.");
                    return;
                }
            }
            case 2 -> {
                type = "Hewan";
                System.out.print("Masukkan habitat: ");
                specificField = scanner.nextLine();

                // Input jumlah organisme jantan dan betina
                System.out.print("Masukkan jumlah jantan: ");
                int maleCount = scanner.nextInt();
                System.out.print("Masukkan jumlah betina: ");
                int femaleCount = scanner.nextInt();
                scanner.nextLine(); // Konsumsi newline

                if (maleCount < 0 || femaleCount < 0) {
                    System.out.println("Jumlah jantan dan betina harus tidak negatif. Proses dibatalkan.");
                    return;
                }
                totalQuantity = maleCount + femaleCount;
            }
            default -> {
                System.out.println("Pilihan tidak valid. Silakan masukkan 1 untuk Tumbuhan atau 2 untuk Hewan.");
                return;
            }
        }

        // Input detail organisme
        System.out.print("Masukkan nama: ");
        String name = scanner.nextLine();
        System.out.print("Masukkan nama ilmiah: ");
        String scientificName = scanner.nextLine();

        // Masukkan data ke database
        String query = "INSERT INTO organisms (type, name, scientific_name, specific_field, quantity) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, type);
            stmt.setString(2, name);
            stmt.setString(3, scientificName);
            stmt.setString(4, specificField);
            stmt.setInt(5, totalQuantity);
            stmt.executeUpdate();
            System.out.println("Organisme berhasil ditambahkan!");
        }
    }

    // Lihat Semua Organisme
    private static void viewAllOrganisms(Connection connection) throws SQLException {
        System.out.println("\n=== Semua Organisme ===");
        String query = "SELECT * FROM organisms";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.printf("%-5s %-10s %-15s %-20s %-20s %-10s%n", 
                "ID", "Jenis", "Nama", "Nama Ilmiah", "Field Spesifik", "Jumlah");
            System.out.println("------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d %-10s %-15s %-20s %-20s %-10d%n", 
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getString("name"),
                    rs.getString("scientific_name"),
                    rs.getString("specific_field"),
                    rs.getInt("quantity"));
            }
        }
    }

    // Perbarui Organisme
    private static void updateOrganism(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("\n=== Perbarui Organisme ===");
        System.out.print("Masukkan ID organisme yang akan diperbarui: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Konsumsi newline

        System.out.print("Masukkan nama baru (tekan enter untuk melewati): ");
        String newName = scanner.nextLine();

        System.out.print("Masukkan jumlah baru (tekan -1 untuk melewati): ");
        int newQuantity = scanner.nextInt();
        scanner.nextLine(); // Konsumsi newline

        // Perbarui data organisme
        String query = "UPDATE organisms SET name = COALESCE(NULLIF(?, ''), name), quantity = CASE WHEN ? = -1 THEN quantity ELSE ? END WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newName);
            stmt.setInt(2, newQuantity);
            stmt.setInt(3, newQuantity);
            stmt.setInt(4, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Organisme berhasil diperbarui!");
            } else {
                System.out.println("Organisme tidak ditemukan.");
            }
        }
    }

    // Hapus Organisme
    private static void deleteOrganism(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("\n=== Hapus Organisme ===");
        System.out.print("Masukkan ID organisme yang akan dihapus: ");
        int id = scanner.nextInt();

        String query = "DELETE FROM organisms WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Organisme berhasil dihapus!");
            } else {
                System.out.println("Organisme tidak ditemukan.");
            }
        }
    }
}
