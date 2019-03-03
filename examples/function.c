int add(int x, int y) {
    return x + y;
}

void proc(int z) {
    int side_effect = z;
}

int main() {
    int a = add(10, 20);

    proc(a);

    return 0;
}
