package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.ResChangelog;

/**
 * @author Aleš Bregar
 */
public class ResChangelogImpl implements ResChangelog {

    private final int version;
    private String description;

    public ResChangelogImpl(final int version, final String description) {
        this.version = version;
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }
}
