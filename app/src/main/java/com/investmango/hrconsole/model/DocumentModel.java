package com.investmango.hrconsole.model;

import java.util.List;

public class DocumentModel
{
    private List<String> documents;

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }

    public DocumentModel() {
    }

    public DocumentModel(List<String> documents) {
        this.documents = documents;
    }

    @Override
    public String toString() {
        return "DocumentModel{" +
                "documents=" + documents +
                '}';
    }
}
