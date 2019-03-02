int mul(int a, int b) {
    if (a > 30) {
        return b;
    }

    int result = a * b;

    return result;
}

int main() {
    int x = mul(21, 2);

    if (x == 42) {
        return 1;
    }

    return 0;
}
