package dev.thecodewarrior.mirror.joor;

import javax.annotation.processing.Processor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Licensed under Apache-2.0
// Source: https://github.com/jOOQ/jOOR

/**
 * @author Lukas Eder
 */
public final class CompileOptions {

    final List<? extends Processor> processors;
    final List<String> options;

    public CompileOptions() {
        this(
            Collections.emptyList(),
            Collections.emptyList()
        );
    }

    private CompileOptions(
        List<? extends Processor> processors,
        List<String> options
    ) {
        this.processors = processors;
        this.options = options;
    }

    public final CompileOptions processors(Processor... newProcessors) {
        return processors(Arrays.asList(newProcessors));
    }

    public final CompileOptions processors(List<? extends Processor> newProcessors) {
        return new CompileOptions(newProcessors, options);
    }

    public final CompileOptions options(String... newOptions) {
        return options(Arrays.asList(newOptions));
    }

    public final CompileOptions options(List<String> newOptions) {
        return new CompileOptions(processors, newOptions);
    }
}

