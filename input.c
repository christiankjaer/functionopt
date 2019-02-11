int fib_help(int n, int a, int b) {
    if (n == 0)
        return a;
    if (n == 1)
        return b;
    return fib_help(n - 1, b, a + b);
}

int fib(int n) {
    return fib_help(n, 0, 1);
}

void println() {
}

// Driver Code
int main() {
    println();
    return fib(10, 0, 1);
}