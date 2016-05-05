/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor.Argument;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
class KeywordCallArgumentsValidator implements ModelUnitValidator {

    private final IFile file;

    private final RobotToken definingToken;

    private final ProblemsReportingStrategy reporter;

    private final ArgumentsDescriptor descriptor;

    private final List<RobotToken> arguments;

    KeywordCallArgumentsValidator(final IFile file, final RobotToken definingToken,
            final ProblemsReportingStrategy reporter, final ArgumentsDescriptor descriptor,
            final List<RobotToken> arguments) {
        this.file = file;
        this.definingToken = definingToken;
        this.reporter = reporter;
        this.descriptor = descriptor;
        this.arguments = arguments;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        boolean shallContinue = validateNumberOfArguments();
        if (!shallContinue) {
            return;
        }
        final Map<String, Argument> namesToArgs = namesToArgsMapping();

        shallContinue = validatePositionalAndNamedArgsOrder(namesToArgs.keySet());
        if (!shallContinue) {
            return;
        }

        final ArgumentsBinding<Argument, RobotToken> argsMapping = mapDescriptorArgumentsToTokens(namesToArgs);

        validateArgumentsBinding(namesToArgs, argsMapping);
    }

    private boolean validateNumberOfArguments() {
        final Range<Integer> expectedArgsNumber = descriptor.getPossibleNumberOfArguments();
        final int actual = arguments.size();
        if (!expectedArgsNumber.contains(actual)) {
            if (!listIsPassed() && !dictIsPassed()) {
                final String additional = String.format("Keyword '%s' expects " + getRangesInfo(expectedArgsNumber)
                        + ", but %d " + toBeInProperForm(actual) + " provided", definingToken.getText(), actual);

                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                        .formatMessageWith(additional);
                reporter.handleProblem(problem, file, definingToken);
                return false;
            }
        }
        return true;
    }

    private boolean dictIsPassed() {
        return hasTokenOfType(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);
    }

    private boolean listIsPassed() {
        return hasTokenOfType(RobotTokenType.VARIABLES_LIST_DECLARATION);
    }

    private boolean hasTokenOfType(final RobotTokenType type) {
        return tryFind(arguments, new Predicate<RobotToken>() {

            @Override
            public boolean apply(final RobotToken argToken) {
                return argToken.getTypes().contains(type);
            }
        }).isPresent();
    }

    private Map<String, Argument> namesToArgsMapping() {
        final Map<String, Argument> argumentsWithNames = new HashMap<>();
        for (final Argument argument : descriptor) {
            argumentsWithNames.put(argument.getName(), argument);
        }
        return argumentsWithNames;
    }

    private boolean validatePositionalAndNamedArgsOrder(final Collection<String> argumentNames) {
        boolean thereWasNamedArgumentAlready = false;
        boolean thereIsAMessInOrder = false;
        for (final RobotToken arg : arguments) {
            if (isNamed(arg, argumentNames)) {
                thereWasNamedArgumentAlready = true;
            } else if (thereWasNamedArgumentAlready) {
                final String additionalMsg;
                if (arg.getText().contains("=")) {
                    final String argName = Splitter.on('=').limit(2).splitToList(arg.getText()).get(0).trim();
                    additionalMsg = ". Although this argument looks like named one, it isn't because there is no '"
                            + argName + "' argument in the keyword definition";
                } else {
                    additionalMsg = "";
                }
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.POSITIONAL_ARGUMENT_AFTER_NAMED)
                        .formatMessageWith(additionalMsg);
                reporter.handleProblem(problem, file, arg);
                thereIsAMessInOrder = true;
            }
        }
        return !thereIsAMessInOrder;
    }

    private ArgumentsBinding<Argument, RobotToken> mapDescriptorArgumentsToTokens(
            final Map<String, Argument> namesToArgs) {
        final List<RobotToken> positional = new ArrayList<>();
        final List<RobotToken> named = new ArrayList<>();
        for (final RobotToken arg : arguments) {
            if (isPositional(arg, namesToArgs.keySet())) {
                positional.add(arg);
            } else {
                named.add(arg);
            }
        }

        final ArgumentsBinding<Argument, RobotToken> mapping = new ArgumentsBinding<>();

        // map positional arguments
        int i = 0, j = 0;
        while (i < descriptor.size() && j < positional.size()) {
            final Argument definingArg = descriptor.get(i);
            final RobotToken currentToken = positional.get(j);

            mapping.bind(definingArg, currentToken);
            if (definingArg.isRequired() || definingArg.isDefault()) {
                i++;
            }
            final List<IRobotTokenType> tokenTypes = currentToken.getTypes();
            if (!(tokenTypes.contains(RobotTokenType.VARIABLES_LIST_DECLARATION) && !isNonCollectionVar(currentToken)
                    && !definingArg.isVarArg())) {
                j++;
            }
        }

        for (final RobotToken argToken : named) {
            final String name = getName(argToken);
            final Argument potentialArgument = namesToArgs.get(name);
            if (potentialArgument != null) {
                mapping.bind(potentialArgument, argToken);
            } else if (descriptor.supportsKwargs()) {
                mapping.bind(descriptor.getKwargArgument().get(), argToken);
            } else if (i < descriptor.size()) {
                mapping.bind(descriptor.get(i), argToken);
                i++;
            }
        }

        while (i < descriptor.size()) {
            if (!named.isEmpty() && named.get(named.size() - 1)
                    .getTypes()
                    .contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION)) {
                mapping.bind(descriptor.get(i), named.get(named.size() - 1));
            }
            i++;
        }
        return mapping;
    }

    private void validateArgumentsBinding(final Map<String, Argument> namesToArgs,
            final ArgumentsBinding<Argument, RobotToken> argsMapping) {
        for (final Argument arg : descriptor) {
            final List<RobotToken> values = argsMapping.getDefinitionsMapping(arg);

            if (arg.isRequired() && values.isEmpty()) {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.NO_VALUE_PROVIDED_FOR_REQUIRED_ARG)
                        .formatMessageWith(definingToken.getText(), arg.getName());
                reporter.handleProblem(problem, file, definingToken);
            } else if ((arg.isRequired() || arg.isDefault()) && values.size() > 1) {
                final String firstValue = values.get(0).getText();
                for (int i = 1; i < values.size(); i++) {
                    final RobotToken argToken = values.get(i);
                    final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.MULTIPLE_MATCH_TO_SINGLE_ARG)
                            .formatMessageWith(arg.getName(), firstValue);
                    reporter.handleProblem(problem, file, argToken);
                }
            } else if (arg.isKwArg()) {
                for (final RobotToken argToken : values) {
                    if (isPositional(argToken, namesToArgs.keySet())) {
                        final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.MISMATCHING_ARGUMENT)
                                .formatMessageWith(argToken.getText(), definingToken.getText(), arg.getName());
                        reporter.handleProblem(problem, file, argToken);
                    }
                }
            }
        }

        for (final RobotToken useSiteArg : arguments) {
            final List<Argument> defs = argsMapping.getUsageMapping(useSiteArg);

            if ((useSiteArg.getTypes().contains(RobotTokenType.VARIABLES_LIST_DECLARATION)
                    || useSiteArg.getTypes().contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION))
                    && !isNonCollectionVar(useSiteArg)) {

                if (!defs.isEmpty()) {
                    final List<Argument> required = newArrayList(filter(defs, onlyRequired()));
                    if (!required.isEmpty()) {
                        final ArgumentProblem cause = useSiteArg.getTypes()
                                .contains(RobotTokenType.VARIABLES_LIST_DECLARATION)
                                        ? ArgumentProblem.LIST_ARGUMENT_SHOULD_PROVIDE_ARGS
                                        : ArgumentProblem.DICT_ARGUMENT_SHOULD_PROVIDE_ARGS;
                        final int noOfRequiredArgs = required.size();
                        final RobotProblem problem = RobotProblem.causedBy(cause).formatMessageWith(
                                useSiteArg.getText(), noOfRequiredArgs + toPluralIfNeeded(" value", noOfRequiredArgs),
                                "[" + Joiner.on(", ").join(required) + "]");
                        reporter.handleProblem(problem, file, useSiteArg);
                    }
                } else {
                    final ArgumentProblem cause = useSiteArg.getTypes()
                            .contains(RobotTokenType.VARIABLES_LIST_DECLARATION)
                                    ? ArgumentProblem.LIST_ARGUMENT_SHOULD_PROVIDE_ARGS
                                    : ArgumentProblem.DICT_ARGUMENT_SHOULD_PROVIDE_ARGS;
                    final RobotProblem problem = RobotProblem.causedBy(cause).formatMessageWith(useSiteArg.getText(),
                            "0 values", "[]");
                    reporter.handleProblem(problem, file, useSiteArg);
                }
            }
        }
    }

    private static Predicate<Argument> onlyRequired() {
        return new Predicate<Argument>() {
            @Override
            public boolean apply(final Argument arg) {
                return arg.isRequired();
            }
        };
    }

    private boolean isNamed(final RobotToken arg, final Collection<String> argumentNames) {
        return !isPositional(arg, argumentNames);
    }

    private boolean isPositional(final RobotToken arg, final Collection<String> argumentNames) {
        final String argument = arg.getText();
        if (argument.contains("=")) {
            final String name = Splitter.on('=').limit(2).splitToList(argument).get(0);
            return !descriptor.supportsKwargs() && !argumentNames.contains(name);
        } else if (arg.getTypes().contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION)) {
            return isNonCollectionVar(arg);
        } else {
            return true;
        }
    }

    private boolean isNonCollectionVar(final RobotToken arg) {
        final MappingResult extractedVars = new VariableExtractor().extract(arg, null);
        return !extractedVars.isOnlyPossibleCollectionVariable();
    }

    private String getName(final RobotToken robotToken) {
        return Splitter.on('=').limit(2).splitToList(robotToken.getText()).get(0);
    }

    private static String getRangesInfo(final Range<Integer> range) {
        final int minArgs = range.lowerEndpoint();
        if (!range.hasUpperBound()) {
            return "at least " + minArgs + " " + toPluralIfNeeded("argument", minArgs);
        } else if (range.lowerEndpoint().equals(range.upperEndpoint())) {
            return minArgs + " " + toPluralIfNeeded("argument", minArgs);
        } else {
            final int maxArgs = range.upperEndpoint();
            return "from " + minArgs + " to " + maxArgs + " arguments";
        }
    }

    private static String toBeInProperForm(final int amount) {
        return amount == 1 ? "is" : "are";
    }

    private static String toPluralIfNeeded(final String noun, final int amount) {
        return amount == 1 ? noun : noun + "s";
    }

    private static class ArgumentsBinding<D, U> {

        public void bind(final D key, final U val) {
            defToUsageMapping.put(key, val);
            usageToDefMapping.put(val, key);
        }

        public List<U> getDefinitionsMapping(final D arg) {
            return defToUsageMapping.get(arg);
        }

        public List<D> getUsageMapping(final U arg) {
            return usageToDefMapping.get(arg);
        }

        private final ArrayListMultimap<D, U> defToUsageMapping = ArrayListMultimap.create();

        private final ArrayListMultimap<U, D> usageToDefMapping = ArrayListMultimap.create();
    }
}
