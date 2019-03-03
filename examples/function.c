int mul(int a, int b) {
    int result = a * b;

    return result;
}

int max(int x, int y) {
    if (x > y) {
        return x;
    }

    return y;
}

int main() {
    int i = mul(21, 2);

    int j = max(42, i);

    int k = max(j, 100);

    if (k != 42) {
        return 1;
    }

    return 0;
}
