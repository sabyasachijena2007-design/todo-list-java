import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

import org.w3c.dom.events.MouseEvent;

public class TodoAppPremium {

    
    static final Color GOLD_BRIGHT   = new Color(255, 215,   0);
    static final Color GOLD_DARK     = new Color(184, 134,  11);
    static final Color GOLD_LIGHT    = new Color(255, 236, 120);
    static final Color GOLD_ACCENT   = new Color(212, 175,  55);
    static final Color BLACK_DEEP    = new Color(  8,   8,   8);
    static final Color BLACK_CARD    = new Color( 18,  18,  18);
    static final Color BLACK_PANEL   = new Color( 28,  28,  28);
    static final Color BLACK_BORDER  = new Color( 45,  45,  45);
    static final Color TEXT_PRIMARY  = new Color(255, 230, 100);
    static final Color TEXT_MUTED    = new Color(150, 130,  70);
    static final Color GREEN_DONE    = new Color( 80, 200, 100);
    static final Color RED_DELETE    = new Color(220,  70,  70);

 
    static Font FONT_TITLE, FONT_BODY, FONT_SMALL, FONT_ICON;

    private JFrame frame;
    private List<TaskItem> tasks = new ArrayList<>();
    private LocalDate selectedDate = LocalDate.now();
    private JPanel taskListPanel;
    private JPanel calendarPanel;
    private JTextField taskField;
    private JComboBox<String> priorityBox;
    private JPanel mainContent;
    private CardLayout cardLayout;
    private JLabel statusLabel;
    private int completedCount = 0;

    ─
    private List<Particle> particles = new ArrayList<>();
    private JPanel particleLayer;

   
    static class TaskItem {
        String text;
        boolean done;
        String priority;  
        LocalDate date;
        boolean checked;
        float fadeAlpha = 0f;
        int yOffset = 30;

        TaskItem(String text, String priority, LocalDate date) {
            this.text = text;
            this.priority = priority;
            this.date = date;
        }
    }

    static class Particle {
        float x, y, vx, vy, alpha, size;
        Particle(float x, float y) {
            this.x = x; this.y = y;
            this.vx = (float)(Math.random() * 4 - 2);
            this.vy = (float)(Math.random() * -3 - 1);
            this.alpha = 1f;
            this.size = (float)(Math.random() * 6 + 3);
        }
        boolean update() {
            x += vx; y += vy; vy += 0.08f;
            alpha -= 0.025f;
            return alpha > 0;
        }
    }

    public TodoAppPremium() {
        loadFonts();
        buildFrame();
        buildUI();
        startAnimationLoop();
        animateIn();
    }

    private void loadFonts() {
        FONT_TITLE = new Font("Georgia", Font.BOLD, 22);
        FONT_BODY  = new Font("Georgia", Font.PLAIN, 14);
        FONT_SMALL = new Font("Monospaced", Font.BOLD, 11);
        FONT_ICON  = new Font("Dialog", Font.BOLD, 16);
    }

   
    private void buildFrame() {
        frame = new JFrame("◆SOMU TASKS");
        frame.setSize(860, 680);
        frame.setMinimumSize(new Dimension(760, 580));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setUndecorated(true);

   
        JPanel titleBar = buildTitleBar();
        frame.add(titleBar, BorderLayout.NORTH);
        frame.getContentPane().setBackground(BLACK_DEEP);
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, BLACK_CARD, getWidth(), 0, new Color(35,30,10));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(GOLD_DARK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        bar.setPreferredSize(new Dimension(0, 48));
        bar.setBorder(new EmptyBorder(0, 18, 0, 12));

    
        JLabel logo = new JLabel("◆ TO-DO LIST APP");
        logo.setFont(FONT_TITLE);
        logo.setForeground(GOLD_BRIGHT);

       
        JPanel controls =  // Window controlsnew JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        JButton minBtn  = makeDotBtn(new Color(255, 189, 68),  "─");
        JButton closeBtn= makeDotBtn(RED_DELETE, "✕");
        minBtn.addActionListener(e -> frame.setExtendedState(JFrame.ICONIFIED));
        closeBtn.addActionListener(e -> System.exit(0));
        controls.add(minBtn);
        controls.add(closeBtn);

        bar.add(logo, BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);

       
        Point[] drag = {null};
        bar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { drag[0] = e.getPoint(); }
        });
        bar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = frame.getLocation();
                frame.setLocation(loc.x + e.getX() - drag[0].x,
                                  loc.y + e.getY() - drag[0].y);
            }
        });
        return bar;
    }

    private JButton makeDotBtn(Color color, String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.brighter() : color);
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Dialog", Font.BOLD, 9));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, (getWidth()-fm.stringWidth(label))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BLACK_DEEP);

       
        JPanel sidebar = buildSidebar();

      
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(BLACK_DEEP);
        mainContent.add(buildTasksPage(), "TASKS");
        mainContent.add(buildCalendarPage(), "CALENDAR");

        root.add(sidebar, BorderLayout.WEST);
        root.add(mainContent, BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        frame.add(root, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, BLACK_CARD, 0, getHeight(), BLACK_PANEL);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(GOLD_DARK);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
            }
        };
        side.setPreferredSize(new Dimension(200, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(24, 0, 24, 0));

     
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth()/2, cy = 44;
            
                for (int i = 5; i > 0; i--) {
                    g2.setColor(new Color(255,215,0, 15*i));
                    g2.fillOval(cx-35-i*2, cy-35-i*2, (35+i*2)*2, (35+i*2)*2);
                }
             
                GradientPaint gp = new GradientPaint(cx-35,cy-35, GOLD_BRIGHT, cx+35, cy+35, GOLD_DARK);
                g2.setPaint(gp);
                g2.fillOval(cx-35, cy-35, 70, 70);
                g2.setColor(BLACK_DEEP);
                g2.setFont(new Font("Georgia", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("A", cx - fm.stringWidth("A")/2, cy + fm.getAscent()/2 - 4);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(200, 100));
        avatar.setMaximumSize(new Dimension(200, 100));

        side.add(avatar);
        side.add(Box.createVerticalStrut(8));

       JLabel name = centeredLabel("SOMU", new Font("Georgia", Font.BOLD, 14), GOLD_BRIGHT);
        JLabel sub  = centeredLabel("To - Do App", FONT_SMALL, TEXT_MUTED);
        side.add(name);
        side.add(sub);
        side.add(Box.createVerticalStrut(28));

     
        String[][] navItems = {{"📋", "My Tasks", "TASKS"}, {"📅", "Calendar", "CALENDAR"}};
        for (String[] item : navItems) {
            JButton btn = buildNavButton(item[0], item[1], item[2]);
            side.add(btn);
            side.add(Box.createVerticalStrut(6));
        }

        side.add(Box.createVerticalGlue());

      
        JPanel stats = buildSidebarStats();
        side.add(stats);

        return side;
    }

    private JButton buildNavButton(String icon, String label, String page) {
        JButton btn = new JButton() {
            boolean hovered = false;
            { 
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setAlignmentX(CENTER_ALIGNMENT);
                setMaximumSize(new Dimension(180, 46));
                setPreferredSize(new Dimension(180, 46));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { cardLayout.show(mainContent, page); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    GradientPaint gp = new GradientPaint(0,0,new Color(255,215,0,30), getWidth(),0,new Color(0,0,0,0));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(GOLD_ACCENT);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                    g2.fillRect(0, 8, 3, getHeight()-16);
                }
                g2.setFont(new Font("Dialog", Font.PLAIN, 16));
                g2.setColor(hovered ? GOLD_BRIGHT : TEXT_MUTED);
                g2.drawString(icon + "  " + label, 18, getHeight()/2 + 6);
            }
        };
        return btn;
    }

    private JPanel buildSidebarStats() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLACK_BORDER);
                g2.fillRoundRect(10, 5, getWidth()-20, getHeight()-10, 14, 14);
                g2.setColor(GOLD_DARK);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(10, 5, getWidth()-20, getHeight()-10, 14, 14);

                g2.setFont(FONT_SMALL);
                g2.setColor(TEXT_MUTED);
                g2.drawString("TASKS TODAY", 22, 28);

                int total = tasks.stream().filter(t -> t.date.equals(LocalDate.now())).mapToInt(t->1).sum();
                int done  = tasks.stream().filter(t -> t.date.equals(LocalDate.now()) && t.done).mapToInt(t->1).sum();

                g2.setFont(new Font("Georgia", Font.BOLD, 28));
                g2.setColor(GOLD_BRIGHT);
                g2.drawString(done + "/" + total, 22, 62);

                
                int barW = getWidth() - 44;
                g2.setColor(BLACK_PANEL);
                g2.fillRoundRect(22, 72, barW, 6, 6, 6);
                if (total > 0) {
                    GradientPaint gp = new GradientPaint(22,72, GOLD_DARK, 22 + barW * done / total, 72, GOLD_BRIGHT);
                    g2.setPaint(gp);
                    g2.fillRoundRect(22, 72, barW * done / total, 6, 6, 6);
                }
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(200, 96));
        p.setMaximumSize(new Dimension(200, 96));
        return p;
    }

  
    private JPanel buildTasksPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BLACK_DEEP);
        page.setBorder(new EmptyBorder(24, 24, 0, 24));

      
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel h = new JLabel("My Tasks");
        h.setFont(new Font("Georgia", Font.BOLD, 26));
        h.setForeground(GOLD_BRIGHT);
        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setForeground(TEXT_MUTED);
        header.add(h, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);

       
        JPanel inputCard = buildInputCard();

        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(BLACK_DEEP);

        JScrollPane scroll = new JScrollPane(taskListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new GoldScrollBarUI());
        scroll.getVerticalScrollBar().setBackground(BLACK_DEEP);

        page.add(header, BorderLayout.NORTH);
        page.add(inputCard, BorderLayout.SOUTH);
        page.add(scroll, BorderLayout.CENTER);

        return page;
    }

    private JPanel buildInputCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
                for (int i = 3; i > 0; i--) {
                    g2.setColor(new Color(255,215,0,10*i));
                    g2.setStroke(new BasicStroke(i*2f));
                    g2.drawRoundRect(i, i, getWidth()-i*2, getHeight()-i*2, 18, 18);
                }
                g2.setColor(BLACK_CARD);
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 16, 16);
                g2.setColor(GOLD_DARK);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 16, 16);
            }
        };
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 14));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 64));

        taskField = new JTextField(18);
        styleTextField(taskField, "✏  Add a new task...");

        priorityBox = new JComboBox<>(new String[]{"🔴 HIGH", "🟡 MED", "🟢 LOW"});
        styleCombo(priorityBox);

        JButton addBtn = buildGoldButton("＋  ADD TASK");
        addBtn.addActionListener(e -> addTask());
        taskField.addActionListener(e -> addTask());

        card.add(taskField);
        card.add(priorityBox);
        card.add(addBtn);

        return card;
    }

    private void addTask() {
        String text = taskField.getText().trim();
        if (text.isEmpty()) {
            shakeField(taskField);
            return;
        }
        String pri = ((String)priorityBox.getSelectedItem()).substring(3);
        TaskItem t = new TaskItem(text, pri, selectedDate);
        tasks.add(t);
        taskField.setText("");
        refreshTaskList();
        spawnParticles(taskField.getLocationOnScreen().x, taskField.getLocationOnScreen().y);
        updateStatus();
    }

    private void refreshTaskList() {
        taskListPanel.removeAll();
        taskListPanel.add(Box.createVerticalStrut(8));
        for (int i = 0; i < tasks.size(); i++) {
            TaskItem task = tasks.get(i);
            JPanel row = buildTaskRow(task, i);
            taskListPanel.add(row);
            taskListPanel.add(Box.createVerticalStrut(8));
           
            final float[] alpha = {0};
            final int[] yOff = {20};
            Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    alpha[0] = Math.min(1f, alpha[0] + 0.08f);
                    yOff[0]  = Math.max(0, yOff[0] - 2);
                    if (alpha[0] >= 1f) t.cancel();
                    SwingUtilities.invokeLater(() -> row.repaint());
                }
            }, 0, 16);
        }
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel buildTaskRow(TaskItem task, int index) {
        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               
                g2.setColor(task.done ? new Color(30, 50, 30) : BLACK_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
               
                Color stripeColor = task.priority.equals("HIGH") ? new Color(220,70,70)
                        : task.priority.equals("MED")  ? GOLD_DARK : GREEN_DONE;
                g2.setColor(stripeColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
               
                g2.setColor(task.done ? new Color(80,120,80) : BLACK_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row.setPreferredSize(new Dimension(0, 58));
        row.setBorder(new EmptyBorder(0, 12, 0, 12));

        
        JCheckBox check = new JCheckBox();
        check.setSelected(task.done);
        check.setOpaque(false);
        check.setUI(new GoldCheckBoxUI());
        check.addActionListener(e -> {
            task.done = check.isSelected();
            updateStatus();
            refreshTaskList();
        });

      
        JLabel label = new JLabel(task.done ? "<html><strike>" + task.text + "</strike></html>" : task.text);
        label.setFont(new Font("Georgia", Font.PLAIN, 14));
        label.setForeground(task.done ? TEXT_MUTED : TEXT_PRIMARY);

   
        JLabel dateBadge = new JLabel(task.date.format(DateTimeFormatter.ofPattern("MMM d")));
        dateBadge.setFont(FONT_SMALL);
        dateBadge.setForeground(TEXT_MUTED);

       
        JButton del = new JButton("✕") {
            boolean hov = false;
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setFont(new Font("Dialog", Font.BOLD, 12));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov=true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hov=false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hov) { g2.setColor(new Color(220,70,70,80)); g2.fillOval(0,0,getWidth(),getHeight()); }
                g2.setColor(hov ? RED_DELETE : TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("✕", (getWidth()-fm.stringWidth("✕"))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        del.setPreferredSize(new Dimension(30, 30));
        del.addActionListener(e -> {
            tasks.remove(index);
            refreshTaskList();
            updateStatus();
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(check);
        left.add(label);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(dateBadge);
        right.add(del);

        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

       
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { row.setBorder(new CompoundBorder(
                new LineBorder(GOLD_DARK, 1, true), new EmptyBorder(0, 12, 0, 12))); row.repaint(); }
            public void mouseExited(MouseEvent e) { row.setBorder(new EmptyBorder(0, 12, 0, 12)); row.repaint(); }
        });

        return row;
    }

   
    private JPanel buildCalendarPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BLACK_DEEP);
        page.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel h = new JLabel("Calendar View");
        h.setFont(new Font("Georgia", Font.BOLD, 26));
        h.setForeground(GOLD_BRIGHT);

        calendarPanel = buildCalendarWidget();

        
        JPanel dayTasks = new JPanel(new BorderLayout());
        dayTasks.setBackground(BLACK_CARD);
        dayTasks.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(GOLD_DARK, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        JLabel dl = new JLabel("Tasks for selected date will appear here");
        dl.setFont(FONT_BODY);
        dl.setForeground(TEXT_MUTED);
        dayTasks.add(dl, BorderLayout.CENTER);

        page.add(h, BorderLayout.NORTH);
        page.add(calendarPanel, BorderLayout.CENTER);
        page.add(dayTasks, BorderLayout.SOUTH);

        return page;
    }

    private JPanel buildCalendarWidget() {
        JPanel cal = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLACK_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(GOLD_DARK);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        cal.setOpaque(false);
        cal.setBorder(new EmptyBorder(20, 20, 20, 20));
        cal.add(buildCalHeader(), BorderLayout.NORTH);
        cal.add(buildCalGrid(), BorderLayout.CENTER);
        return cal;
    }

    private YearMonth calMonth = YearMonth.now();
    private JPanel calGridContainer;

    private JPanel buildCalHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);

        JButton prev = makeCalNavBtn("◀");
        JButton next = makeCalNavBtn("▶");
        JLabel monthLabel = new JLabel(calMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")), JLabel.CENTER);
        monthLabel.setFont(new Font("Georgia", Font.BOLD, 18));
        monthLabel.setForeground(GOLD_BRIGHT);

        prev.addActionListener(e -> { calMonth = calMonth.minusMonths(1); rebuildCalGrid(monthLabel); });
        next.addActionListener(e -> { calMonth = calMonth.plusMonths(1); rebuildCalGrid(monthLabel); });

        hdr.add(prev, BorderLayout.WEST);
        hdr.add(monthLabel, BorderLayout.CENTER);
        hdr.add(next, BorderLayout.EAST);
        return hdr;
    }

    private void rebuildCalGrid(JLabel monthLabel) {
        monthLabel.setText(calMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendarPanel.remove(calGridContainer);
        calGridContainer = buildCalGrid();
        calendarPanel.add(calGridContainer, BorderLayout.CENTER);
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel buildCalGrid() {
        calGridContainer = new JPanel(new GridLayout(7, 7, 6, 6));
        calGridContainer.setOpaque(false);

        String[] days = {"SUN","MON","TUE","WED","THU","FRI","SAT"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, JLabel.CENTER);
            lbl.setFont(FONT_SMALL);
            lbl.setForeground(GOLD_DARK);
            calGridContainer.add(lbl);
        }

        LocalDate first = calMonth.atDay(1);
        int startDay = first.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < startDay; i++) calGridContainer.add(new JLabel());

        for (int d = 1; d <= calMonth.lengthOfMonth(); d++) {
            final LocalDate date = calMonth.atDay(d);
            boolean isToday = date.equals(LocalDate.now());
            boolean isSel   = date.equals(selectedDate);
            long hasTasks = tasks.stream().filter(t -> t.date.equals(date)).count();

            JButton btn = new JButton(String.valueOf(d)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSel) {
                        GradientPaint gp = new GradientPaint(0,0, GOLD_DARK, getWidth(), getHeight(), GOLD_BRIGHT);
                        g2.setPaint(gp);
                        g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                        g2.setColor(BLACK_DEEP);
                    } else if (isToday) {
                        g2.setColor(new Color(255,215,0,50));
                        g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                        g2.setColor(GOLD_BRIGHT);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawOval(2, 2, getWidth()-4, getHeight()-4);
                        g2.setColor(GOLD_BRIGHT);
                    } else {
                        if (getModel().isRollover()) {
                            g2.setColor(new Color(255,215,0,25));
                            g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                        }
                        g2.setColor(TEXT_PRIMARY);
                    }
                    g2.setFont(new Font("Georgia", Font.BOLD, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    String s = String.valueOf(date.getDayOfMonth());
                    g2.drawString(s, (getWidth()-fm.stringWidth(s))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                
                    if (hasTasks > 0) {
                        g2.setColor(isSel ? BLACK_DEEP : GOLD_BRIGHT);
                        g2.fillOval(getWidth()/2-3, getHeight()-8, 6, 6);
                    }
                }
            };
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                selectedDate = date;
                refreshTaskList();
            });
            calGridContainer.add(btn);
        }
        return calGridContainer;
    }

    private JButton makeCalNavBtn(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(255,215,0,40) : new Color(0,0,0,0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(GOLD_BRIGHT);
                g2.setFont(new Font("Dialog", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, (getWidth()-fm.stringWidth(label))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

   
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(BLACK_CARD);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(GOLD_DARK);
                g2.drawLine(0,0,getWidth(),0);
            }
        };
        bar.setPreferredSize(new Dimension(0, 30));
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));

        statusLabel = new JLabel("Ready  ◆  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy")));
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_MUTED);

        JLabel ver = new JLabel("SOMU TASKS v2.0");
        ver.setFont(FONT_SMALL);
        ver.setForeground(new Color(90, 75, 30));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(ver, BorderLayout.EAST);
        return bar;
    }

    private void updateStatus() {
        long done = tasks.stream().filter(t -> t.done).count();
        statusLabel.setText("Tasks: " + tasks.size() + "  ◆  Completed: " + done + "  ◆  Pending: " + (tasks.size()-done));
    }

  
    private JButton buildGoldButton(String text) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            {
                setContentAreaFilled(false); setBorderPainted(false);
                setFocusPainted(false); setFont(new Font("Georgia", Font.BOLD, 12));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov=true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hov=false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, hov ? GOLD_BRIGHT : GOLD_DARK,
                        getWidth(), getHeight(), hov ? GOLD_DARK : new Color(100,80,0));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(BLACK_DEEP);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setPreferredSize(new Dimension(130, 38));
        return btn;
    }

    private void styleTextField(JTextField tf, String placeholder) {
        tf.setBackground(BLACK_PANEL);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(GOLD_BRIGHT);
        tf.setFont(new Font("Georgia", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(GOLD_DARK, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        tf.setPreferredSize(new Dimension(260, 38));
        tf.putClientProperty("placeholder", placeholder);
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(BLACK_PANEL);
        cb.setForeground(GOLD_BRIGHT);
        cb.setFont(new Font("Georgia", Font.PLAIN, 12));
        cb.setBorder(new LineBorder(GOLD_DARK, 1, true));
        cb.setPreferredSize(new Dimension(110, 38));
    }

    private JLabel centeredLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(200, 24));
        return l;
    }

    private void shakeField(JTextField tf) {
        Point orig = tf.getLocation();
        Timer t = new Timer();
        int[] count = {0};
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                int dx = (count[0] % 2 == 0) ? 5 : -5;
                SwingUtilities.invokeLater(() -> tf.setLocation(orig.x + dx, orig.y));
                if (++count[0] >= 8) { t.cancel(); SwingUtilities.invokeLater(() -> tf.setLocation(orig)); }
            }
        }, 0, 40);
    }

    private void spawnParticles(int x, int y) {
        for (int i = 0; i < 18; i++) particles.add(new Particle(x, y));
    }

    private void animateIn() {
        frame.setOpacity(0f);
        Timer t = new Timer();
        float[] op = {0f};
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                op[0] = Math.min(1f, op[0] + 0.05f);
                SwingUtilities.invokeLater(() -> frame.setOpacity(op[0]));
                if (op[0] >= 1f) t.cancel();
            }
        }, 0, 16);
    }

    private void startAnimationLoop() {
        Timer t = new Timer(true);
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                particles.removeIf(p -> !p.update());
                SwingUtilities.invokeLater(() -> frame.repaint());
            }
        }, 0, 16);
    }

    static class GoldCheckBoxUI extends javax.swing.plaf.basic.BasicCheckBoxUI {
        @Override public synchronized void paint(Graphics g, JComponent c) {
            JCheckBox cb = (JCheckBox) c;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = 20;
            if (cb.isSelected()) {
                GradientPaint gp = new GradientPaint(0,0, GOLD_DARK, s, s, GOLD_BRIGHT);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, s, s, 6, 6);
                g2.setColor(BLACK_DEEP);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(4, 10, 8, 15);
                g2.drawLine(8, 15, 16, 5);
            } else {
                g2.setColor(BLACK_PANEL);
                g2.fillRoundRect(0, 0, s, s, 6, 6);
                g2.setColor(GOLD_DARK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, s-1, s-1, 6, 6);
            }
        }
        @Override public Dimension getPreferredSize(JComponent c) { return new Dimension(22, 22); }
    }

    static class GoldScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = GOLD_DARK;
            trackColor = BLACK_PANEL;
        }
        @Override protected JButton createDecreaseButton(int o) { return noButton(); }
        @Override protected JButton createIncreaseButton(int o) { return noButton(); }
        private JButton noButton() {
            JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(r.x,r.y, GOLD_DARK, r.x+r.width,r.y+r.height, GOLD_BRIGHT);
            g2.setPaint(gp);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
        }
    }

  
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            new TodoAppPremium();
        });
    }
}
