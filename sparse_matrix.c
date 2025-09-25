#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>

// Cấu trúc ma trận thưa dạng COO (Coordinate format)
typedef struct {
    int row;
    int col;
    double value;
} SparseElement;

typedef struct {
    SparseElement* elements;
    int num_elements;
    int rows;
    int cols;
} SparseMatrix;

// Cấu trúc ma trận dày đặc
typedef struct {
    double** data;
    int rows;
    int cols;
} DenseMatrix;

// ===== Hàm tạo và giải phóng bộ nhớ =====
SparseMatrix* createSparseMatrix(int rows, int cols) {
    SparseMatrix* sm = (SparseMatrix*)malloc(sizeof(SparseMatrix));
    sm->elements = NULL;
    sm->num_elements = 0;
    sm->rows = rows;
    sm->cols = cols;
    return sm;
}

DenseMatrix* createDenseMatrix(int rows, int cols) {
    DenseMatrix* dm = (DenseMatrix*)malloc(sizeof(DenseMatrix));
    dm->rows = rows;
    dm->cols = cols;
    dm->data = (double**)malloc(rows * sizeof(double*));
    for (int i = 0; i < rows; i++) {
        dm->data[i] = (double*)calloc(cols, sizeof(double));
    }
    return dm;
}

void freeSparseMatrix(SparseMatrix* sm) {
    if (sm) {
        if (sm->elements) free(sm->elements);
        free(sm);
    }
}

void freeDenseMatrix(DenseMatrix* dm) {
    if (dm) {
        for (int i = 0; i < dm->rows; i++) {
            free(dm->data[i]);
        }
        free(dm->data);
        free(dm);
    }
}

// ===== Hàm tạo ma trận ngẫu nhiên =====
DenseMatrix* generateRandomMatrix(int size, double density) {
    DenseMatrix* dm = createDenseMatrix(size, size);
    
    srand(time(NULL));
    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            if ((double)rand() / RAND_MAX < density) {
                dm->data[i][j] = (double)(rand() % 10 + 1); // Giá trị 1-10
            }
        }
    }
    return dm;
}

// ===== Chuyển đổi Dense Matrix -> Sparse Matrix =====
SparseMatrix* denseToSparse(DenseMatrix* dm) {
    // Đếm số phần tử khác 0
    int count = 0;
    for (int i = 0; i < dm->rows; i++) {
        for (int j = 0; j < dm->cols; j++) {
            if (dm->data[i][j] != 0.0) count++;
        }
    }
    
    SparseMatrix* sm = createSparseMatrix(dm->rows, dm->cols);
    sm->elements = (SparseElement*)malloc(count * sizeof(SparseElement));
    
    int index = 0;
    for (int i = 0; i < dm->rows; i++) {
        for (int j = 0; j < dm->cols; j++) {
            if (dm->data[i][j] != 0.0) {
                sm->elements[index].row = i;
                sm->elements[index].col = j;
                sm->elements[index].value = dm->data[i][j];
                index++;
            }
        }
    }
    sm->num_elements = count;
    return sm;
}

// ===== Chuyển đổi Sparse Matrix -> Dense Matrix =====
DenseMatrix* sparseToDense(SparseMatrix* sm) {
    DenseMatrix* dm = createDenseMatrix(sm->rows, sm->cols);
    
    for (int i = 0; i < sm->num_elements; i++) {
        int row = sm->elements[i].row;
        int col = sm->elements[i].col;
        dm->data[row][col] = sm->elements[i].value;
    }
    return dm;
}

// ===== PHÉP CHUYỂN VỊ =====
// Chuyển vị ma trận dày đặc
DenseMatrix* transposeDense(DenseMatrix* dm) {
    DenseMatrix* result = createDenseMatrix(dm->cols, dm->rows);
    
    for (int i = 0; i < dm->rows; i++) {
        for (int j = 0; j < dm->cols; j++) {
            result->data[j][i] = dm->data[i][j];
        }
    }
    return result;
}

// Chuyển vị ma trận thưa
SparseMatrix* transposeSparse(SparseMatrix* sm) {
    SparseMatrix* result = createSparseMatrix(sm->cols, sm->rows);
    result->num_elements = sm->num_elements;
    result->elements = (SparseElement*)malloc(sm->num_elements * sizeof(SparseElement));
    
    for (int i = 0; i < sm->num_elements; i++) {
        result->elements[i].row = sm->elements[i].col;
        result->elements[i].col = sm->elements[i].row;
        result->elements[i].value = sm->elements[i].value;
    }
    return result;
}

// ===== PHÉP CỘNG MA TRẬN =====
// Cộng ma trận dày đặc
DenseMatrix* addDense(DenseMatrix* dm1, DenseMatrix* dm2) {
    if (dm1->rows != dm2->rows || dm1->cols != dm2->cols) return NULL;
    
    DenseMatrix* result = createDenseMatrix(dm1->rows, dm1->cols);
    
    for (int i = 0; i < dm1->rows; i++) {
        for (int j = 0; j < dm1->cols; j++) {
            result->data[i][j] = dm1->data[i][j] + dm2->data[i][j];
        }
    }
    return result;
}

// Cộng ma trận thưa
SparseMatrix* addSparse(SparseMatrix* sm1, SparseMatrix* sm2) {
    if (sm1->rows != sm2->rows || sm1->cols != sm2->cols) return NULL;
    
    // Chuyển về dạng dense để cộng, sau đó chuyển lại sparse
    DenseMatrix* dm1 = sparseToDense(sm1);
    DenseMatrix* dm2 = sparseToDense(sm2);
    DenseMatrix* sum = addDense(dm1, dm2);
    SparseMatrix* result = denseToSparse(sum);
    
    freeDenseMatrix(dm1);
    freeDenseMatrix(dm2);
    freeDenseMatrix(sum);
    
    return result;
}

// ===== PHÉP NHÂN MA TRẬN =====
// Nhân ma trận dày đặc
DenseMatrix* multiplyDense(DenseMatrix* dm1, DenseMatrix* dm2) {
    if (dm1->cols != dm2->rows) return NULL;
    
    DenseMatrix* result = createDenseMatrix(dm1->rows, dm2->cols);
    
    for (int i = 0; i < dm1->rows; i++) {
        for (int j = 0; j < dm2->cols; j++) {
            double sum = 0.0;
            for (int k = 0; k < dm1->cols; k++) {
                sum += dm1->data[i][k] * dm2->data[k][j];
            }
            result->data[i][j] = sum;
        }
    }
    return result;
}

// Nhân ma trận thưa (tối ưu)
SparseMatrix* multiplySparse(SparseMatrix* sm1, SparseMatrix* sm2) {
    if (sm1->cols != sm2->rows) return NULL;
    
    // Tạo ma trận kết quả dạng dense trước
    DenseMatrix* result = createDenseMatrix(sm1->rows, sm2->cols);
    
    // Nhân thưa: chỉ nhân khi cả hai phần tử đều khác 0
    for (int i = 0; i < sm1->num_elements; i++) {
        int row1 = sm1->elements[i].row;
        int col1 = sm1->elements[i].col;
        double val1 = sm1->elements[i].value;
        
        for (int j = 0; j < sm2->num_elements; j++) {
            int row2 = sm2->elements[j].row;
            int col2 = sm2->elements[j].col;
            double val2 = sm2->elements[j].value;
            
            if (col1 == row2) { // Điều kiện nhân ma trận
                result->data[row1][col2] += val1 * val2;
            }
        }
    }
    
    // Chuyển kết quả về dạng sparse
    SparseMatrix* sparseResult = denseToSparse(result);
    freeDenseMatrix(result);
    
    return sparseResult;
}

// ===== HÀM HIỂN THỊ =====
void printDenseMatrix(DenseMatrix* dm, const char* name) {
    printf("\n%s (%dx%d):\n", name, dm->rows, dm->cols);
    if (dm->rows > 10) {
        printf("Ma trận quá lớn, chỉ hiển thị 10x10 đầu:\n");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                printf("%6.1f ", dm->data[i][j]);
            }
            printf("\n");
        }
    } else {
        for (int i = 0; i < dm->rows; i++) {
            for (int j = 0; j < dm->cols; j++) {
                printf("%6.1f ", dm->data[i][j]);
            }
            printf("\n");
        }
    }
}

void printSparseMatrix(SparseMatrix* sm, const char* name) {
    printf("\n%s (Sparse %dx%d, %d phần tử khác 0):\n", 
           name, sm->rows, sm->cols, sm->num_elements);
    
    if (sm->num_elements <= 50) {
        printf("Danh sách phần tử khác 0:\n");
        for (int i = 0; i < sm->num_elements; i++) {
            printf("(%d,%d) = %.1f\n", 
                   sm->elements[i].row, sm->elements[i].col, sm->elements[i].value);
        }
    } else {
        printf("Quá nhiều phần tử để hiển thị (>50)\n");
    }
}

// ===== KIỂM TRA HIỆU SUẤT =====
double measureTime(clock_t start, clock_t end) {
    return ((double)(end - start) / CLOCKS_PER_SEC) * 1000.0; // milliseconds
}

void performanceTest() {
    printf("\n========================================\n");
    printf("KIỂM TRA HIỆU SUẤT MA TRẬN THƯA\n");
    printf("========================================\n");
    
    int sizes[] = {10, 100, 500, 1000};
    int num_sizes = 4;
    double density = 0.1; // 10% phần tử khác 0
    int iterations = 5; // Giảm số lần lặp để chạy nhanh hơn
    
    printf("%-10s %-12s %-15s %-15s %-10s\n", 
           "Kích thước", "Phép toán", "Dense (ms)", "Sparse (ms)", "Tăng tốc");
    printf("--------------------------------------------------------------------\n");
    
    for (int s = 0; s < num_sizes; s++) {
        int size = sizes[s];
        double dense_transpose = 0, sparse_transpose = 0;
        double dense_add = 0, sparse_add = 0;
        double dense_multiply = 0, sparse_multiply = 0;
        
        printf("Đang test kích thước %dx%d...\n", size, size);
        
        for (int iter = 0; iter < iterations; iter++) {
            // Tạo ma trận test
            DenseMatrix* dm1 = generateRandomMatrix(size, density);
            DenseMatrix* dm2 = generateRandomMatrix(size, density);
            SparseMatrix* sm1 = denseToSparse(dm1);
            SparseMatrix* sm2 = denseToSparse(dm2);
            
            // Test Transpose
            clock_t start = clock();
            DenseMatrix* dt = transposeDense(dm1);
            clock_t end = clock();
            dense_transpose += measureTime(start, end);
            freeDenseMatrix(dt);
            
            start = clock();
            SparseMatrix* st = transposeSparse(sm1);
            end = clock();
            sparse_transpose += measureTime(start, end);
            freeSparseMatrix(st);
            
            // Test Addition
            start = clock();
            DenseMatrix* da = addDense(dm1, dm2);
            end = clock();
            dense_add += measureTime(start, end);
            freeDenseMatrix(da);
            
            start = clock();
            SparseMatrix* sa = addSparse(sm1, sm2);
            end = clock();
            sparse_add += measureTime(start, end);
            freeSparseMatrix(sa);
            
            // Test Multiplication
            start = clock();
            DenseMatrix* dm = multiplyDense(dm1, dm2);
            end = clock();
            dense_multiply += measureTime(start, end);
            freeDenseMatrix(dm);
            
            start = clock();
            SparseMatrix* sm = multiplySparse(sm1, sm2);
            end = clock();
            sparse_multiply += measureTime(start, end);
            freeSparseMatrix(sm);
            
            // Dọn dẹp
            freeDenseMatrix(dm1);
            freeDenseMatrix(dm2);
            freeSparseMatrix(sm1);
            freeSparseMatrix(sm2);
        }
        
        // Tính trung bình và in kết quả
        dense_transpose /= iterations;
        sparse_transpose /= iterations;
        dense_add /= iterations;
        sparse_add /= iterations;
        dense_multiply /= iterations;
        sparse_multiply /= iterations;
        
        printf("%-10s %-12s %-15.2f %-15.2f %-10.2fx\n", 
               "", "Chuyển vị", dense_transpose, sparse_transpose,
               dense_transpose / sparse_transpose);
        
        printf("%-10s %-12s %-15.2f %-15.2f %-10.2fx\n", 
               "", "Cộng", dense_add, sparse_add,
               dense_add / sparse_add);
        
        printf("%-10s %-12s %-15.2f %-15.2f %-10.2fx\n", 
               "", "Nhân", dense_multiply, sparse_multiply,
               dense_multiply / sparse_multiply);
        
        printf("--------------------------------------------------------------------\n");
    }
}

// ===== HÀM DEMO =====
void demoOperations() {
    printf("========================================\n");
    printf("DEMO CÁC PHÉP TOÁN MA TRẬN THƯA\n");
    printf("========================================\n");
    
    // Tạo ma trận mẫu 5x5
    DenseMatrix* dm1 = generateRandomMatrix(5, 0.3);
    DenseMatrix* dm2 = generateRandomMatrix(5, 0.3);
    
    SparseMatrix* sm1 = denseToSparse(dm1);
    SparseMatrix* sm2 = denseToSparse(dm2);
    
    // Hiển thị ma trận gốc
    printDenseMatrix(dm1, "Ma trận A (Dense)");
    printSparseMatrix(sm1, "Ma trận A (Sparse)");
    
    // Demo chuyển vị
    printf("\n--- PHÉP CHUYỂN VỊ ---");
    SparseMatrix* transposed = transposeSparse(sm1);
    printSparseMatrix(transposed, "A^T (Chuyển vị)");
    freeSparseMatrix(transposed);
    
    // Demo cộng
    printf("\n--- PHÉP CỘNG ---");
    printSparseMatrix(sm2, "Ma trận B");
    SparseMatrix* sum = addSparse(sm1, sm2);
    printSparseMatrix(sum, "A + B");
    freeSparseMatrix(sum);
    
    // Demo nhân
    printf("\n--- PHÉP NHÂN ---");
    SparseMatrix* product = multiplySparse(sm1, sm2);
    printSparseMatrix(product, "A × B");
    freeSparseMatrix(product);
    
    // Dọn dẹp
    freeDenseMatrix(dm1);
    freeDenseMatrix(dm2);
    freeSparseMatrix(sm1);
    freeSparseMatrix(sm2);
}

// ===== HÀM MAIN =====
int main() {
    printf("CHƯƠNG TRÌNH PHÂN TÍCH HIỆU SUẤT MA TRẬN THƯA\n");
    printf("==============================================\n");
    
    int choice;
    
    while (1) {
        printf("\nMENU:\n");
        printf("1. Demo các phép toán cơ bản\n");
        printf("2. Kiểm tra hiệu suất (Performance Test)\n");
        printf("3. Thoát\n");
        printf("Lựa chọn của bạn: ");
        
        scanf("%d", &choice);
        
        switch (choice) {
            case 1:
                demoOperations();
                break;
            case 2:
                performanceTest();
                break;
            case 3:
                printf("Tạm biệt!\n");
                return 0;
            default:
                printf("Lựa chọn không hợp lệ!\n");
        }
    }
    
    return 0;
}