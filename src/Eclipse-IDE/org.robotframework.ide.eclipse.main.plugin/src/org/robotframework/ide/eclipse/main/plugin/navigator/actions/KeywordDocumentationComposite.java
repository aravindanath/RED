/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;

class KeywordDocumentationComposite extends InputLoadingFormComposite {

    private InputLoadingFormComposite.InputJob collectingJob;
    private FormText argumentsText;
    private ScrolledFormText scrolledFormText;
    private FormText documentationText;

    KeywordDocumentationComposite(final Composite parent, final KeywordSpecification specification) {
        super(parent, SWT.NONE, specification.getName());
        this.collectingJob = new InputLoadingFormComposite.InputJob("Loading keyword documentation") {
            @Override
            protected Object createInput(final IProgressMonitor monitor) {
                setStatus(Status.OK_STATUS);
                return new Documentation(specification);
            }
        };
        createComposite();
    }

    @Override
    protected Composite createControl(final Composite parent) {
        setFormImage(RedImages.getKeywordImage());

        final Composite actualComposite = getToolkit().createComposite(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(actualComposite);

        argumentsText = getToolkit().createFormText(actualComposite, false);
        argumentsText.setFont("monospace", JFaceResources.getTextFont());
        argumentsText.setFont("monospace_inline", JFaceResources.getTextFont());
        argumentsText.setColor("header", getToolkit().getColors().getColor(IFormColors.TITLE));
        argumentsText.setFont("header", JFaceResources.getBannerFont());
        GridDataFactory.fillDefaults().span(2, 1).hint(400, SWT.DEFAULT).grab(true, false).applyTo(argumentsText);

        scrolledFormText = new ScrolledFormText(actualComposite, SWT.V_SCROLL | SWT.H_SCROLL, true);
        getToolkit().adapt(scrolledFormText);
        GridDataFactory.fillDefaults().span(2, 1).hint(400, 500).grab(true, true).applyTo(scrolledFormText);
        GridLayoutFactory.fillDefaults().applyTo(scrolledFormText);

        documentationText = scrolledFormText.getFormText();
        documentationText.setFont("monospace", JFaceResources.getTextFont());
        documentationText.setFont("monospace_inline", JFaceResources.getTextFont());
        documentationText.setColor("header", getToolkit().getColors().getColor(IFormColors.TITLE));
        documentationText.setFont("header", JFaceResources.getBannerFont());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(documentationText);
        documentationText.setWhitespaceNormalized(false);

        final HyperlinkAdapter hyperlinkListener = createHyperlinkListener();
        documentationText.addHyperlinkListener(hyperlinkListener);
        argumentsText.addHyperlinkListener(hyperlinkListener);
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                documentationText.removeHyperlinkListener(hyperlinkListener);
                argumentsText.removeHyperlinkListener(hyperlinkListener);
            }
        });
        return actualComposite;
    }

    @Override
    protected Composite getControl() {
        return (Composite) super.getControl();
    }

    private HyperlinkAdapter createHyperlinkListener() {
        return new HyperlinkAdapter() {
            @Override
            public void linkActivated(final HyperlinkEvent event) {
                final Object href = event.getHref();
                if (href instanceof String) {
                    try {
                        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
                                .openURL(new URL((String) href));
                    } catch (PartInitException | MalformedURLException e) {
                        throw new IllegalStateException("Unable to open hyperlink: " + event.getLabel(), e);
                    }
                }
            }
        };
    }

    @Override
    protected InputLoadingFormComposite.InputJob provideInputCollectingJob() {
        return collectingJob;
    }

    @Override
    protected void fillControl(final Object jobResult) {
        final Documentation kwSpec = (Documentation) jobResult;

        if (kwSpec.isHtml) {
            documentationText.setText(kwSpec.text, true, true);
        } else {
            argumentsText.setText(kwSpec.arguments, true, false);
            documentationText.setText(kwSpec.text, false, true);
        }
        argumentsText.layout();
        scrolledFormText.reflow(true);
        getControl().layout();
    }

    private class Documentation {
        private String arguments;
        private final String text;
        private final boolean isHtml;

        public Documentation(final KeywordSpecification spec) {
            isHtml = spec.canBeConvertedToHtml();
            if (isHtml) {
                arguments = "";
                text = "<form>" + createArgumentsDoc(spec.getArguments()) + spec.getDocumentationAsHtml() + "</form>";
            } else {
                arguments = "<form>" + createArgumentsDoc(spec.getArguments()) + "</form>";
                text = spec.getDocumentation();
            }
        }

        private String createArgumentsDoc(final List<String> arguments) {
            final StringBuilder builder = new StringBuilder("<p><b>Arguments</b></p>");
            for (final String arg : arguments) {
                builder.append("<li>");
                builder.append(arg);
                builder.append("</li>");
            }
            return builder.toString();
        }
    }
}
