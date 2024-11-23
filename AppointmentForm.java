import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class AppointmentForm extends JFrame {
    private JTextField appointmentDateField, appointmentTimeField, patientIdField;
    private JComboBox<String> doctorComboBox;
    private JButton viewAppointmentsButton, backButton, scheduleButton;

    public AppointmentForm() {
        setTitle("Appointments");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 2,5, 5));

        // Initialize the components
        appointmentDateField = new JTextField(15);
        appointmentTimeField = new JTextField(15);
        patientIdField = new JTextField(15);

        // Doctor ComboBox with doctors list
        doctorComboBox = new JComboBox<>(getDoctorNames());

        // View appointments button
        viewAppointmentsButton = new JButton("View Appointments");
        scheduleButton = new JButton("Schedule Appointment");

        // Add components to the frame
        add(new JLabel("Appointment Date (YYYY-MM-DD):"));
        add(appointmentDateField);

        add(new JLabel("Appointment Time (HH:MM):"));
        add(appointmentTimeField);

        add(new JLabel("Patient ID:"));
        add(patientIdField);

        add(new JLabel("Select Doctor:"));
        add(doctorComboBox);

        add(viewAppointmentsButton);
        add(scheduleButton);

        backButton = new JButton("Back to Main");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Show the main frame and hide this form
                new HospitalSystemApp().setVisible(true);
                setVisible(false);
            }
        });

        // Add the button to the form
        add(backButton);

        // Add action listener for the button
        viewAppointmentsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAppointments();
            }
        });

        scheduleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scheduleAppointments();
            }
        });
    }

    private void scheduleAppointments(){
        // Get selected doctor name and parse doctor id from it
        String selectedDoctor = (String) doctorComboBox.getSelectedItem();
        int doctorId = getDoctorIdByName(selectedDoctor);

        String appointmentDate = appointmentDateField.getText();
        String appointmentTime = appointmentTimeField.getText();
        String patientId = patientIdField.getText();

        if(appointmentDate.isEmpty() || appointmentTime.isEmpty()){
            JOptionPane.showMessageDialog(this, "Please enter both date and time");
            return;
        }

        if (!isValidPatientId(Integer.parseInt(patientId))) {
            JOptionPane.showMessageDialog(this, "Invalid Patient ID. Please check and try again.");
            return;
        }


        try(Connection connection = DatabaseConnection.getConnection()){
            String sql ="INSERT INTO APPOINTMENTS(patient_id, doctor_id, appointment_date, appointment_time) VALUES(?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, patientId);
            statement.setInt(2, doctorId);
            statement.setString(3, appointmentDate);
            statement.setString(4, appointmentTime);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment scheduled successfully");
        } catch(SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error Scheduling Appointment");
        }

    }
    // method to check if the patient id is in the database
    private boolean isValidPatientId(int patientId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM PATIENTS WHERE patient_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, patientId);
            ResultSet rs = statement.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Patient exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Patient not found
    }


    // Fetch doctor names from the database
    private String[] getDoctorNames() {
        ArrayList<String> doctors = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT doctor_firstname, doctor_lastname FROM DOCTORS");
            while (resultSet.next()) {
                String fullName = resultSet.getString("doctor_firstname") + " " + resultSet.getString("doctor_lastname");
                doctors.add(fullName);
            }

            // Check if no doctors were found and show a message
            if (doctors.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No doctors are available in the system.", "No Doctors", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors.toArray(new String[0]);
    }

    // Method to view appointments based on input filters
    private void viewAppointments() {
        String appointmentDate = appointmentDateField.getText().trim();
        String appointmentTime = appointmentTimeField.getText().trim();
        String patientId = patientIdField.getText().trim();
        String selectedDoctor = (String) doctorComboBox.getSelectedItem();

        // Build SQL query
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT a.appointment_id, d.doctor_firstname, d.doctor_lastname, " +
                        "a.appointment_date, a.appointment_time, " +
                        "p.patient_firstname, p.patient_lastname " +
                        "FROM APPOINTMENTS a " +
                        "JOIN DOCTORS d ON a.doctor_id = d.doctor_id " +
                        "JOIN PATIENTS p ON a.patient_id = p.patient_id WHERE 1=1 "
        );

        // storing the retrieved data in a list
        ArrayList<Object> parameters = new ArrayList<>();

        if (!appointmentDate.isEmpty()) {
            queryBuilder.append("AND a.appointment_date = ? ");
            parameters.add(appointmentDate);
        }
        if (!appointmentTime.isEmpty()) {
            queryBuilder.append("AND a.appointment_time = ? ");
            parameters.add(appointmentTime);
        }
        if (!patientId.isEmpty()) {
            queryBuilder.append("AND a.patient_id = ? ");
            parameters.add(patientId);
        }
        if (selectedDoctor != null) {
            int doctorId = getDoctorIdByName(selectedDoctor);
            queryBuilder.append("AND a.doctor_id = ? ");
            parameters.add(doctorId);
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            StringBuilder resultBuilder = new StringBuilder("Appointments:\n");

            while (rs.next()) {
                resultBuilder.append("Appointment ID: ").append(rs.getInt("appointment_id"))
                        .append(", Doctor: ").append(rs.getString("doctor_firstname")).append(" ")
                        .append(rs.getString("doctor_lastname"))
                        .append(", Date: ").append(rs.getString("appointment_date"))
                        .append(", Time: ").append(rs.getString("appointment_time"))
                        .append(", Patient: ").append(rs.getString("patient_firstname")).append(" ")
                        .append(rs.getString("patient_lastname")).append("\n");
            }

            // Display results
            if (resultBuilder.toString().equals("Appointments:\n")) {
                JOptionPane.showMessageDialog(this, "No appointments found.", "No Appointments", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JTextArea textArea = new JTextArea(resultBuilder.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                JOptionPane.showMessageDialog(this, scrollPane, "Available Appointments", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching appointments.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Get doctor ID based on the full name (used in the filter query)
    private int getDoctorIdByName(String fullName) {
        String[] nameParts = fullName.split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT doctor_id FROM DOCTORS WHERE doctor_firstname = ? AND doctor_lastname = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getInt("doctor_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the doctor is not found
    }
}