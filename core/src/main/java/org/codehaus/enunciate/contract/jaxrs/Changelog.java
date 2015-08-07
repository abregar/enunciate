package org.codehaus.enunciate.contract.jaxrs;

/**
 * @author Ales Bregar
 */
public class Changelog {

    private int version;
    private String description;


    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
