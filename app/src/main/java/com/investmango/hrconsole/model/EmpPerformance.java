package com.investmango.hrconsole.model;

import android.os.Build;

import java.util.HashMap;
import java.util.Map;

public class EmpPerformance {
    private long attendance;
    private String comment;
    private long createdOn;
    private long discipline;
    private long generalConduct;
    private long givenById;
    private String givenByName;
    private long givenToId;
    private String givenToName;
    private long id;
    private long initiative;
    private long jobKnowledge;
    private long obtainedScore;
    private String overAll;
    private long skills;
    private long teamWork;
    private long totalScore;
    private long updatedOn;
    private long workQuality;

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(long percentage) {
        this.percentage = percentage;
    }

    private float percentage;
    public EmpPerformance(){}
    public EmpPerformance(
            long attendance, String comment, long createdOn, long discipline,
            long generalConduct, long givenById, String givenByName, long givenToId,
            String givenToName, long id, long initiative, long jobKnowledge,
            long obtainedScore, String overAll, long skills, long teamWork,
            long totalScore, long updatedOn, long workQuality) {
        this.attendance = attendance;
        this.comment = comment;
        this.createdOn = createdOn;
        this.discipline = discipline;
        this.generalConduct = generalConduct;
        this.givenById = givenById;
        this.givenByName = givenByName;
        this.givenToId = givenToId;
        this.givenToName = givenToName;
        this.id = id;
        this.initiative = initiative;
        this.jobKnowledge = jobKnowledge;
        this.obtainedScore = obtainedScore;
        this.overAll = overAll;
        this.skills = skills;
        this.teamWork = teamWork;
        this.totalScore = totalScore;
        this.updatedOn = updatedOn;
        this.workQuality = workQuality;
    }

    public Long getAttendance() {
        return  attendance;
    }

    public void setAttendance(long attendance) {
        this.attendance = attendance;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public long getDiscipline() {
        return discipline;
    }

    public void setDiscipline(long discipline) {
        this.discipline = discipline;
    }

    public long getGeneralConduct() {
        return generalConduct;
    }

    public void setGeneralConduct(long generalConduct) {
        this.generalConduct = generalConduct;
    }

    public long getGivenById() {
        return givenById;
    }

    public void setGivenById(long givenById) {
        this.givenById = givenById;
    }

    public String getGivenByName() {
        return givenByName;
    }

    public void setGivenByName(String givenByName) {
        this.givenByName = givenByName;
    }

    public long getGivenToId() {
        return givenToId;
    }

    public void setGivenToId(long givenToId) {
        this.givenToId = givenToId;
    }

    public String getGivenToName() {
        return givenToName;
    }

    public void setGivenToName(String givenToName) {
        this.givenToName = givenToName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getInitiative() {
        return initiative;
    }

    public void setInitiative(long initiative) {
        this.initiative = initiative;
    }

    public long getJobKnowledge() {
        return jobKnowledge;
    }

    public void setJobKnowledge(long jobKnowledge) {
        this.jobKnowledge = jobKnowledge;
    }

    public long getObtainedScore() {
        return obtainedScore;
    }

    public void setObtainedScore(long obtainedScore) {
        this.obtainedScore = obtainedScore;
    }

    public String getOverAll() {
        if (overAll != null && !overAll.isEmpty()) {
            return convertOverAllFormat(overAll);
        }
        return null;
    }

    private String convertOverAllFormat(String serverFormat) {
        Map<String, String> formatMapping = new HashMap<>();
        formatMapping.put("EXCEPTIONAL", "Exceptional");
        formatMapping.put("EXCEEDS_EXPECTATIONS", "Exceeds Expectations");
        formatMapping.put("MEET_EXPECTATIONS", "Meet Expectations");
        formatMapping.put("NEED_IMPROVEMENTS", "Need Improvements");
        formatMapping.put("BELOW_EXPECTATIONS", "Below Expectations");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return formatMapping.getOrDefault(serverFormat, serverFormat);
        }
        return serverFormat;
    }


    public void setOverAll(String overAll) {
        this.overAll = overAll;
    }

    public long getSkills() {
        return skills;
    }

    public void setSkills(long skills) {
        this.skills = skills;
    }

    public long getTeamWork() {
        return teamWork;
    }

    public void setTeamWork(long teamWork) {
        this.teamWork = teamWork;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(long totalScore) {
        this.totalScore = totalScore;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public long getWorkQuality() {
        return workQuality;
    }

    public void setWorkQuality(long workQuality) {
        this.workQuality = workQuality;
    }
}
