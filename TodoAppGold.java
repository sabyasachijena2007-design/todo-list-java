import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

public class TodoAppGold {

    static final Color GOLD        = new Color(255, 215,   0);
    static final Color GOLD_DIM    = new Color(184, 134,  11);
    static final Color GOLD_SOFT   = new Color(212, 175,  55);
    static final Color BG_DEEP     = new Color(  8,   8,   8);
    static final Color BG_CARD     = new Color( 20,  20,  20);
    static final Color BG_PANEL    = new Color( 30,  30,  30);
    static final Color BORDER_COL  = new Color( 50,  50,  50);
    static final Color TEXT_GOLD   = new Color(255, 225, 100);
    static final Color TEXT_MUTED  = new Color(130, 110,  60);
    static final Color GREEN_OK    = new Color( 72, 199,  89);
    static final Color RED_DEL     = new Color(210,  60,  60);

    private final JFrame       frame        = new JFrame();
    private final List<Task>   tasks        = new ArrayList<>();
    private       LocalDate    pickedDate   = LocalDate.now();
    private       YearMonth    calMonth     = YearMonth.now();

    private JTextField        taskField;
    private JComboBox<String> priorityBox;
    private JPanel            taskListPanel;
    private JPanel            calGridWrap;
    private JLabel            calTitle;
    private JLabel            statusLabel;
    private JPanel            mainCards;
    private CardLayout        cardLayout;
    private JPanel            sidebarStatsCard;
    private JButton           activeNavButton  = null;

    public TodoAppGold() {
        buildFrame();
        buildUI();
        loadTasksFromDB();
        refreshTaskList();
        fadeIn();
    }

    private void buildFrame() {
        frame.setSize(880, 680);
        frame.setMinimumSize(new Dimension(760, 580));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setUndecorated(true);
        frame.getContentPane().setBackground(BG_DEEP);
        frame.setLayout(new BorderLayout());
    }

    private void buildUI() {
        frame.add(titleBar(),  BorderLayout.NORTH);
        frame.add(sidebar(),   BorderLayout.WEST);
        frame.add(content(),   BorderLayout.CENTER);
        frame.add(statusBar(), BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void loadTasksFromDB() {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM tasks";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            tasks.clear();

            while (rs.next()) {
                String text        = rs.getString("text");
                String priority    = rs.getString("priority");
                boolean done       = rs.getBoolean("done");
                java.sql.Date date = rs.getDate("date");
                int id             = rs.getInt("id");

                tasks.add(new Task(id, text, priority, done, date.toLocalDate()));
            }

            System.out.println("DATA LOADED FROM DB");

        } catch (Exception e) {
            e.printStackTrace();
        }
        updateStatus();
    }

    private JPanel titleBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, BG_CARD, getWidth(), 0, new Color(28, 24, 6)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(GOLD_DIM);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 48));
        bar.setBorder(new EmptyBorder(0, 20, 0, 12));

        JLabel logo = new JLabel("◆TO-DO LIST APP");
        logo.setFont(new Font("Georgia", Font.BOLD, 20));
        logo.setForeground(GOLD);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 11));
        controls.setOpaque(false);
        controls.add(dotButton(new Color(255, 189, 68), "–", e -> frame.setExtendedState(JFrame.ICONIFIED)));
        controls.add(dotButton(RED_DEL, "✕", e -> System.exit(0)));

        bar.add(logo,     BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);

        Point[] anchor = {null};
        bar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { anchor[0] = e.getPoint(); }
        });
        bar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (anchor[0] != null) {
                    Point loc = frame.getLocation();
                    frame.setLocation(loc.x + e.getX() - anchor[0].x,
                                      loc.y + e.getY() - anchor[0].y);
                }
            }
        });
        return bar;
    }

    private JPanel sidebar() {
        JPanel side = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, BG_CARD, 0, getHeight(), BG_PANEL));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(GOLD_DIM);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(200, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(24, 0, 24, 0));

        side.add(avatarArea());
        side.add(Box.createVerticalStrut(24));
        JButton myTasksBtn = navBtn("📋  My Tasks",  "TASKS");
        activeNavButton = myTasksBtn;
        side.add(myTasksBtn);
        side.add(Box.createVerticalStrut(6));
        side.add(navBtn("📅  Calendar",  "CALENDAR"));
        side.add(Box.createVerticalGlue());
        sidebarStatsCard = statsCard();
        side.add(sidebarStatsCard);
        return side;
    }

    private JPanel avatarArea() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = 44;
                for (int i = 4; i > 0; i--) {
                    g2.setColor(new Color(255, 215, 0, 12 * i));
                    g2.fillOval(cx - 36 - i * 2, cy - 36 - i * 2, (36 + i * 2) * 2, (36 + i * 2) * 2);
                }
                g2.setPaint(new GradientPaint(cx - 36, cy - 36, GOLD, cx + 36, cy + 36, GOLD_DIM));
                g2.fillOval(cx - 36, cy - 36, 72, 72);
                g2.setColor(BG_DEEP);
                g2.setFont(new Font("Georgia", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("A", cx - fm.stringWidth("A") / 2, cy + fm.getAscent() / 2 - 3);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(200, 100));
        p.setMaximumSize(new Dimension(200, 100));

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.add(p);

        JLabel name = new JLabel("GOLD", JLabel.CENTER);
        name.setFont(new Font("Georgia", Font.BOLD, 13));
        name.setForeground(GOLD);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Premium Tasks", JLabel.CENTER);
        sub.setFont(new Font("Monospaced", Font.PLAIN, 10));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        wrap.add(name);
        wrap.add(Box.createVerticalStrut(2));
        wrap.add(sub);
        return wrap;
    }

    private JButton navBtn(String label, String page) {
        JButton btn = new JButton() {
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setAlignmentX(CENTER_ALIGNMENT);
                setMaximumSize(new Dimension(186, 44));
                setPreferredSize(new Dimension(186, 44));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = (this == activeNavButton);
                boolean isHover  = getModel().isRollover();
                if (isActive) {
                    g2.setPaint(new GradientPaint(0, 0, new Color(255, 215, 0, 55), getWidth(), 0, new Color(0, 0, 0, 0)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(GOLD);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                    g2.setColor(GOLD);
                    g2.fillRect(0, 8, 3, getHeight() - 16);
                } else if (isHover) {
                    g2.setPaint(new GradientPaint(0, 0, new Color(255, 215, 0, 35), getWidth(), 0, new Color(0, 0, 0, 0)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(GOLD_SOFT);
                    g2.setStroke(new BasicStroke(1.3f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                    g2.setColor(GOLD);
                    g2.fillRect(0, 10, 3, getHeight() - 20);
                }
                g2.setFont(new Font("Dialog", Font.PLAIN, 14));
                g2.setColor((isActive || isHover) ? GOLD : TEXT_MUTED);
                g2.drawString(label, 16, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        btn.setRolloverEnabled(true);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            public void mouseExited (MouseEvent e) { btn.repaint(); }
            public void mouseClicked(MouseEvent e) {
                if (activeNavButton != null) activeNavButton.repaint();
                activeNavButton = btn;
                btn.repaint();
                cardLayout.show(mainCards, page);
            }
        });
        return btn;
    }

    private JPanel statsCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER_COL);
                g2.fillRoundRect(10, 5, getWidth() - 20, getHeight() - 10, 14, 14);
                g2.setColor(GOLD_DIM);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(10, 5, getWidth() - 20, getHeight() - 10, 14, 14);

                long total = tasks.size();
                long done  = tasks.stream().filter(t -> t.done).count();

                g2.setFont(new Font("Monospaced", Font.BOLD, 10));
                g2.setColor(TEXT_MUTED);
                g2.drawString("ALL TASKS", 22, 26);

                g2.setFont(new Font("Georgia", Font.BOLD, 26));
                g2.setColor(GOLD);
                g2.drawString(done + "/" + total, 22, 58);

                int barW = getWidth() - 44;
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(22, 66, barW, 5, 5, 5);
                if (total > 0) {
                    int fill = (int)(barW * done / total);
                    g2.setPaint(new GradientPaint(22, 66, GOLD_DIM, 22 + fill, 66, GOLD));
                    g2.fillRoundRect(22, 66, fill, 5, 5, 5);
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(200, 90));
        p.setMaximumSize(new Dimension(200, 90));
        return p;
    }

    private JPanel content() {
        cardLayout = new CardLayout();
        mainCards  = new JPanel(cardLayout);
        mainCards.setBackground(BG_DEEP);
        mainCards.add(tasksPage(),    "TASKS");
        mainCards.add(calendarPage(), "CALENDAR");
        return mainCards;
    }

    private JPanel tasksPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BG_DEEP);
        page.setBorder(new EmptyBorder(22, 22, 0, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("My Tasks");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(GOLD);
        JLabel dateTag = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        dateTag.setFont(new Font("Monospaced", Font.PLAIN, 11));
        dateTag.setForeground(TEXT_MUTED);
        header.add(title,   BorderLayout.WEST);
        header.add(dateTag, BorderLayout.EAST);

        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(BG_DEEP);

        JScrollPane scroll = new JScrollPane(taskListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new GoldScrollBarUI());

        JPanel inputCard = inputCard();

        page.add(header,    BorderLayout.NORTH);
        page.add(scroll,    BorderLayout.CENTER);
        page.add(inputCard, BorderLayout.SOUTH);
        return page;
    }

    private JPanel inputCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 3; i > 0; i--) {
                    g2.setColor(new Color(255, 215, 0, 8 * i));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 18, 18);
                }
                g2.setColor(BG_CARD);
                g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 16, 16);
                g2.setColor(GOLD_DIM);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));
        card.setPreferredSize(new Dimension(0, 62));

        taskField = new JTextField(18);
        taskField.setBackground(BG_PANEL);
        taskField.setForeground(TEXT_GOLD);
        taskField.setCaretColor(GOLD);
        taskField.setFont(new Font("Georgia", Font.PLAIN, 13));
        taskField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(GOLD_DIM, 1, true), new EmptyBorder(5, 10, 5, 10)));
        taskField.setPreferredSize(new Dimension(260, 36));

        priorityBox = new JComboBox<>(new String[]{"🔴 HIGH", "🟡 MED", "🟢 LOW"});
        priorityBox.setBackground(BG_PANEL);
        priorityBox.setForeground(GOLD);
        priorityBox.setFont(new Font("Georgia", Font.PLAIN, 12));
        priorityBox.setPreferredSize(new Dimension(112, 36));

        JButton addBtn = goldButton("＋  ADD TASK", 130, 36);
        addBtn.addActionListener(e -> addTask());
        taskField.addActionListener(e -> addTask());

        card.add(taskField);
        card.add(priorityBox);
        card.add(addBtn);
        return card;
    }

    private void addTask() {
        String text = taskField.getText().trim();
        if (text.isEmpty()) { shakeField(taskField); return; }

        String pri = ((String) priorityBox.getSelectedItem()).substring(3);

        try {
            Connection con = DBConnection.getConnection();
            String sql = "INSERT INTO tasks (text, priority, done, date) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, text);
            ps.setString(2, pri);
            ps.setBoolean(3, false);
            ps.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            ps.executeUpdate();
            System.out.println("DATA INSERTED");
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadTasksFromDB();
        refreshTaskList();
        taskField.setText("");
        updateStatus();
    }

    private void refreshTaskList() {
        taskListPanel.removeAll();
        taskListPanel.add(Box.createVerticalStrut(6));
        for (int i = 0; i < tasks.size(); i++) {
            taskListPanel.add(taskRow(tasks.get(i), i));
            taskListPanel.add(Box.createVerticalStrut(8));
        }
        taskListPanel.revalidate();
        taskListPanel.repaint();
        updateStatus();
    }

    private JPanel taskRow(Task task, int idx) {
        JPanel row = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(task.done ? new Color(20, 40, 20) : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                Color stripe = task.priority.equals("HIGH") ? RED_DEL
                             : task.priority.equals("MED")  ? GOLD_DIM
                             : GREEN_OK;
                g2.setColor(stripe);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.setColor(task.done ? new Color(60, 100, 60) : BORDER_COL);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setPreferredSize(new Dimension(0, 56));
        row.setBorder(new EmptyBorder(0, 12, 0, 10));

        JCheckBox check = new JCheckBox();
        check.setSelected(task.done);
        check.setOpaque(false);
        check.setUI(new GoldCheckUI());

        JLabel lbl = new JLabel(task.done
            ? "<html><strike>" + task.text + "</strike></html>"
            : task.text);
        lbl.setFont(new Font("Georgia", Font.PLAIN, 14));
        lbl.setForeground(task.done ? TEXT_MUTED : TEXT_GOLD);

        check.addActionListener(e -> {
            task.done = check.isSelected();
            try {
                Connection con = DBConnection.getConnection();
                String sql = "UPDATE tasks SET done = ? WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setBoolean(1, task.done);
                ps.setInt(2, task.getId());
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            lbl.setText(task.done
                ? "<html><strike>" + task.text + "</strike></html>"
                : task.text);
            lbl.setForeground(task.done ? TEXT_MUTED : TEXT_GOLD);
            row.repaint();
            updateStatus();
        });

        JLabel badge = new JLabel(task.date.format(DateTimeFormatter.ofPattern("MMM d")));
        badge.setFont(new Font("Monospaced", Font.PLAIN, 10));
        badge.setForeground(TEXT_MUTED);

        JButton del = iconButton("✕", RED_DEL);
        del.addActionListener(e -> {
            try {
                Connection con = DBConnection.getConnection();
                String sql = "DELETE FROM tasks WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, task.getId());
                ps.executeUpdate();
                System.out.println("DELETED FROM DB");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            tasks.remove(idx);
            refreshTaskList();
            updateStatus();
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(check);
        left.add(lbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(badge);
        right.add(del);

        row.add(left,  BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(GOLD_DIM, 1, true),
                    new EmptyBorder(0, 12, 0, 10)));
                row.repaint();
            }
            public void mouseExited(MouseEvent e) {
                row.setBorder(new EmptyBorder(0, 12, 0, 10));
                row.repaint();
            }
        });
        return row;
    }

    private JPanel calendarPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BG_DEEP);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel title = new JLabel("Calendar");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(GOLD);

        JPanel calCard = calCard();

        JLabel hint = new JLabel("Click a date to set the task date. Dots = tasks on that day.");
        hint.setFont(new Font("Monospaced", Font.PLAIN, 10));
        hint.setForeground(TEXT_MUTED);
        hint.setHorizontalAlignment(JLabel.CENTER);

        page.add(title,   BorderLayout.NORTH);
        page.add(calCard, BorderLayout.CENTER);
        page.add(hint,    BorderLayout.SOUTH);
        return page;
    }

    private JPanel calCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(GOLD_DIM);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.add(calNavBar(), BorderLayout.NORTH);
        calGridWrap = buildCalGrid();
        card.add(calGridWrap, BorderLayout.CENTER);
        return card;
    }

    private JPanel calNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);

        calTitle = new JLabel(calMonth.format(DateTimeFormatter.ofPattern("MMMM  yyyy")), JLabel.CENTER);
        calTitle.setFont(new Font("Georgia", Font.BOLD, 18));
        calTitle.setForeground(GOLD);

        JButton prev = calNavBtn("◀");
        JButton next = calNavBtn("▶");

        prev.addActionListener(e -> { calMonth = calMonth.minusMonths(1); refreshCal(); });
        next.addActionListener(e -> { calMonth = calMonth.plusMonths(1); refreshCal(); });

        nav.add(prev,     BorderLayout.WEST);
        nav.add(calTitle, BorderLayout.CENTER);
        nav.add(next,     BorderLayout.EAST);
        return nav;
    }

    private void refreshCal() {
        calTitle.setText(calMonth.format(DateTimeFormatter.ofPattern("MMMM  yyyy")));
        Container parent = calGridWrap.getParent();
        parent.remove(calGridWrap);
        calGridWrap = buildCalGrid();
        parent.add(calGridWrap, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    private JPanel buildCalGrid() {
        JPanel grid = new JPanel(new GridLayout(7, 7, 6, 6));
        grid.setOpaque(false);

        for (String d : new String[]{"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"}) {
            JLabel h = new JLabel(d, JLabel.CENTER);
            h.setFont(new Font("Monospaced", Font.BOLD, 10));
            h.setForeground(GOLD_DIM);
            grid.add(h);
        }

        int startOffset = calMonth.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < startOffset; i++) grid.add(new JLabel());

        for (int d = 1; d <= calMonth.lengthOfMonth(); d++) {
            final LocalDate date = calMonth.atDay(d);
            boolean isToday    = date.equals(LocalDate.now());
            boolean isSelected = date.equals(pickedDate);
            long taskCount = tasks.stream().filter(t -> t.date.equals(date)).count();

            JButton cell = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (isSelected) {
                        g2.setPaint(new GradientPaint(0, 0, GOLD_DIM, getWidth(), getHeight(), GOLD));
                        g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                        g2.setColor(BG_DEEP);
                    } else if (isToday) {
                        g2.setColor(new Color(255, 215, 0, 45));
                        g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                        g2.setColor(GOLD);
                        g2.setStroke(new BasicStroke(1.8f));
                        g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
                        g2.setColor(GOLD);
                    } else {
                        if (getModel().isRollover()) {
                            g2.setColor(new Color(255, 215, 0, 22));
                            g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                        }
                        g2.setColor(TEXT_GOLD);
                    }

                    g2.setFont(new Font("Georgia", Font.BOLD, 13));
                    String s = String.valueOf(date.getDayOfMonth());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2,
                                  (getHeight() + fm.getAscent() - fm.getDescent()) / 2);

                    if (taskCount > 0) {
                        g2.setColor(isSelected ? BG_DEEP : GOLD);
                        g2.fillOval(getWidth() / 2 - 3, getHeight() - 9, 6, 6);
                    }
                    g2.dispose();
                }
            };
            cell.setContentAreaFilled(false);
            cell.setBorderPainted(false);
            cell.setFocusPainted(false);
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addActionListener(e -> {
                pickedDate = date;
                refreshCal();
                updateStatus();
            });
            grid.add(cell);
        }
        return grid;
    }

    private JPanel statusBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG_CARD);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(GOLD_DIM);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        bar.setPreferredSize(new Dimension(0, 28));
        bar.setBorder(new EmptyBorder(0, 18, 0, 18));

        statusLabel = new JLabel("0/0");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
        statusLabel.setForeground(TEXT_MUTED);

        JLabel version = new JLabel("AURUM v2.0");
        version.setFont(new Font("Monospaced", Font.PLAIN, 10));
        version.setForeground(new Color(80, 65, 25));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(version,     BorderLayout.EAST);
        return bar;
    }

    private void updateStatus() {
        int total = tasks.size();
        int done  = 0;
        for (Task t : tasks) {
            if (t.done) done++;
        }
        String text = done + "/" + total;
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
        if (sidebarStatsCard != null) {
            sidebarStatsCard.repaint();
        }
    }

    private JButton goldButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            {
                setContentAreaFilled(false); setBorderPainted(false);
                setFocusPainted(false);
                setFont(new Font("Georgia", Font.BOLD, 12));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, hov ? GOLD : GOLD_DIM,
                                              getWidth(), getHeight(), hov ? GOLD_DIM : new Color(90, 70, 0)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BG_DEEP);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        return btn;
    }

    private JButton iconButton(String label, Color color) {
        JButton btn = new JButton() {
            boolean hov = false;
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(28, 28));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hov) { g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70));
                           g2.fillOval(0, 0, getWidth(), getHeight()); }
                g2.setColor(hov ? color : TEXT_MUTED);
                g2.setFont(new Font("Dialog", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, (getWidth() - fm.stringWidth(label)) / 2,
                              (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        return btn;
    }

    private JButton dotButton(Color color, String label, ActionListener action) {
        JButton btn = new JButton() {
            boolean hov = false;
            {
                setPreferredSize(new Dimension(24, 24));
                setContentAreaFilled(false); setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addActionListener(action);
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? color.brighter() : color);
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Dialog", Font.BOLD, 9));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, (getWidth() - fm.stringWidth(label)) / 2,
                              (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        return btn;
    }

    private JButton calNavBtn(String label) {
        JButton btn = new JButton() {
            boolean hov = false;
            {
                setPreferredSize(new Dimension(34, 34));
                setContentAreaFilled(false); setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hov) { g2.setColor(new Color(255, 215, 0, 35));
                           g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                g2.setColor(GOLD);
                g2.setFont(new Font("Dialog", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, (getWidth() - fm.stringWidth(label)) / 2,
                              (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        return btn;
    }

    private void shakeField(JTextField tf) {
        Point orig = tf.getLocation();
        int[] step = {0};
        Timer t = new Timer(true);
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                int dx = (step[0] % 2 == 0) ? 5 : -5;
                SwingUtilities.invokeLater(() -> tf.setLocation(orig.x + dx, orig.y));
                if (++step[0] >= 8) {
                    t.cancel();
                    SwingUtilities.invokeLater(() -> tf.setLocation(orig));
                }
            }
        }, 0, 40);
    }

    private void fadeIn() {
        frame.setOpacity(0f);
        float[] op = {0f};
        Timer t = new Timer(true);
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                op[0] = Math.min(1f, op[0] + 0.06f);
                SwingUtilities.invokeLater(() -> frame.setOpacity(op[0]));
                if (op[0] >= 1f) t.cancel();
            }
        }, 0, 16);
    }

    static class GoldCheckUI extends BasicCheckBoxUI {
        @Override public synchronized void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean sel = ((JCheckBox) c).isSelected();
            int s = 20;
            if (sel) {
                g2.setPaint(new GradientPaint(0, 0, GOLD_DIM, s, s, GOLD));
                g2.fillRoundRect(0, 0, s, s, 6, 6);
                g2.setColor(BG_DEEP);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(4, 10, 8, 15);
                g2.drawLine(8, 15, 16, 5);
            } else {
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(0, 0, s, s, 6, 6);
                g2.setColor(GOLD_DIM);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, s - 1, s - 1, 6, 6);
            }
            g2.dispose();
        }
        @Override public Dimension getPreferredSize(JComponent c) { return new Dimension(22, 22); }
    }

    static class GoldScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = GOLD_DIM;
            trackColor = BG_PANEL;
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(r.x, r.y, GOLD_DIM, r.x + r.width, r.y + r.height, GOLD));
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new TodoAppGold();
        });
    }
}