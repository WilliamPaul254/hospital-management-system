import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.*;

public class DoctorForm extends JFrame{
    private JTextField DidField, DFNameField, DLNameField, DAgeField;
    private JComboBox<String> DTypeComboBox, DSpecialization, DAvailableTime;
    private JButton btnViewDoctors, btnRegisterDoctor, backButton;

    public DoctorForm() {
        setTitle("DOCTORS");
        setSize(600, 400);
        setLayout(new GridLayout(10,2,5,5));

        DidField = new JTextField(20);
        DFNameField = new JTextField(20);
        DLNameField = new JTextField(20);
        DTypeComboBox = new JComboBox<>(new String[]{"Intern", "Employed"});
        DAgeField = new JTextField(3);
        DSpecialization = new JComboBox<>(new String[]{"Cardiologist", "Neurologist", "Oncologist", "Psychiatrist", "Dermatologist"});
        DAvailableTime = new JComboBox<>(new String[]{"Morning", "Afternoon", "Evening", "All Day"});

        //add components to the field
        add(new JLabel("DOCTOR ID"));
        add(DidField);
        add(new JLabel("First Name"));
        add(DFNameField);
        add(new JLabel("Last Name"));
        add(DLNameField);
        add(new JLabel("Doctor Type"));
        add(DTypeComboBox);
        add(new JLabel("Age"));
        add(DAgeField);
        add(new JLabel("Doctor Specialization"));
        add(DSpecialization);
        add(new JLabel("Available time"));
        add(DAvailableTime);

        btnViewDoctors = new JButton("View Doctors");
        btnViewDoctors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewDoctors();
            }
        });
        add(btnViewDoctors);

        btnRegisterDoctor = new JButton("Register Doctor");
        btnRegisterDoctor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerDoctor();
            }
        });
        add(btnRegisterDoctor);

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
        setVisible(true);
    }
        public void viewDoctors() {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "SELECT * FROM Doctors";
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();

                StringBuilder DoctorList = new StringBuilder();
                // processing the result se row by row
                while (resultSet.next()) {
                    //retrieve data in each column
                    String Did = resultSet.getString("doctor_Id");
                    String DFName = resultSet.getString("doctor_firstname");
                    String DLName = resultSet.getString("doctor_lastname");
                    String DType = resultSet.getString("doctor_type");
                    String Dage = resultSet.getString("doctor_age");
                    String DSpecialization = resultSet.getString("doctor_specialization");
                    String DAvailableTime = resultSet.getString("doctor_availabletime");

                    //append the doctor to the list
                    DoctorList.append("Doctor ID").append(Did)
                            .append("Name").append(DFName).append(" ").append(DLName)
                            .append("Type").append(DType)
                            .append("Dage").append(Dage)
                            .append("Specialization").append(DSpecialization)
                            .append("Available Time").append(DAvailableTime).append("\n");
                }
                if (DoctorList.length() > 12) {
                    JOptionPane.showMessageDialog(this, DoctorList.toString(), "Doctor List", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No Doctors found");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while fetching doctors");

            }

        }
        public void registerDoctor(){
            String Did = DidField.getText();
            String DFName = DFNameField.getText();
            String DLName = DLNameField.getText();
            String DType = (String) DTypeComboBox.getSelectedItem();
            String DAge = DidField.getText();
            String Specialization = (String) DSpecialization.getSelectedItem();
            String AvailableTime = (String) DAvailableTime.getSelectedItem();

            try{
                int doctor_age = Integer.parseInt(DAge);
                if(doctor_age<=0 || doctor_age>120){
                    JOptionPane.showMessageDialog(this, "Enter a valid age between 1- 120");
                    return;
                }
            } catch(NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input");
                return;
            }
            if(Did.isEmpty() || DFName.isEmpty() || DLName.isEmpty() || "Select Type".equals(DType) || DAge.isEmpty() || "Select Type".equals(Specialization) || "Select Type".equals(AvailableTime)){
                JOptionPane.showMessageDialog(this, "Fill all the fields");
                return;
            }
            try(Connection connection = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Doctors(doctor_id, doctor_firstname, doctor_lastname, doctor_type, doctor_age, doctor_specialization, doctor_availabletime) VALUES(?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, Did);
                statement.setString(2, DFName);
                statement.setString(3, DLName);
                statement.setString(4, DType);
                statement.setString(5, DAge);
                statement.setString(6, Specialization);
                statement.setString(7, AvailableTime);
                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Registered Successfully");
                }
            } catch (SQLException e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Registration failed");
            }

        }


}
