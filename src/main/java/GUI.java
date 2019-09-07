import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class GUI {

    private JFileChooser jFileChooser = new JFileChooser();
    private FileTreeBuilder fileTreeInfo = new FileTreeBuilder();
    private JTree jTree = new JTree(fileTreeInfo.getMtb());
    private JPanel jPanel = new JPanel();
    private JFrame jFrame = getFrame();
    private JLabel jLabelCurDir = new JLabel("Текущая директория: не выбрана");
    private JTextField jTextFieldExt = new JTextField("log");
    private JTextField jTextFieldText = new JTextField("Java");
    private JButton jButtonDir = new JButton("Выберите директорию");
    private JButton jButtonSearch = new JButton("Поиск");
    private JTextArea jTextArea = new JTextArea(20, 30);
    private JButton jButtonForward = new JButton("Вперед");
    private JButton jButtonBack = new JButton("Назад");
    private JRadioButton noHighlightAll = new JRadioButton("Снять выделение", true);
    private JRadioButton yesHighlightAll = new JRadioButton("Выделить все", false);
    private JScrollPane jScrollPaneText;
    private JScrollPane jScrollPaneTree;
    private List<Integer> matchList = new ArrayList<>();
    private ListIterator<Integer> matchIterator = matchList.listIterator();
    private Thread fileThread = new Thread();
    private Thread directoryThread = new Thread();
    private volatile boolean scrollBarMaxIsReached = false;

    public GUI() {
        this.execute();
    }

    private void execute() {
        jFrame.setLayout(new BorderLayout());
        jFrame.add(jPanel, BorderLayout.NORTH);
        jPanel.setLayout(new GridBagLayout());
        JPanel jPanel1 = getTopPanel();
        JPanel jPanel2 = getLowerPanel();
        jPanel2.setPreferredSize(getDimensionWithSimilarWidth(jPanel1, jPanel2));
        jPanel.add(jPanel1, getConstraints(0, 0, 0, 0, 10, 10,
                0, 0, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));
        jPanel.add(jPanel2, getConstraints(0, 0, 0, 10, 10, 10,
                0, 0, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));
        jFrame.setMinimumSize(jPanel.getPreferredSize());
        jFrame.pack();
        jPanel.revalidate();
    }

    private JPanel getTopPanel() {
        JPanel jPanel = new JPanel();
        Border border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Параметры поиска");
        jPanel.setLayout(new GridBagLayout());
        jPanel.setBorder(border);

        //Фрейм текстовой области задания расширения файла
        jTextFieldExt.setHorizontalAlignment(JTextField.CENTER);
        jTextFieldExt.setToolTipText("Введите расширение файла");
        jTextFieldExt.setPreferredSize(jButtonDir.getPreferredSize());

        //Фрейм текстовой области задания текста для поиска
        jTextFieldText.setHorizontalAlignment(JTextField.CENTER);
        jTextFieldText.setToolTipText("Введите строку для поиска");
        jTextFieldText.setPreferredSize(jButtonDir.getPreferredSize());

        //Фрейм кнопки поиска
        jButtonSearch.setPreferredSize(jButtonDir.getPreferredSize());
        jButtonSearch.addActionListener(new BeginSearchListener());

        //Фрейм кнопки выбора директории
        jButtonDir.addActionListener(e -> {
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jFileChooser.showOpenDialog(jPanel) == 0) {
                fileTreeInfo.setCurrentDirectory(jFileChooser.getSelectedFile().toPath());
                jLabelCurDir.setText("Текущая директория: " + jFileChooser.getSelectedFile().toPath());
            }
        });

        jPanel.add(jButtonDir, getConstraints(0, 0, 0, 0,
                20, 20, 5, 5, 5, 5, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));
        jPanel.add(jTextFieldExt, getConstraints(0, 0, 20, 0,
                20, 20, 5, 5, 5, 5, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));
        jPanel.add(jTextFieldText, getConstraints(0, 0, 40, 0,
                20, 20, 5, 5, 5, 5, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));
        jPanel.add(jLabelCurDir, getConstraints(0, 0, 0, 20,
                20, 80, 5, 5, 5, 5, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));
        jPanel.add(jButtonSearch, getConstraints(0, 0, 60, 0,
                20, 20, 5, 5, 5, 5, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));
        return jPanel;
    }

    //Метод построения нижней панели
    private JPanel getLowerPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
        jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Результат поиска"));

        //Фрейм текстовой зоны
        jTextArea.setLineWrap(true);
        jTextArea.setEditable(false);
        jScrollPaneText = new JScrollPane(jTextArea);
        jScrollPaneText.getVerticalScrollBar().getModel().addChangeListener(new DynamicTextScrollbarListener());
        jPanel.add(jScrollPaneText, getConstraints(1, 1, 100, 0, 100, 100,
                10, 0, 10, 10, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH));

        //Фрейм системного дерева
        jTree.getSelectionModel().addTreeSelectionListener(new TreeSelector());
        jScrollPaneTree = new JScrollPane(jTree);
        jPanel.add(jScrollPaneTree, getConstraints(1, 1, 0, 0, 100, 100,
                10, 10, 10, 10, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH));

        //Фрейм радиопанели кнопок выбора подсветки совпадений в тексте
        ButtonGroup highlightButtonGroup = new ButtonGroup();
        yesHighlightAll.addActionListener(e -> highlightMatches());
        noHighlightAll.addActionListener(e -> {
            jTextArea.getHighlighter().removeAllHighlights();
            matchIterator = matchList.listIterator();
        });
        highlightButtonGroup.add(yesHighlightAll);
        highlightButtonGroup.add(noHighlightAll);
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(2, 1));
        radioPanel.add(yesHighlightAll);
        radioPanel.add(noHighlightAll);
        radioPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Выделить совпадения"));
        radioPanel.setPreferredSize(getDimensionWithSimilarWidth(jButtonSearch, radioPanel));
        jPanel.add(radioPanel, getConstraints(0, 0, 100, 100, 100, 100,
                0, 0, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        //Фрейм кнопки перехода к следующему совпадению
        jButtonForward.setPreferredSize(jButtonSearch.getPreferredSize());
        jButtonForward.addActionListener(new ForwardFocusListener());
        jPanel.add(jButtonForward, getConstraints(0, 0, 100, 200, 100, 100,
                0, 0, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));

        //Фрейм кнопки перехода к предыдущему совпадению
        jButtonBack.setPreferredSize(jButtonSearch.getPreferredSize());
        jButtonBack.addActionListener(new PreviousFocusListener());
        jPanel.add(jButtonBack, getConstraints(0, 0, 100, 300, 100, 100,
                0, 0, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE));
        return jPanel;
    }

    private JFrame getFrame() {
        JFrame jFrame = new JFrame();
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = dimension.width, screenHeight = dimension.height, appWidth = 1000, appHeight = 1000;
        jFrame.setBounds(screenWidth / 2 - appWidth / 2, screenHeight / 2 - appHeight / 2, appWidth, appHeight);
        jFrame.setTitle("Поиск текста в директории");
        return jFrame;
    }

    private void highlightMatches() {
        DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
            matchIterator = matchList.listIterator();
            while (matchIterator.hasNext()) {
                Integer match = matchIterator.next();
                try {
                    jTextArea.getHighlighter().addHighlight(match - fileTreeInfo.getText().length(), match, highlightPainter);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
    }

    private GridBagConstraints getConstraints(int weightx, int weighty, int gridx, int gridy,
                                              int gridheight, int gridwidth, int top, int left, int bottom, int right, int anchor, int fill) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.weightx = weightx;
        gridBagConstraints.weighty = weighty;
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridheight = gridheight;
        gridBagConstraints.gridwidth = gridwidth;
        gridBagConstraints.insets = new Insets(top, left, bottom, right);
        gridBagConstraints.anchor = anchor;
        gridBagConstraints.fill = fill;
        return gridBagConstraints;
    }

    private Dimension getDimensionWithSimilarWidth(JComponent whose, JComponent toWhom) {
        Dimension preferredSize = toWhom.getPreferredSize();
        preferredSize.width = whose.getPreferredSize().width;
        return preferredSize;
    }

    private class TreeSelector implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            try {
            Thread.getAllStackTraces().entrySet().stream()
                    .filter(x -> x.getKey().getName().equals(fileThread.getName()))
                    .forEach(x-> {
                        if (!x.getKey().isInterrupted()) {
                            x.getKey().interrupt();
                        }
                    });
            }catch(NoSuchElementException exc) {
                exc.printStackTrace();
            }
            if (e.getNewLeadSelectionPath() != null) {
                FileReaderThread fileReaderThread = new FileReaderThread();
                fileReaderThread.setTreePath(e.getNewLeadSelectionPath());
                fileThread = new Thread(fileReaderThread, "fileReaderThread");
                fileThread.setDaemon(true);
                fileThread.start();
            }
        }
    }

    private class BeginSearchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("begin");
            jTextArea.setText("");
            fileTreeInfo.setExtension(jTextFieldExt.getText());
            fileTreeInfo.setText(jTextFieldText.getText());
            if (fileTreeInfo.readyToSearch()) {
                try {
                    Thread.getAllStackTraces().entrySet().stream()
                            .filter(x -> x.getKey().getName().equals(directoryThread.getName()))
                            .forEach(x -> {
                                if (!x.getKey().isInterrupted()) {
                                    x.getKey().interrupt();
                                }
                            });
                } catch (NoSuchElementException exc) {
                    exc.printStackTrace();
                }
                directoryThread = new Thread(new SearchDirectoryThread(), "searchDirectoryThread");
                directoryThread.setDaemon(true);
                directoryThread.start();
            } else {
                JOptionPane.showMessageDialog(jPanel, "Один из параметров поиска задан неверно", "Ошибка!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class DynamicTextScrollbarListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            BoundedRangeModel boundedRangeModel = jScrollPaneText.getVerticalScrollBar().getModel();
            boolean valueIsAdjusting = boundedRangeModel.getValueIsAdjusting();
            if(!valueIsAdjusting) {
                if(boundedRangeModel.getValue() + boundedRangeModel.getExtent() >= boundedRangeModel.getMaximum()/1.2) {
                    scrollBarMaxIsReached = true;
                } else {
                    scrollBarMaxIsReached = false;
                }
            }
        }
    }

    private class ForwardFocusListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (matchIterator.hasNext()) {
                Integer position = matchIterator.next();
                jTextArea.grabFocus();
                jTextArea.select(position - fileTreeInfo.getText().length(), position);
            } else {
                JOptionPane.showMessageDialog(jPanel, "Текст не найден", "Внимание!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class PreviousFocusListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (matchIterator.hasPrevious()) {
                Integer position = matchIterator.previous();
                jTextArea.grabFocus();
                jTextArea.select(position - fileTreeInfo.getText().length(), position);
            } else {
                JOptionPane.showMessageDialog(jPanel, "Текст не найден", "Внимание!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    class FileReaderThread implements Runnable {

        private TreePath treePath;

        @Override
        public void run() {
            validation(treePath);
            dynamicHighlightOrNot();
        }

        private void validation(TreePath treePath) {

            matchList = new ArrayList<>();
            String text = fileTreeInfo.getText();
            jTextArea.setText("");
            for (File file : fileTreeInfo.getFiles()) {
                String nodeParsed = treePath.toString().substring(1, treePath.toString().length() - 1).replace(", ", "\\");
                if (file.getPath().replace(", ","\\").endsWith(nodeParsed)) {
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                        int caretPosition = 0;
                        int textStart = 0;
                        int symbol = in.read();
                        boolean trigger = true;
                        while(symbol != -1) {
                            if (scrollBarMaxIsReached) {
                                jTextArea.append("" + (char) symbol);
                                if ((char) symbol == text.charAt(textStart)) {
                                    textStart++;
                                    if (textStart == text.length()) {
                                        matchList.add(caretPosition + 1);
                                        textStart = 0;
                                    }
                                } else {
                                    textStart = 0;
                                }
                                caretPosition++;
                                symbol = in.read();
                                trigger = true;
                            } else if (trigger) {
                                dynamicHighlightOrNot();
                                trigger = false;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void dynamicHighlightOrNot() {
            matchList.sort(Integer::compareTo);
            matchIterator = matchList.listIterator();
            if (yesHighlightAll.isSelected()) {
                highlightMatches();
            } else {
                jTextArea.getHighlighter().removeAllHighlights();
            }
        }

        public void setTreePath(TreePath treePath) {
            this.treePath = treePath;
        }

    }

    class SearchDirectoryThread implements Runnable {

        @Override
        public void run() {
            fileTreeInfo.execute();
            jTree.setModel(new JTree(fileTreeInfo.getMtb()).getModel());
            jTree.scrollPathToVisible(new TreePath(((DefaultMutableTreeNode)fileTreeInfo.getMtb()).getLastLeaf().getPath()));
            System.out.println("Расширение: " + fileTreeInfo.getExtension() + "\tТекст: " + fileTreeInfo.getText() + "\tДиректория:" + fileTreeInfo.getCurrentDirectory().toString());
        }
    }
}
