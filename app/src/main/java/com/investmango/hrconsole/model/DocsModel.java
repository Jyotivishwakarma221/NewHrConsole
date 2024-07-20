package com.investmango.hrconsole.model;

import java.util.List;

public class DocsModel {
    private List<String> documents;
    private boolean documentVerified;

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }

    public boolean isDocumentVerified() {
        return documentVerified;
    }

    public void setDocumentVerified(boolean documentVerified) {
        this.documentVerified = documentVerified;
    }
}
