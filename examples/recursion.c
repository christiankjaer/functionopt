void poll_n_times(int n) {
    int polling = 1;

    if (n > 0) {
        poll_n_times(n - 1);
    }
}

int make_eq(int i, int j) {
    if (i < j) {
        return make_eq(i + 1, j);
    }

    return i;
}

int main() {
    return 0;
}
