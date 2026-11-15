
package com.tripgoai;

public class TravelProfile {

    private int profileId;
    private String budgetLevel;
    private String preferences;

    public TravelProfile(int profileId, String budgetLevel, String preferences) {
        this.profileId = profileId;
        this.budgetLevel = budgetLevel;
        this.preferences = preferences;
    }

    public int getProfileId() {
        return profileId;
    }

    public String getBudgetLevel() {
        return budgetLevel;
    }

    public void setBudgetLevel(String budgetLevel) {
        this.budgetLevel = budgetLevel;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "Travel Profile #" + profileId
                + "\nBudget Level: " + budgetLevel
                + "\nPreferences: " + preferences;
    }
}