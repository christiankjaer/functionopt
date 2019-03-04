int branch(int a, int b) {
    if (a < b) {
        return a;
    }

    return b;
}

int main() {
    int a = branch(1, 2);

    return 0;
}
