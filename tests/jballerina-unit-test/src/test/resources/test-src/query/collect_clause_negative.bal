// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function testIncompatibleQueryResultType1() {
    int x1 = from var {x} in [{"x":2, "y":3}, {"x":4, "y":5}]
                collect [x]; // error
    string[] x2 = from var {x} in [{"x":2, "y":3}, {"x":4, "y":5}]
                    collect [x]; // error
}

function testIncompatibleQueryResultType2() {
    string x1 = from var {x} in [{"x":2, "y":3}, {"x":4, "y":5}]
                    collect int:sum(x); // error
    int[] x2 = from var {x} in [{"x":2, "y":3}, {"x":4, "y":5}]
                    collect int:sum(x); // error
}

function testIncompatibleParameterTypes() {
    int x1 = from var {x} in [{"x":"2", "y":"3"}, {"x":"4", "y":"5"}]
                collect int:sum(x); // error
    string x2 = from var {x} in [{"x":"2", "y":"3"}, {"x":"4", "y":"5"}]
                    collect int:toHexString(x); // error
    string x3 = from var {x} in [{"x":2, "y":3}, {"x":4, "y":5}]
                    collect ",".'join(x); // error
    string x4 = from var {x} in [{"x":2, "y":3}, {"x":4, "y":5}]
                collect string:'join(",", x);
}

function testInvalidExpressions1() {
    string x1 = from var {x} in [{"x":"2", "y":"3"}, {"x":"4", "y":"5"}]
                collect x; // error
    int x2 = from var {x} in [{"x":2, "y":3}, {"x":4, "y":5}]
                collect x + 2; // error
    record {| int x; |} rec = from var {x, y} in [{"x":2, "y":3}, {"x":4, "y":5}]
                                collect { x: [x] }; // error
}

// function testInvalidArgOrder() {
//     int _ = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect int:sum(salary, bonus); // error

//     int i = 2;
//     int _ = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect int:sum(salary, i); // error

//     int[] j = [];
//     int _ = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect int:sum(salary, ...j); // error

//     int _ = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect int:sum(salary + bonus, 3); // error
// }

// function testInvalidExpressions2() {
//     int[] a = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect salary; // error
// }

// function testInvalidListConstructors() {
//     int[] a = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect [salary + 2]; // error
//     int _ = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect int:sum([salary, bonus]); // error
// }

// function testInvalidExpressions3() {
//     int _ = from var {_} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect salary; // error
// }

// function foo(int... ns) returns int {
//     return 2;
// }

// function testInvalidFunctionInvocations1() {
//     int _ = from var {salary} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect foo(salary); // error
//     int[] _ = from var {salary} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect [foo(salary)]; // error
// }

function testInvalidAssignment() {
    int[6] a = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
                collect [salary]; // error
    record {| int[6] intArr; |} r = from var {salary, bonus} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
                                        collect { intArr: [salary] }; // error
}

// function testInvalidFunctionInvocations2() {
//     int _ = from var {salary} in [{salary: 2, bonus: 1}, {salary: 4, bonus: 2}]
//                 collect foo(salary, 2); // error
// }

// TODO: multiple negative collect clauses