import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PatientForm extends JFrame {
    private JTextField PIdField;
    private JTextField PFNameField;
    private JTextField PLNameField;
    private JTextField PAgeField;
    private JTextField PDobField;
    private JButton btnSubmit, btnViewPatients, backButton;

    public PatientForm(){
        setTitle("PATIENTS");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(8, 2, 5,5));

        //initialize fields
        PIdField = new JTextField(20);
        PFNameField = new JTextField(20);
        PLNameField= new JTextField(20);
        PAgeField = new JTextField();
        PDobField = new JTextField();
        btnSubmit = new JButton("Submit");
        btnViewPatients = new JButton("View Patients");
        backButton = new JButton("Back to Main");

        //Add Components to the form
        add(new JLabel("Patient ID:"));
        add(PIdField);
        add(new JLabel("First Name:"));
        add(PFNameField);
        add(new JLabel("Last Name:"));
        add(PLNameField);
        add(new JLabel("AGE:"));
        add(PAgeField);
        add(new JLabel("Date of Birth:"));
        add(PDobField);
        add(btnSubmit);
        add(btnViewPatients);
        add(backButton);

        btnViewPatients.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                viewPatients();
            }
        });
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                registerPatient();
            }
        });
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Show the main frame and hide this form
                new HospitalSystemApp().setVisible(true);
                setVisible(false);
            }
        });
        //make the window visible
        setVisible(true);
    }
    private void registerPatient(){
        //get data from the fields
        String patient_id = PIdField.getText();
        String patient_firstname = PFNameField.getText();
        String patient_lastname = PLNameField.getText();
        String ageInput = PAgeField.getText();
        String dateOfBirth = PDobField.getText();

        try{
            int patient_age = Integer.parseInt(ageInput);
            if(patient_age<=0 || patient_age>120){
                JOptionPane.showMessageDialog(this, "Enter a valid age between 1- 120");
                return;
            }
        } catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "Invalid input");
            return;
        }

        LocalDate patient_dob;
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            patient_dob = LocalDate.parse(dateOfBirth, formatter);
        } catch(DateTimeParseException e){
            JOptionPane.showMessageDialog(this,"Invalid date, use format yyyy-MM-date");
            return;
        }

        try(Connection connection = DatabaseConnection.getConnection()){
            String sql ="INSERT INTO Patients(patient_id, patient_firstname, patient_lastname, patient_age, patient_dob ) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement statement= connection.prepareStatement(sql);
            statement.setString(1, patient_id);
            statement.setString(2, patient_firstname);
            statement.setString(3, patient_lastname);
            statement.setInt(4, Integer.parseInt(ageInput));
            statement.setDate(5, java.sql.Date.valueOf(patient_dob));
            int rowsInserted = statement.executeUpdate();

            if(rowsInserted>0){
                JOptionPane.showMessageDialog(this, "Registered successfully");
            }
        } catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Registration failed");
        }
    }

    private void viewPatients(){
        try(Connection connection = DatabaseConnection.getConnection()){
            String sql = "SELECT * FROM Patients";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            StringBuilder patientLists = new StringBuilder();
            // Process the result set row by row
            while(resultSet.next()){
                // retrieve data in each column
                String Pid= resultSet.getString("patient_id");
                String PFName = resultSet.getString("patient_firstname");
                String PLName = resultSet.getString("patient_lastname");
                int age = resultSet.getInt("patient_age");
                Date dateOfBirth = resultSet.getDate("patient_dob");

                //Formatting the date to a readable format
                LocalDate dob = dateOfBirth != null ? dateOfBirth.toLocalDate() : null;
                String dobFormatted = (dob != null) ? dob.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";

                //Append patient to the list
                patientLists.append("Patient ID").append(Pid)
                        .append("Name:").append(PFName).append(" ").append(PLName)
                        .append("Age").append(age)
                        .append("Date of Birth").append(dobFormatted).append("\n");// new line for next patient
            }
            if(patientLists.length()>12){// check length to account for header text
                JOptionPane.showMessageDialog(this, patientLists.toString(), "Patient List", JOptionPane.INFORMATION_MESSAGE);
            } else{ JOptionPane.showMessageDialog(this, " No patients found");}

        } catch(SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An Error occurred while Fetching Patients");
        }


    }




}
