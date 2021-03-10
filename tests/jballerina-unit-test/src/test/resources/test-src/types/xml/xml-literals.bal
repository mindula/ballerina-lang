function testXMLSequence() {
    string v1 = "interpolation1";
    string v2 = "interpolation2";
    xml x1 = xml `<foo>foo</foo><?foo?>text1<!--comment-->`;
    assert(x1.toString(), "<foo>foo</foo><?foo ?>text1<!--comment-->");
    xml x2 = xml `text1 text2 <foo>foo</foo>text2<!--comment-->text3 text4`;
    assert(x2.toString(), "text1 text2 <foo>foo</foo>text2<!--comment-->text3 text4");
    xml x3 = xml `text1${v1}<foo>foo</foo>text2${v2}<!--comment ${v2}-->text3`;
    assert(x3.toString(), "text1interpolation1<foo>foo</foo>text2interpolation2<!--comment interpolation2-->text3");
    xml x4 = xml `<!--comment--><?foo ${v1}?>text1${v1}<root>text2 ${v2}${v1} text3!<foo>12</foo><bar></bar></root>text2${v2}`;
    assert(x4.toString(), "<!--comment--><?foo interpolation1?>text1interpolation1<root>text2 "+
    "interpolation2interpolation1 text3!<foo>12</foo><bar></bar></root>text2interpolation2");

    xml x5 = xml `<!--comment-->text1`;
    assert(x5.toString(), "<!--comment-->text1");
    xml x6 = xml `<!--comment-->`;
    assert(x6.toString(), "<!--comment-->");
    xml<'xml:Text> x7 = xml `text1 text2`;
    assert(x7.toString(), "text1 text2");
    'xml:Text x8 = xml `text1`;
    assert(x8.toString(), "text1");
    xml<'xml:Text|'xml:Comment> x9 = xml `<!--comment-->`;
    assert(x9.toString(), "<!--comment-->");
    xml<'xml:Text>|xml<'xml:Comment> x12 = xml `<!--comment-->`;
    assert(x12.toString(), "<!--comment-->");
    'xml:Text|'xml:Comment x14 = xml `<!--comment-->`;
    assert(x14.toString(), "<!--comment-->");

    xml<'xml:Text|'xml:Comment> x10 = xml `<!--comment-->text1`;
    assert(x10.toString(), "<!--comment-->text1");

    //xml<'xml:Element|'xml:ProcessingInstruction> x11 = xml `<root> text1<foo>100</foo><foo>200</foo></root><?foo?>`;
    //assert(x11.toString(), "<root> text1<foo>100</foo><foo>200</foo></root><?foo?>");

    //xml<'xml:Text>|xml<'xml:Comment> x13 = xml `<!--comment-->text1`;
    //assert(x13.toString(), "<!--comment-->text1");

    //'xml:Text|'xml:Comment x15 = xml `<!--comment-->text1`;
    //assert(x15.toString(), "<!--comment-->text1");
}

function testXMLTextLiteral() returns [xml, xml, xml, xml, xml, xml] {
    string v1 = "11";
    string v2 = "22";
    string v3 = "33";
    xml x1 = xml `aaa`;
    xml x2 = xml `${v1}`;
    xml x3 = xml `aaa${v1}bbb${v2}ccc`;
    xml x4 = xml `aaa${v1}bbb${v2}ccc{d{}e}{f{`;
    xml x5 = xml `aaa${v1}b${"${"}bb${v2}c\}cc{d{}e}{f{`;
    xml x6 = xml ` `;
    return [x1, x2, x3, x4, x5, x6];
}

function testXMLCommentLiteral() returns [xml, xml, xml, xml, xml, xml] {
    int v1 = 11;
    string v2 = "22";
    string v3 = "33";
    xml x1 = xml `<!--aaa-->`;
    xml x2 = xml `<!--${v1}-->`;
    xml x3 = xml `<!--aaa${v1}bbb${v2}ccc-->`;
    xml x4 = xml `<!--<aaa${v1}bbb${v2}cccd->e->-f<<{>>>-->`;
    xml x5 = xml `<!---a-aa${v1}b${"${"}bb${v2}c\}cc{d{}e}{f{-->`;
    xml x6 = xml `<!---->`;
    xml x7 = xml `<!-- $ -->`;
    return [x1, x2, x3, x4, x5, x6];
}


function testXMLPILiteral() returns [xml, xml, xml, xml, xml] {
    int v1 = 11;
    string v2 = "22";
    string v3 = "33";
    xml x1 = xml `<?foo ?>`;
    xml x2 = xml `<?foo ${v1}?>`;
    xml x3 = xml `<?foo aaa${v1}bbb${v2}ccc?>`;
    xml x4 = xml `<?foo <aaa${v1}bbb${v2}ccc??d?e>?f<<{>>>?>`;
    xml x5 = xml `<?foo ?a?aa${v1}b${"${"}bb${v2}c\}cc{d{}e}{f{?>`;

    return [x1, x2, x3, x4, x5];
}

function testExpressionAsAttributeValue() returns [xml, xml, xml, xml, xml] {
    string v0 = "\"zzz\"";
    string v1 = "zzz";
    string v2 = "33>22";
    xml x1 = xml `<foo bar="${v0}"/>`;
    xml x2 = xml `<foo bar="aaa${v1}bb'b${v2}ccc?"/>`;
    xml x3 = xml `<foo bar="}aaa${v1}bbb${v2}ccc{d{}e}{f{"/>`;
    xml x4 = xml `<foo bar1='aaa{${v1}}b${"${"}b"b${v2}c\}cc{d{}e}{f{' bar2='aaa{${v1}}b${"${"}b"b${v2}c\}cc{d{}e}{f{'/>`;
    xml x5 = xml `<foo bar=""/>`;
    return [x1, x2, x3, x4, x5];
}

function testElementLiteralWithTemplateChildren() returns [xml, xml] {
    string v2 = "aaa<bbb";
    xml x1 = xml `<fname>John</fname>`;
    xml x2 = xml `<lname>Doe</lname>`;

    xml x3 = xml `<root>hello ${v2} good morning ${x1} ${x2}. Have a nice day!<foo>123</foo><bar></bar></root>`;
    xml x4 = x3/*;
    return [x3, x4];
}

function testDefineInlineNamespace() returns (xml) {
    xml x1 = xml `<foo foo="http://wso2.com" >hello</foo>`;
    return x1;
}

function testTextWithValidMultiTypeExpressions() returns (xml) {
    int v1 = 11;
    string v2 = "world";
    float v3 = 1.35;
    boolean v4 = true;

    xml x = xml `hello ${v1} ${v2}. How ${v3} are you ${v4}?`;
    return x;
}


function testArithmaticExpreesionInXMLTemplate() returns (xml) {
    xml x1 = xml `<foo id="hello ${ 3 + 6 / 3}" >hello</foo>`;
    
    return x1;
}

function f1() returns (string) {
  return "returned from a function";
}

function testFunctionCallInXMLTemplate() returns (xml) {
    xml x1 = xml `<foo>${ "<-->" + f1()}</foo>`;

    return x1;
}

function testBracketSequenceInXMLLiteral() returns (xml) {
    xml x1 = xml `{}{{ {{{ { } }} }}} - extra }`;
    xml x2 = xml `<elem>{}{{</elem>`;
    return x1 + x2;
}

function testInterpolatingVariousTypes() returns (xml) {
    int i = 42;
    float f = 3.14;
    decimal d = 31.4444;
    string s = "this-is-a-string";
    xml elem = xml `<abc/>`;

    xml ip = xml `<elem>${i}|${f}|${d}|${s}|${elem}</elem>`;
    return ip;
}

function testXMLStartTag() returns [xml, xml, xml, xml] {
    xml x1 = xml `<fname>John</fname>`;
    xml x2 = xml `<Country>US</Country>`;
    xml x3 = xml `<_foo id="hello ${ 3 + 6 / 3}" >hello</_foo>`;
    xml x4 = xml `<_-foo id="hello ${ 3 + 6 / 3}" >hello</_-foo>`;
    return [x1, x2, x3, x4];
}

function testXMLLiteralWithEscapeSequence() returns [xml, int, any[]] {
    xml x1 = xml `hello &lt; &gt; &amp;`;

    any[] elements = [];
    int i = 0;
    // There are no 'xml elements' in x1
    foreach var e in x1.elements() {
        elements[i] = e;
        i += 1;
    }
    return [x1, x1.elements().length(), elements];
}

function testDollarSignOnXMLLiteralTemplate() returns [xml, xml, xml] {
    string a = "hello";
    xml x1 = xml `<foo id="hello $${ 3 + 6 / 3}" >${a}</foo>`;
    xml x2 = xml `<foo id="hello $$${ 3 + 6 / 3}" >$${a}</foo>`;
    xml x3 = xml `<foo id="hello $$ ${ 3 + 6 / 3}" >$$ ${a}</foo>`;

    return [x1, x2, x3];
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
