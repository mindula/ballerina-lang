public function test() {
    lock {
        fail error("error!");
    } on fail anydata error(err) {
        io:println(err);
    }
}
