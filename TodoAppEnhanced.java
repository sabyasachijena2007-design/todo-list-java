import java.awt.*;
import javax.swing.*;

public class TodoAppEnhanced {

    private JFrame frame;
    private JTextField taskField;
    private DefaultListModel<String> taskModel;
    private JList<String> taskList;

    public TodoAppEnhanced() {

        
        frame = new JFrame("✨ To-Do List Manager");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(30, 144, 255)); 

        taskField = new JTextField(20);
        JButton addBtn = new JButton("➕ Add");

        styleButton(addBtn);

        topPanel.add(taskField);
        topPanel.add(addBtn);

        
        taskModel = new DefaultListModel<>();
        taskList = new JList<>(taskModel);
        taskList.setFont(new Font("Arial", Font.BOLD, 14));
        taskList.setSelectionBackground(new Color(173, 216, 230));

        JScrollPane scrollPane = new JScrollPane(taskList);

        
        JPanel bottomPanel = new JPanel();

        JButton deleteBtn = new JButton("❌ Delete");
        JButton completeBtn = new JButton("✔ Complete");
        JButton clearBtn = new JButton("🧹 Clear");

        styleButton(deleteBtn);
        styleButton(completeBtn);
        styleButton(clearBtn);

        bottomPanel.add(deleteBtn);
        bottomPanel.add(completeBtn);
        bottomPanel.add(clearBtn);

        
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);


        
        addBtn.addActionListener(e -> {
            String task = taskField.getText().trim();

            if (!task.isEmpty()) {
                taskModel.addElement("🟡 " + task);
                taskField.setText("");

                flashEffect(frame); 
            } else {
                JOptionPane.showMessageDialog(frame, "Task cannot be empty!");
            }
        });

        
        deleteBtn.addActionListener(e -> {
            int index = taskList.getSelectedIndex();

            if (index != -1) {
                taskModel.remove(index);
            } else {
                JOptionPane.showMessageDialog(frame, "Select a task!");
            }
        });

       
        completeBtn.addActionListener(e -> {
            int index = taskList.getSelectedIndex();

            if (index != -1) {
                String task = taskModel.get(index);

                if (!task.contains("✔")) {
                    taskModel.set(index, "✔ " + task.replace("🟡 ", ""));
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Select a task!");
            }
        });

       
        clearBtn.addActionListener(e -> taskModel.clear());

        frame.setVisible(true);
    }

   
    private void styleButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
    }

   
    private void flashEffect(JFrame frame) {
        Color original = frame.getContentPane().getBackground();

        new Thread(() -> {
            try {
                frame.getContentPane().setBackground(Color.GREEN);
                Thread.sleep(100);
                frame.getContentPane().setBackground(original);
            } catch (Exception ignored) {}
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoAppEnhanced::new);
    }
}
