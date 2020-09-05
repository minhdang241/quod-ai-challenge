package Models;

public class RepoStat {
    private String repoName;
    private int numberOfCommits;
    private int avgCommitsPerDay;
    private long avgTimeIssueRemainOpened;
    private long avgTimeToMergePR;

    public RepoStat(String repoName, int numberOfCommits, int avgCommitsPerDay, long avgTimeIssueRemainOpened, long avgTimeToMergePR) {
        this.repoName = repoName;
        this.numberOfCommits = numberOfCommits;
        this.avgCommitsPerDay = avgCommitsPerDay;
        this.avgTimeIssueRemainOpened = avgTimeIssueRemainOpened;
        this.avgTimeToMergePR = avgTimeToMergePR;
    }

    public long getAvgTimeToMergePR() {
        return avgTimeToMergePR;
    }

    public void setAvgTimeToMergePR(long avgTimeToMergePR) {
        this.avgTimeToMergePR = avgTimeToMergePR;
    }

    public int getAvgCommitsPerDay() {
        return avgCommitsPerDay;
    }

    public void setAvgCommitsPerDay(int avgCommitsPerDay) {
        this.avgCommitsPerDay = avgCommitsPerDay;
    }

    public long getAvgTimeIssueRemainOpened() {
        return avgTimeIssueRemainOpened;
    }

    public void setAvgTimeIssueRemainOpened(long avgTimeIssueRemainOpened) {
        this.avgTimeIssueRemainOpened = avgTimeIssueRemainOpened;
    }

    public void incrementCommit(int commits) {
        this.numberOfCommits += commits;
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



    @Override
    public String toString() {
        return "RepoStat{" +
                "repoName='" + repoName + '\'' +
                ", numberOfCommits=" + numberOfCommits +
                '}';
    }

}
