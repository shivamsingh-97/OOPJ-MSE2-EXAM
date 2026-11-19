package ordermanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class OrderManagement {

    private static final String[] VALID_STATUS = { "Pending", "Shipped", "Delivered", "Cancelled" };

    public static void main(String[] args) {

        try (Connection con = DBConnection.getConnection();
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Connected to ecommerce_db successfully.");

            boolean running = true;
            while (running) {
                System.out.println("\n========== ORDER MANAGEMENT SYSTEM ==========");
                System.out.println("1. Insert 5 sample orders");
                System.out.println("2. Insert a new order (enter your own data)");
                System.out.println("3. Show orders by status (quantity > 2, price high to low)");
                System.out.println("4. Increase quantity of an order");
                System.out.println("5. Update order status");
                System.out.println("6. Show total order value");
                System.out.println("7. Delete a Cancelled order");
                System.out.println("8. Display all orders");
                System.out.println("9. Exit");
                int choice = readInt(sc, "Enter your choice: ");

                try {
                    switch (choice) {
                        case 1: insertSampleOrders(con); break;
                        case 2: insertNewOrder(con, sc); break;
                        case 3: showOrdersByStatus(con, sc); break;
                        case 4: increaseQuantity(con, sc); break;
                        case 5: updateStatus(con, sc); break;
                        case 6: showTotalValue(con); break;
                        case 7: deleteCancelledOrder(con, sc); break;
                        case 8: displayAllOrders(con); break;
                        case 9:
                            running = false;
                            System.out.println("Goodbye!");
                            break;
                        default:
                            System.out.println("Invalid choice. Enter a number from 1 to 9.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not connect to the database: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Input helpers
    // ---------------------------------------------------------------
    private static int readInt(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String text = sc.nextLine().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private static double readDouble(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String text = sc.nextLine().trim();
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private static String readText(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String text = sc.nextLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    // Returns the correctly spelled status, or null if it is not valid
    private static String validStatus(String input) {
        for (String s : VALID_STATUS) {
            if (s.equalsIgnoreCase(input)) {
                return s;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Display helpers
    // ---------------------------------------------------------------
    private static void printHeader() {
        System.out.printf("%-8s %-18s %-18s %-8s %-12s %-10s%n",
                "ID", "Customer", "Product", "Qty", "Unit Price", "Status");
        System.out.println("----------------------------------------------------------------------------");
    }

    private static void printRow(ResultSet rs) throws SQLException {
        System.out.printf("%-8d %-18s %-18s %-8d %-12.2f %-10s%n",
                rs.getInt("order_id"),
                rs.getString("customer_name"),
                rs.getString("product_name"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price"),
                rs.getString("status"));
    }

    private static void printOrderById(Connection con, int id) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                printHeader();
                while (rs.next()) {
                    printRow(rs);
                }
            }
        }
    }

    private static boolean orderExists(Connection con, int id) throws SQLException {
        String sql = "SELECT order_id FROM Orders WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ---------------------------------------------------------------
    // a) Insert orders (PreparedStatement + duplicate handling)
    // ---------------------------------------------------------------
    private static void insertOrder(Connection con, int id, String customer, String product,
                                    int qty, double price, String status) throws SQLException {

        if (orderExists(con, id)) {
            System.out.println("Order ID " + id + " already exists. Duplicate not inserted.");
            return;
        }

        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, customer);
            ps.setString(3, product);
            ps.setInt(4, qty);
            ps.setDouble(5, price);
            ps.setString(6, status);
            ps.executeUpdate();
            System.out.println("Order ID " + id + " inserted successfully.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {   // MySQL duplicate entry error code
                System.out.println("Order ID " + id + " already exists. Duplicate not inserted.");
            } else {
                throw e;
            }
        }
    }

    private static void insertSampleOrders(Connection con) throws SQLException {
        insertOrder(con, 101, "Rahul Sharma", "Laptop", 3, 55000.0, "Pending");
        insertOrder(con, 102, "Priya Verma", "Mouse", 5, 750.50, "Shipped");
        insertOrder(con, 103, "Amit Singh", "Keyboard", 2, 1500.0, "Delivered");
        insertOrder(con, 104, "Neha Gupta", "Headphones", 4, 2200.0, "Cancelled");
        insertOrder(con, 105, "Karan Mehta", "Monitor", 4, 12000.0, "Pending");
    }

    private static void insertNewOrder(Connection con, Scanner sc) throws SQLException {
        int id = readInt(sc, "Enter order ID: ");
        if (id <= 0) {
            System.out.println("Order ID must be a positive number.");
            return;
        }
        String customer = readText(sc, "Enter customer name: ");
        String product = readText(sc, "Enter product name: ");
        int qty = readInt(sc, "Enter quantity: ");
        double price = readDouble(sc, "Enter unit price: ");
        String statusInput = readText(sc, "Enter status (Pending/Shipped/Delivered/Cancelled): ");

        if (qty <= 0 || price <= 0) {
            System.out.println("Quantity and unit price must be greater than 0.");
            return;
        }
        String status = validStatus(statusInput);
        if (status == null) {
            System.out.println("Invalid status. Allowed: Pending, Shipped, Delivered, Cancelled.");
            return;
        }
        insertOrder(con, id, customer, product, qty, price, status);
    }

    // ---------------------------------------------------------------
    // b) Orders by status, quantity > 2, sorted by unit price DESC
    // ---------------------------------------------------------------
    private static void showOrdersByStatus(Connection con, Scanner sc) throws SQLException {
        String status = readText(sc, "Enter order status: ");

        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    if (!found) {
                        printHeader();
                        found = true;
                    }
                    printRow(rs);
                }
                if (!found) {
                    System.out.println("No orders found with status '" + status + "' and quantity greater than 2.");
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // c) Increase quantity
    // ---------------------------------------------------------------
    private static void increaseQuantity(Connection con, Scanner sc) throws SQLException {
        int id = readInt(sc, "Enter order ID: ");
        int extra = readInt(sc, "Enter additional quantity: ");

        if (extra <= 0) {
            System.out.println("Additional quantity must be greater than 0.");
            return;
        }

        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, extra);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("Order with ID " + id + " does not exist.");
            } else {
                System.out.println("Quantity increased by " + extra + ". Updated order:");
                printOrderById(con, id);
            }
        }
    }

    // ---------------------------------------------------------------
    // d) Update status (only if order exists and status is valid)
    // ---------------------------------------------------------------
    private static void updateStatus(Connection con, Scanner sc) throws SQLException {
        int id = readInt(sc, "Enter order ID: ");
        String newStatusInput = readText(sc, "Enter new status (Pending/Shipped/Delivered/Cancelled): ");

        if (!orderExists(con, id)) {
            System.out.println("Order with ID " + id + " does not exist.");
            return;
        }

        System.out.println("Current order details:");
        printOrderById(con, id);

        String newStatus = validStatus(newStatusInput);
        if (newStatus == null) {
            System.out.println("Invalid status '" + newStatusInput
                    + "'. Allowed: Pending, Shipped, Delivered, Cancelled. No update done.");
            return;
        }

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("Status updated successfully. Updated order details:");
            printOrderById(con, id);
        }
    }

    // ---------------------------------------------------------------
    // e) Total order value
    // ---------------------------------------------------------------
    private static void showTotalValue(Connection con) throws SQLException {
        String sql = "SELECT SUM(quantity * unit_price) AS total_value FROM Orders";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            double total = 0;
            if (rs.next()) {
                total = rs.getDouble("total_value");   // returns 0 when the table is empty (NULL)
            }
            System.out.printf("Total order value = %.2f%n", total);
        }
    }

    // ---------------------------------------------------------------
    // e) Delete order only if status is Cancelled
    // ---------------------------------------------------------------
    private static void deleteCancelledOrder(Connection con, Scanner sc) throws SQLException {
        showTotalValue(con);

        int id = readInt(sc, "Enter order ID to delete: ");

        String selectSql = "SELECT status FROM Orders WHERE order_id = ?";
        String status = null;
        try (PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    status = rs.getString("status");
                }
            }
        }

        if (status == null) {
            System.out.println("Order with ID " + id + " does not exist. Nothing deleted.");
            return;
        }

        if (!status.equalsIgnoreCase("Cancelled")) {
            System.out.println("Order " + id + " cannot be deleted because its status is '"
                    + status + "'. Only Cancelled orders can be deleted.");
            return;
        }

        String deleteSql = "DELETE FROM Orders WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Cancelled order " + id + " deleted successfully.");
        }
    }

    // ---------------------------------------------------------------
    // Extra: display all orders
    // ---------------------------------------------------------------
    private static void displayAllOrders(Connection con) throws SQLException {
        String sql = "SELECT * FROM Orders ORDER BY order_id";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                if (!found) {
                    printHeader();
                    found = true;
                }
                printRow(rs);
            }
            if (!found) {
                System.out.println("No orders in the table.");
            }
        }
    }
}