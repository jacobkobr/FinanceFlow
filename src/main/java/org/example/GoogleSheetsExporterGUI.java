import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.formdev.flatlaf.FlatDarculaLaf;



import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoogleSheetsExporterGUI {

    private JTextField checkAmountField;
    private JTextField checkDateField;
    private JTextField totalSavingsField;
    private JTextArea resultArea;
    private JTextField sheetIdField;
    private JPanel categoryPanel;
    private List<JTextField> categoryNameFields;
    private List<JTextField> percentageFields;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        SwingUtilities.invokeLater(() -> new GoogleSheetsExporterGUI().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("FinanceFlow");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon(getClass().getResource("/logo.png"));
        frame.setIconImage(icon.getImage());

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        sheetIdField = new JTextField();
        checkAmountField = new JTextField();
        checkDateField = new JTextField();
        totalSavingsField = new JTextField();

        panel.add(new JLabel("Google Sheet ID:"));
        panel.add(sheetIdField);
        panel.add(new JLabel("Check Amount:"));
        panel.add(checkAmountField);
        panel.add(new JLabel("Check Date (MM/DD/YYYY):"));
        panel.add(checkDateField);
        panel.add(new JLabel("Total Savings to Date:"));
        panel.add(totalSavingsField);

        JButton setCategoriesButton = new JButton("Set Categories");
        setCategoriesButton.addActionListener(e -> setCategories());
        panel.add(setCategoriesButton);

        JButton submitButton = new JButton("Submit Check");
        submitButton.addActionListener(e -> {
            try {
                exportCheckData();
            } catch (Exception ex) {
                ex.printStackTrace();
                resultArea.setText("Error: " + ex.getMessage());
            }
        });
        panel.add(submitButton);

        categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBorder(new TitledBorder("Categories"));

        resultArea = new JTextArea(5, 30);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(new TitledBorder("Results"));

        frame.add(panel, BorderLayout.NORTH);
        frame.add(categoryPanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void setCategories() {
        String input = JOptionPane.showInputDialog("How many categories?");
        int numCategories;
        try {
            numCategories = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            resultArea.setText("Invalid number of categories.");
            return;
        }

        categoryPanel.removeAll();
        categoryNameFields = new ArrayList<>();
        percentageFields = new ArrayList<>();

        for (int i = 0; i < numCategories; i++) {
            JPanel category = new JPanel(new GridLayout(1, 4, 5, 5));
            category.setBorder(new TitledBorder("Category " + (i + 1)));

            JTextField nameField = new JTextField("Category " + (i + 1));
            JTextField percentageField = new JTextField("0");

            category.add(new JLabel("Name:"));
            category.add(nameField);
            category.add(new JLabel("Percentage:"));
            category.add(percentageField);

            categoryNameFields.add(nameField);
            percentageFields.add(percentageField);
            categoryPanel.add(category);
        }

        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    private void exportCheckData() throws Exception {
        String spreadsheetId = sheetIdField.getText();

        if (spreadsheetId.isEmpty()) {
            resultArea.setText("Please enter a Google Sheet ID.");
            return;
        }

        double checkAmount = Double.parseDouble(checkAmountField.getText());
        String checkDate = checkDateField.getText();
        double totalSavings = Double.parseDouble(totalSavingsField.getText());

        List<List<Object>> values = new ArrayList<>();
        List<Object> headers = new ArrayList<>(Arrays.asList("Check Date", "Check Amount", "Total Savings"));
        List<Object> row = new ArrayList<>(Arrays.asList(checkDate, "$" + checkAmount, "$" + totalSavings));

        for (int i = 0; i < categoryNameFields.size(); i++) {
            String name = categoryNameFields.get(i).getText();
            double percentage = Double.parseDouble(percentageFields.get(i).getText()) / 100;
            double amount = checkAmount * percentage;

            headers.add(name + " (" + (percentage * 100) + "%)");
            row.add("$" + amount);
        }

        values.add(headers);
        values.add(row);

        ValueRange body = new ValueRange().setValues(values);

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = new Sheets.Builder(
                HTTP_TRANSPORT,
                GoogleSheetsAuthorization.getJsonFactory(),
                GoogleSheetsAuthorization.authorize()
        ).setApplicationName("Google Sheets API Java").build();

        sheetsService.spreadsheets().values().append(spreadsheetId, "Sheet1!A:E", body)
                .setValueInputOption("RAW")
                .execute();

        resultArea.setText("Check data submitted successfully.");
    }
}
