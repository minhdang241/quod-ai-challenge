package ai.quod.challenge.Models;

public class RepoStat {
    private String repoName;
    private int numberOfCommits;
    private float avgCommitsPerDay;
    private float avgTimeIssueRemainOpened;
    private float avgTimeToMergePR;
    private float avgCommitPerDeveloper;
    private float health;

    public RepoStat(String repoName, int numberOfCommits, float avgCommitsPerDay, float avgTimeIssueRemainOpened, float avgTimeToMergePR, float avgCommitPerDeveloper) {
        this.repoName = repoName;
        this.numberOfCommits = numberOfCommits;
        this.avgCommitsPerDay = avgCommitsPerDay;
        this.avgTimeIssueRemainOpened = avgTimeIssueRemainOpened;
        this.avgTimeToMergePR = avgTimeToMergePR;
        this.avgCommitPerDeveloper = avgCommitPerDeveloper;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public int getNumberOfCommits() {
        return numberOfCommits;
    }

    public void setNumberOfCommits(int numberOfCommits) {
        this.numberOfCommits = numberOfCommits;
    }

    public float getAvgCommitsPerDay() {
        return avgCommitsPerDay;
    }

    public void setAvgCommitsPerDay(float avgCommitsPerDay) {
        this.avgCommitsPerDay = avgCommitsPerDay;
    }

    public float getAvgTimeIssueRemainOpened() {
        return avgTimeIssueRemainOpened;
    }

    public void setAvgTimeIssueRemainOpened(float avgTimeIssueRemainOpened) {
        this.avgTimeIssueRemainOpened = avgTimeIssueRemainOpened;
    }

    public float getAvgTimeToMergePR() {
        return avgTimeToMergePR;
    }

    public void setAvgTimeToMergePR(float avgTimeToMergePR) {
        this.avgTimeToMergePR = avgTimeToMergePR;
    }

    public float getAvgCommitPerDeveloper() {
        return avgCommitPerDeveloper;
    }

    public void setAvgCommitPerDeveloper(float avgCommitPerDeveloper) {
        this.avgCommitPerDeveloper = avgCommitPerDeveloper;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    @Override
    public String toString() {
        return "RepoStat{" +
                "repoName='" + repoName + '\'' +
                ", numberOfCommits=" + numberOfCommits +
                ", avgCommitsPerDay=" + avgCommitsPerDay +
                ", avgTimeIssueRemainOpened=" + avgTimeIssueRemainOpened +
                ", avgTimeToMergePR=" + avgTimeToMergePR +
                ", avgCommitPerDeveloper=" + avgCommitPerDeveloper +
                ", health=" + health +
                '}';
    }
}
