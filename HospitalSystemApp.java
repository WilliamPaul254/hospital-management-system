import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class HospitalSystemApp extends JFrame {
    private JButton btnPatients, btnDoctors, btnBilling, btnAppointments;

    public HospitalSystemApp(){
        setTitle("HOSPITAL SYSTEM APP");
        setSize(400,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 10, 10));

        btnPatients = new JButton("Patients");
        btnPatients.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { showPatientsForm();}
        });
        add(btnPatients);

        btnDoctors = new JButton("Doctors");
        btnDoctors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { showDoctorsForm();}
        });
        add(btnDoctors);

        btnBilling = new JButton("Billing");
        btnBilling.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { showBillingForm();}
        });
        add(btnBilling);

        btnAppointments = new JButton("Appointments");
        btnAppointments.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { showAppointmentsForm();}
        });
        add(btnAppointments);

    }

    private void showPatientsForm() {
        new PatientForm().setVisible(true);
        this.setVisible(false);  // Hide the main frame
    }

    private void showDoctorsForm() {
        new DoctorForm().setVisible(true);
        this.setVisible(false);  // Hide the main frame
    }

    private void showBillingForm() {
        new BillingForm().setVisible(true);
        this.setVisible(false);  // Hide the main frame
    }

    private void showAppointmentsForm() {
        new AppointmentForm().setVisible(true);
        this.setVisible(false);  // Hide the main frame
    }

    // Main method to launch the HospitalSystemApp
    public static void main(String[] args) {
        // Run the application in the event dispatch thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HospitalSystemApp().setVisible(true);  // Display the main window
            }
        });
    }

}
