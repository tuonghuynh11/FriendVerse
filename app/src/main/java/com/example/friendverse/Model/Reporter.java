package com.example.friendverse.Model;

public class Reporter {
    private String id;
    private String username;
    private String reported;
    private String report;
    private String reporter;
    public Reporter() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReported() {
        return reported;
    }

    public void setReported(String reported) {
        this.reported = reported;
    }

    public Reporter(String id, String username, String reported, String report, String reporter) {
        this.id = id;
        this.username = username;
        this.reported =reported;
        this.report = report;
        this.reporter = reporter;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }
}
