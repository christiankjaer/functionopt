int dummy(int a, int b) {
    return a;
}

int main() {
    if (1 < 2) {
        dummy(1, 2);
    } else {
        dummy(3, 4);
    }

    return 0;
}
