package com.teamwizardry.mirror.testsupport;

public class GenericCheckedExceptionMethodHolder<T extends Exception> {
    void generic() throws T { }
}
