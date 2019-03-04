int simple() {
    return 0;
}

int recursive() {
    if (1 < 2) {
        return recursive();
    }

    return 0;
}

int cycle1();

int cycle3() {
    return cycle1();
}

int cycle2() {
    return cycle3();
}

int cycle1() {
    return cycle2();
}

int main() {
    return simple() + recursive() + cycle1();
}
