int fac(int n) {
    if (n == 0) {
        return 1;
    }

    return fac(n - 1) * n;
}

int main() {
    return fac(4);
}
