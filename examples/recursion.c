int fib_impl(int n, int a, int b) {
    if (n == 0) {
        return a;
    }

    if (n == 1) {
        return b;
    }

    return fib_impl(n - 1, b, a + b);
}

int fib(int n) {
    return fib_impl(n, 0, 1);
}

int main() {
    return fib(7);
}
