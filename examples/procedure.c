void procedure(int parameter1, int parameter2) {
    int side_effect = parameter1 * parameter2;
}

int main() {
    procedure(21, 2);

    return 42;
}
