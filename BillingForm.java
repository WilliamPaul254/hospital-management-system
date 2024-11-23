import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import  java.util.*;

public class BillingForm  extends JFrame {
    private JComboBox<String> specializationComboBox;
    private JCheckBox consultationCheckBox, drugIssuanceCheckBox, surgeryCheckBox;
    private JButton btnCalculate, backButton;
    private JLabel totalLabel;
    private JTextField patientIdField;
    private JButton CheckButton;
    private static final double REGISTRATION_FEE = 50.00;


    public BillingForm(){
        setTitle("Billing Form");
        setSize(400, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7,2,10,10));

        patientIdField = new JTextField();

        // add components to the frame
        add(new JLabel("Patient ID"));
        add(patientIdField);

        specializationComboBox = new JComboBox<>(getSpecializations());
        consultationCheckBox = new JCheckBox("Consultation");
        drugIssuanceCheckBox = new JCheckBox("Drug Issuance");
        surgeryCheckBox = new JCheckBox("Surgery");

        add(new JLabel("Select Specialization"));
        add(specializationComboBox);

        // to group the checkboxes under one panel
        JPanel servicesPanel= new JPanel();
        servicesPanel.setLayout(new BoxLayout(servicesPanel, BoxLayout.Y_AXIS));

        //label for services
        servicesPanel.add(new JLabel("Select Services"));
        servicesPanel.add(consultationCheckBox);
        servicesPanel.add(drugIssuanceCheckBox);
        servicesPanel.add(surgeryCheckBox);
        add(servicesPanel);// add the services panel to the frame

        CheckButton = new JButton("Check Patient");
        backButton = new JButton("Back to Main");

        CheckButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkPatientWithBilling();
            }
        });
        add(CheckButton);

        btnCalculate = new JButton("Calculate Total");
        btnCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateTotal();
            }
        });
        add(btnCalculate);

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Show the main frame and hide this form
                new HospitalSystemApp().setVisible(true);
                setVisible(false);
            }
        });
        add(backButton);

        totalLabel = new JLabel("Total:KSH0.00");
        add(totalLabel);
    }

    private void checkPatientWithBilling() {
        String patientId = patientIdField.getText().trim();
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Patient ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Simplified SQL to get patient details and outstanding balance
            String sql = "SELECT p.patient_firstname, p.patient_lastname, p.patient_age, p.patient_dob, "
                    + "(SELECT IFNULL(SUM(amount_owed), 0) FROM BILLING WHERE patient_id = p.patient_id) AS outstanding_balance "
                    + "FROM PATIENTS p WHERE p.patient_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, patientId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Retrieve patient details and outstanding balance
                    String patientName = resultSet.getString("patient_firstname") + " " + resultSet.getString("patient_lastname");
                    int age = resultSet.getInt("patient_age");
                    String dob = resultSet.getDate("patient_dob").toString();
                    double outstandingBalance = resultSet.getDouble("outstanding_balance");

                    // Format and display patient details
                    String patientDetails = "Patient Details:\n"
                            + "Name: " + patientName + "\n"
                            + "Age: " + age + "\n"
                            + "DOB: " + dob + "\n"
                            + "Outstanding Balance: KShs:" + String.format("%.2f", outstandingBalance);

                    JOptionPane.showMessageDialog(this, patientDetails, "Patient Billing Information", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Patient not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching patient billing information.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private String[] getSpecializations(){
        ArrayList<String> specializations = new ArrayList<>();
        try(Connection connection = DatabaseConnection.getConnection()) {
            Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT specialization_name FROM DoctorSpecialization");
                while(resultSet.next()){
                    specializations.add(resultSet.getString("specialization_name"));
                }
        } catch(SQLException e){ e.printStackTrace();}
        return specializations.toArray(new String[0]);
    }

    private void calculateTotal(){
        double total = REGISTRATION_FEE;
        String patientId = patientIdField.getText();

        String selectedSpecialization = (String) specializationComboBox.getSelectedItem();
        try(Connection connection= DatabaseConnection.getConnection()){
            if(consultationCheckBox.isSelected()){
                total += getServiceFee(connection, selectedSpecialization, "Consultation");
            }
            if(drugIssuanceCheckBox.isSelected()){
                total += getServiceFee(connection, selectedSpecialization, "Drug Issuance");
            }
            if(surgeryCheckBox.isSelected()){
                total += getServiceFee(connection, selectedSpecialization, "Surgical Admission");
            }
            totalLabel.setText("Total: Ksh" + total);

            //storing the total in the billing table
            updateAmountOwed(patientId, total);
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this,"ERROR CALCULATING TOTAL");
            e.printStackTrace();
        }

    }

    private double getServiceFee(Connection connection, String specialization, String serviceType) throws SQLException{
        String sql ="SELECT fee FROM SpecializationFees sf "+
                "JOIN DoctorSpecialization ds ON sf.specialization_id = ds.specialization_id "+
                "WHERE ds.specialization_name = ? AND sf.service_type = ?";

        try(PreparedStatement statement= connection.prepareStatement(sql)){
            statement.setString(1, specialization);
            statement.setString(2,serviceType);
            System.out.println("Generated Query: " + sql);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                return resultSet.getDouble("fee");
            }
        }
        return  0.0;
    }

    public void updateAmountOwed(String patientId, double amountOwed) {
        // Query to check if the patient has an existing billing record
        String checkSql = "SELECT COUNT(*) FROM BILLING WHERE patient_id = ?";
        String updateSql = "UPDATE BILLING SET amount_owed = amount_owed + ? WHERE patient_id = ?";
        String insertSql = "INSERT INTO BILLING (patient_id, amount_owed) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {

            // Check if the patient already has a billing record
            checkStatement.setString(1, patientId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                // If a record exists, update the amount owed
                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                    updateStatement.setDouble(1, amountOwed);
                    updateStatement.setString(2, patientId);
                    updateStatement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Amount owed updated successfully for patient ID: " + patientId);
                }
            } else {
                // If no record exists, insert a new one
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    insertStatement.setString(1, patientId);
                    insertStatement.setDouble(2, amountOwed);
                    insertStatement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "New Billing record created for patient ID: " + patientId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
