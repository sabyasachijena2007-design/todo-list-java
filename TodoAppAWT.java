import java.awt.*;
import java.awt.event.*;

public class TodoAppAWT extends Frame implements ActionListener {

    
    private TextField taskField;
    private Button addBtn, deleteBtn, completeBtn, clearBtn;
    private List taskList;

    public TodoAppAWT() {

        
        setTitle("To-Do List Manager (AWT)");
        setSize(500, 400);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        
        Panel topPanel = new Panel();
        topPanel.setLayout(new FlowLayout());

        taskField = new TextField(25);
        addBtn = new Button("Add Task");

        topPanel.add(new Label("Task: "));
        topPanel.add(taskField);
        topPanel.add(addBtn);

       
        taskList = new List(15); 

        
        Panel bottomPanel = new Panel();
        bottomPanel.setLayout(new FlowLayout());

        deleteBtn = new Button("Delete");
        completeBtn = new Button("Mark Completed");
        clearBtn = new Button("Clear All");

        bottomPanel.add(deleteBtn);
        bottomPanel.add(completeBtn);
        bottomPanel.add(clearBtn);

       
        add(topPanel, BorderLayout.NORTH);
        add(taskList, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        
        addBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        completeBtn.addActionListener(this);
        clearBtn.addActionListener(this);

        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    
    public void actionPerformed(ActionEvent e) {

        
        if (e.getSource() == addBtn) {
            String task = taskField.getText().trim();

            if (!task.isEmpty()) {
                taskList.add(task + " [Pending]");
                taskField.setText("");
            } else {
                showMessage("Task cannot be empty!");
            }
        }

        
        else if (e.getSource() == deleteBtn) {
            int index = taskList.getSelectedIndex();

            if (index != -1) {
                taskList.remove(index);
            } else {
                showMessage("Select a task to delete!");
            }
        }

        
        else if (e.getSource() == completeBtn) {
            int index = taskList.getSelectedIndex();

            if (index != -1) {
                String task = taskList.getItem(index);

                if (!task.contains("[Completed]")) {
                    task = task.replace("[Pending]", "[Completed]");
                    taskList.replaceItem(task, index);
                }
            } else {
                showMessage("Select a task first!");
            }
        }

        
        else if (e.getSource() == clearBtn) {
            taskList.removeAll();
        }
    }

    
    private void showMessage(String msg) {
        Dialog d = new Dialog(this, "Message", true);
        d.setLayout(new FlowLayout());
        d.setSize(250, 120);

        Label label = new Label(msg);
        Button ok = new Button("OK");

        ok.addActionListener(e -> d.dispose());

        d.add(label);
        d.add(ok);

        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    
    public static void main(String[] args) {
        new TodoAppAWT();
    }
}