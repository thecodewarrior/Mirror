package com.teamwizardry.mirror.testsupport;

public class GenericCheckedExceptionMethodHolder<T extends Exception> {
    void generic() throws T { }
    <A extends Exception> void genericMethodParameters() throws A {}
    <A extends Exception> void genericClassAndMethodParameters() throws T, A {}
}
