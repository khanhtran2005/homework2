import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class SparseMatrixComplete extends JFrame {
    
    // GUI Components
    private JTextField sizeField;
    private JTextField densityField;
    private JTable matrix1Table, matrix2Table, resultTable;
    private JTextArea sparseInfoArea;
    private JLabel timeLabel;
    private DefaultTableModel model1, model2, modelResult;
    
    // Data storage
    private double[][] matrix1, matrix2, resultMatrix;
    private SparseMatrix sparseMatrix1, sparseMatrix2;
    
    // Sparse Matrix Class
    class SparseElement {
        int row, col;
        double value;
        
        public SparseElement(int r, int c, double v) {
            row = r;
            col = c; 
            value = v;
        }
        
        public String toString() {
            return String.format("(%d, %d) = %.1f", row, col, value);
        }
    }
    
    class SparseMatrix {
        ArrayList<SparseElement> elements;
        int rows, cols;
        
        public SparseMatrix(int r, int c) {
            rows = r;
            cols = c;
            elements = new ArrayList<SparseElement>();
        }
        
        public void addElement(int row, int col, double value) {
            if (value != 0.0) {
                elements.add(new SparseElement(row, col, value));
            }
        }
        
        public double[][] toDenseMatrix() {
            double[][] dense = new double[rows][cols];
            for (SparseElement elem : elements) {
                dense[elem.row][elem.col] = elem.value;
            }
            return dense;
        }
        
        public int getNonZeroCount() {
            return elements.size();
        }
        
        public double getSparsity() {
            int total = rows * cols;
            return (1.0 - (double)elements.size() / total) * 100.0;
        }
    }
    
    // Constructor
    public SparseMatrixComplete() {
        super("Sparse Matrix Generator - Complete Version");
        initializeGUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    private void initializeGUI() {
        setLayout(new BorderLayout());
        
        // Top Panel - Input controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center Panel - Matrix displays
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom Panel - Status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Input fields
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Enter matrix size (n):"));
        sizeField = new JTextField("50", 8);
        inputPanel.add(sizeField);
        
        inputPanel.add(new JLabel("Enter density (0 to 1):"));
        densityField = new JTextField("0.1", 8);
        inputPanel.add(densityField);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        
        JButton btn1 = new JButton("Generate Matrix 1");
        JButton btn2 = new JButton("Generate Matrix 2");
        JButton btnAdd = new JButton("Add Matrices");
        JButton btnSubtract = new JButton("Subtract Matrices");
        JButton btnMultiplyTraditional = new JButton("Multiply (Traditional)");
        JButton btnMultiplySparse = new JButton("Multiply (Sparse)");
        JButton btnTransposeSparse = new JButton("Transpose (Sparse)");
        JButton btnTransposeOriginal = new JButton("Transpose (Original)");
        
        // Add action listeners
        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { generateMatrix1(); }
        });
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { generateMatrix2(); }
        });
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addMatrices(); }
        });
        btnSubtract.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { subtractMatrices(); }
        });
        btnMultiplyTraditional.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { multiplyTraditional(); }
        });
        btnMultiplySparse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { multiplySparse(); }
        });
        btnTransposeSparse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { transposeSparse(); }
        });
        btnTransposeOriginal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { transposeOriginal(); }
        });
        
        buttonPanel.add(btn1);
        buttonPanel.add(btn2);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSubtract);
        buttonPanel.add(btnMultiplyTraditional);
        buttonPanel.add(btnMultiplySparse);
        buttonPanel.add(btnTransposeSparse);
        buttonPanel.add(btnTransposeOriginal);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        // Initialize table models
        model1 = new DefaultTableModel();
        model2 = new DefaultTableModel();
        modelResult = new DefaultTableModel();
        
        // Create tables
        matrix1Table = createStyledTable(model1);
        matrix2Table = createStyledTable(model2);
        resultTable = createStyledTable(modelResult);
        
        // Create scroll panes
        JScrollPane scroll1 = new JScrollPane(matrix1Table);
        scroll1.setBorder(BorderFactory.createTitledBorder("Matrix 1"));
        scroll1.setPreferredSize(new Dimension(300, 250));
        
        JScrollPane scroll2 = new JScrollPane(matrix2Table);
        scroll2.setBorder(BorderFactory.createTitledBorder("Matrix 2"));
        scroll2.setPreferredSize(new Dimension(300, 250));
        
        JScrollPane scrollResult = new JScrollPane(resultTable);
        scrollResult.setBorder(BorderFactory.createTitledBorder("Result Matrix"));
        scrollResult.setPreferredSize(new Dimension(300, 250));
        
        // Sparse info area
        sparseInfoArea = new JTextArea();
        sparseInfoArea.setEditable(false);
        sparseInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        JScrollPane sparseScroll = new JScrollPane(sparseInfoArea);
        sparseScroll.setBorder(BorderFactory.createTitledBorder("Sparse Matrix Elements"));
        sparseScroll.setPreferredSize(new Dimension(300, 250));
        
        panel.add(scroll1);
        panel.add(scroll2);
        panel.add(scrollResult);
        panel.add(sparseScroll);
        
        return panel;
    }
    
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        table.setRowHeight(20);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        
        // Custom cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(JLabel.CENTER);
                
                if (value != null) {
                    double val = Double.parseDouble(value.toString());
                    if (val == 0.0) {
                        setText("0");
                        setBackground(Color.WHITE);
                        setForeground(Color.LIGHT_GRAY);
                    } else {
                        setText(String.format("%.0f", val));
                        setBackground(new Color(255, 255, 200)); // Light yellow
                        setForeground(Color.BLUE);
                    }
                } else {
                    setText("0");
                    setBackground(Color.WHITE);
                    setForeground(Color.LIGHT_GRAY);
                }
                
                if (isSelected) {
                    setBackground(Color.BLUE);
                    setForeground(Color.WHITE);
                }
                
                return this;
            }
        });
        
        return table;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeLabel = new JLabel("Execution Time: 0 ns");
        timeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(timeLabel);
        return panel;
    }
    
    // Matrix generation
    private void generateMatrix1() {
        try {
            int size = Integer.parseInt(sizeField.getText().trim());
            double density = Double.parseDouble(densityField.getText().trim());
            
            if (size <= 0 || size > 1000) {
                JOptionPane.showMessageDialog(this, "Matrix size must be between 1 and 1000!");
                return;
            }
            
            if (density < 0 || density > 1) {
                JOptionPane.showMessageDialog(this, "Density must be between 0 and 1!");
                return;
            }
            
            matrix1 = generateRandomMatrix(size, density);
            sparseMatrix1 = convertToSparse(matrix1);
            displayMatrix(matrix1, model1);
            updateSparseInfo();
            
            timeLabel.setText("Matrix 1 generated successfully");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
        }
    }
    
    private void generateMatrix2() {
        try {
            int size = Integer.parseInt(sizeField.getText().trim());
            double density = Double.parseDouble(densityField.getText().trim());
            
            if (size <= 0 || size > 1000) {
                JOptionPane.showMessageDialog(this, "Matrix size must be between 1 and 1000!");
                return;
            }
            
            if (density < 0 || density > 1) {
                JOptionPane.showMessageDialog(this, "Density must be between 0 and 1!");
                return;
            }
            
            matrix2 = generateRandomMatrix(size, density);
            sparseMatrix2 = convertToSparse(matrix2);
            displayMatrix(matrix2, model2);
            updateSparseInfo();
            
            timeLabel.setText("Matrix 2 generated successfully");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
        }
    }
    
    private double[][] generateRandomMatrix(int size, double density) {
        double[][] matrix = new double[size][size];
        Random random = new Random();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (random.nextDouble() < density) {
                    matrix[i][j] = random.nextInt(9) + 1; // Values 1-9
                }
            }
        }
        return matrix;
    }
    
    private SparseMatrix convertToSparse(double[][] matrix) {
        SparseMatrix sparse = new SparseMatrix(matrix.length, matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] != 0) {
                    sparse.addElement(i, j, matrix[i][j]);
                }
            }
        }
        return sparse;
    }
    
    private void displayMatrix(double[][] matrix, DefaultTableModel model) {
        // Clear existing data
        model.setRowCount(0);
        model.setColumnCount(0);
        
        if (matrix == null || matrix.length == 0) return;
        
        // Add columns
        for (int j = 0; j < matrix[0].length; j++) {
            model.addColumn(String.valueOf(j));
        }
        
        // Add rows
        for (int i = 0; i < matrix.length; i++) {
            Object[] row = new Object[matrix[0].length];
            for (int j = 0; j < matrix[0].length; j++) {
                row[j] = matrix[i][j];
            }
            model.addRow(row);
        }
    }
    
    private void updateSparseInfo() {
        StringBuilder info = new StringBuilder();
        
        if (sparseMatrix1 != null) {
            info.append("=== MATRIX 1 (SPARSE) ===\n");
            info.append("Size: ").append(sparseMatrix1.rows).append("x").append(sparseMatrix1.cols).append("\n");
            info.append("Non-zero elements: ").append(sparseMatrix1.getNonZeroCount()).append("\n");
            info.append("Sparsity: ").append(String.format("%.2f%%", sparseMatrix1.getSparsity())).append("\n");
            
            if (sparseMatrix1.elements.size() <= 50) {
                info.append("Elements:\n");
                for (SparseElement elem : sparseMatrix1.elements) {
                    info.append(elem.toString()).append("\n");
                }
            } else {
                info.append("Too many elements to display (>50)\n");
            }
            info.append("\n");
        }
        
        if (sparseMatrix2 != null) {
            info.append("=== MATRIX 2 (SPARSE) ===\n");
            info.append("Size: ").append(sparseMatrix2.rows).append("x").append(sparseMatrix2.cols).append("\n");
            info.append("Non-zero elements: ").append(sparseMatrix2.getNonZeroCount()).append("\n");
            info.append("Sparsity: ").append(String.format("%.2f%%", sparseMatrix2.getSparsity())).append("\n");
            
            if (sparseMatrix2.elements.size() <= 50) {
                info.append("Elements:\n");
                for (SparseElement elem : sparseMatrix2.elements) {
                    info.append(elem.toString()).append("\n");
                }
            } else {
                info.append("Too many elements to display (>50)\n");
            }
        }
        
        sparseInfoArea.setText(info.toString());
        sparseInfoArea.setCaretPosition(0);
    }
    
    // Matrix operations
    private void addMatrices() {
        if (!checkMatricesExist()) return;
        
        long startTime = System.nanoTime();
        resultMatrix = performAddition(matrix1, matrix2);
        long endTime = System.nanoTime();
        
        displayMatrix(resultMatrix, modelResult);
        timeLabel.setText("Matrix Addition Execution Time: " + (endTime - startTime) + " ns");
    }
    
    private void subtractMatrices() {
        if (!checkMatricesExist()) return;
        
        long startTime = System.nanoTime();
        resultMatrix = performSubtraction(matrix1, matrix2);
        long endTime = System.nanoTime();
        
        displayMatrix(resultMatrix, modelResult);
        timeLabel.setText("Matrix Subtraction Execution Time: " + (endTime - startTime) + " ns");
    }
    
    private void multiplyTraditional() {
        if (!checkMatricesExist()) return;
        
        long startTime = System.nanoTime();
        resultMatrix = performTraditionalMultiplication(matrix1, matrix2);
        long endTime = System.nanoTime();
        
        displayMatrix(resultMatrix, modelResult);
        timeLabel.setText("Traditional Multiplication Execution Time: " + (endTime - startTime) + " ns");
    }
    
    private void multiplySparse() {
        if (!checkSparseMatricesExist()) return;
        
        long startTime = System.nanoTime();
        SparseMatrix result = performSparseMultiplication(sparseMatrix1, sparseMatrix2);
        resultMatrix = result.toDenseMatrix();
        long endTime = System.nanoTime();
        
        displayMatrix(resultMatrix, modelResult);
        timeLabel.setText("Sparse Multiplication Execution Time: " + (endTime - startTime) + " ns");
    }
    
    private void transposeSparse() {
        if (sparseMatrix2 == null) {
            JOptionPane.showMessageDialog(this, "Please generate Matrix 2 first!");
            return;
        }
        
        long startTime = System.nanoTime();
        SparseMatrix transposed = performSparseTranspose(sparseMatrix2);
        resultMatrix = transposed.toDenseMatrix();
        long endTime = System.nanoTime();
        
        displayMatrix(resultMatrix, modelResult);
        timeLabel.setText("Sparse Transpose Execution Time: " + (endTime - startTime) + " ns");
    }
    
    private void transposeOriginal() {
        if (matrix2 == null) {
            JOptionPane.showMessageDialog(this, "Please generate Matrix 2 first!");
            return;
        }
        
        long startTime = System.nanoTime();
        resultMatrix = performTraditionalTranspose(matrix2);
        long endTime = System.nanoTime();
        
        displayMatrix(resultMatrix, modelResult);
        timeLabel.setText("Traditional Transpose Execution Time: " + (endTime - startTime) + " ns");
    }
    
    // Helper methods
    private boolean checkMatricesExist() {
        if (matrix1 == null || matrix2 == null) {
            JOptionPane.showMessageDialog(this, "Please generate both matrices first!");
            return false;
        }
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            JOptionPane.showMessageDialog(this, "Matrices must have the same dimensions!");
            return false;
        }
        return true;
    }
    
    private boolean checkSparseMatricesExist() {
        if (sparseMatrix1 == null || sparseMatrix2 == null) {
            JOptionPane.showMessageDialog(this, "Please generate both matrices first!");
            return false;
        }
        return true;
    }
    
    // Matrix operation implementations
    private double[][] performAddition(double[][] a, double[][] b) {
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }
    
    private double[][] performSubtraction(double[][] a, double[][] b) {
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }
    
    private double[][] performTraditionalMultiplication(double[][] a, double[][] b) {
        double[][] result = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < a[0].length; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }
    
    private SparseMatrix performSparseMultiplication(SparseMatrix a, SparseMatrix b) {
        SparseMatrix result = new SparseMatrix(a.rows, b.cols);
        double[][] temp = new double[a.rows][b.cols];
        
        // Perform sparse multiplication
        for (SparseElement elemA : a.elements) {
            for (SparseElement elemB : b.elements) {
                if (elemA.col == elemB.row) {
                    temp[elemA.row][elemB.col] += elemA.value * elemB.value;
                }
            }
        }
        
        // Convert back to sparse
        for (int i = 0; i < temp.length; i++) {
            for (int j = 0; j < temp[0].length; j++) {
                if (temp[i][j] != 0) {
                    result.addElement(i, j, temp[i][j]);
                }
            }
        }
        
        return result;
    }
    
    private double[][] performTraditionalTranspose(double[][] matrix) {
        double[][] result = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }
    
    private SparseMatrix performSparseTranspose(SparseMatrix matrix) {
        SparseMatrix result = new SparseMatrix(matrix.cols, matrix.rows);
        for (SparseElement elem : matrix.elements) {
            result.addElement(elem.col, elem.row, elem.value);
        }
        return result;
    }
    
    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SparseMatrixComplete().setVisible(true);
            }
        });
    }
}