int foo(int x, int y) {
    while (x < y) {
        x = x + 1;
    }

    return x;
}

int main() {
    int r = foo(10, 20);

    if (r != 20) {
        return 1;
    }

    return 0;
}
