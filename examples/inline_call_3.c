int loop(int a, int b) {
    while (a < b) {
        a = a + 1;
    }

    return a;
}

int main() {
    int a = loop(1, 2);

    return 0;
}
