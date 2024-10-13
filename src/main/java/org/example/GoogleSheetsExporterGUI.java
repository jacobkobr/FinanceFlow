import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GoogleSheetsExporterGUI {

    private JTextField checkAmountField;
    private JTextField checkDateField;
    private JTextField totalSavingsField;
    private JTextArea resultArea;
    private JTextField sheetIdField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GoogleSheetsExporterGUI().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("FinanceFlow");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon(getClass().getResource("/logo.png"));
        frame.setIconImage(icon.getImage());

        // Panel for input
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        // Sheet ID
        JLabel sheetIdLabel = new JLabel("Google Sheet ID:");
        sheetIdField = new JTextField();
        panel.add(sheetIdLabel);
        panel.add(sheetIdField);

        // check amount
        JLabel checkAmountLabel = new JLabel("Check Amount:");
        checkAmountField = new JTextField();
        panel.add(checkAmountLabel);
        panel.add(checkAmountField);

        // check date
        JLabel checkDateLabel = new JLabel("Check Date (MM/DD/YYYY):");
        checkDateField = new JTextField();
        panel.add(checkDateLabel);
        panel.add(checkDateField);

        // total savings
        JLabel totalSavingsLabel = new JLabel("Total Savings to Date:");
        totalSavingsField = new JTextField();
        panel.add(totalSavingsLabel);
        panel.add(totalSavingsField);

        // submit button
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

        // result display area
        resultArea = new JTextArea(5, 30);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Add components to the frame
        frame.add(panel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        // Show the frame
        frame.setVisible(true);
    }

    private void exportCheckData() throws Exception {
        String spreadsheetId = sheetIdField.getText();

        if (spreadsheetId.isEmpty()) {
            resultArea.setText("Please enter a Google Sheet ID.");
            return;
        }

        // Get user input
        double checkAmount = Double.parseDouble(checkAmountField.getText());
        String checkDate = checkDateField.getText();
        double totalSavings = Double.parseDouble(totalSavingsField.getText());

        // Calculate savings (35%) and food (15%)
        double savingsAmount = checkAmount * 0.35;
        double foodAmount = checkAmount * 0.15;

        // Prepare data with dollar signs for export
        String range = "Sheet1!A:E";
        List<List<Object>> values = Arrays.asList(
                Arrays.asList("Check Date", "Check Amount", "Total Savings", "Invest (35%)", "Food (15%)"),
                Arrays.asList(checkDate, "$" + checkAmount, "$" + totalSavings, "$" + savingsAmount, "$" + foodAmount)
        );
        ValueRange body = new ValueRange().setValues(values);

        // Connect to Google Sheets
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = new Sheets.Builder(
                HTTP_TRANSPORT,
                GoogleSheetsAuthorization.getJsonFactory(),
                GoogleSheetsAuthorization.authorize()
        ).setApplicationName("Google Sheets API Java").build();

        // Append data to the spreadsheet
        sheetsService.spreadsheets().values().append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        // Show result in the text area
        resultArea.setText("Check data submitted successfully:\n" +
                "Invest (35%): $" + savingsAmount + "\n" +
                "Food (15%): $" + foodAmount);
    }
}
